import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    
    // Αποθηκευμένα δεδομένα
    private static List<PartyHost> hosts;
    private static List<PartyGuest> guests;
    private static List<Party> parties;
    private static List<Song> songsLibrary;
    
    public static void main(String[] args) {
        initializeData();
        
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = getIntInput("Choose option: ");
            
            switch (choice) {
                case 1:
                    hostMenu();
                    break;
                case 2:
                    guestMenu();
                    break;
                case 3:
                    listAllParties();
                    break;
                case 4:
                    listAllGuests();
                    break;
                case 5:
                    listAllHosts();
                    break;
                case 0:
                    running = false;
                    System.out.println("Exiting application...");
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
        scanner.close();
    }

    private static void initializeData() {
        System.out.println("=== PARTY APP SYSTEM: CONNECTING TO MYSQL ===");

        // 1. Φορτώνουμε πρώτα τα τραγούδια από τη βάση
        songsLibrary = DBManager.loadSongs();

        // 2. Φορτώνουμε τους Hosts από τη βάση
        hosts = DBManager.loadHosts();

        // 3. Φορτώνουμε τους Guests από τη βάση
        guests = DBManager.loadGuests();

        // 4. Φορτώνουμε τα Πάρτι (και συνδέονται αυτόματα με τους Hosts τους)
        parties = DBManager.loadParties(hosts);

        // 5. Φορτώνουμε τις Playlists και γεμίζουμε τα πάρτι με τα τραγούδια τους από τη MySQL
        DBManager.loadPlaylistsForParties(parties, songsLibrary);

        // 6. Φορτώνουμε και τα Song Requests των Guests από τη MySQL2
        DBManager.loadSongRequestsForParties(parties, guests, songsLibrary);

        System.out.println("=============================================");
        System.out.println(" SUCCESS: All entities successfully loaded from MySQL database!");
        System.out.println("Loaded: " + hosts.size() + " Hosts, " + guests.size() + " Guests, " +
                parties.size() + " Parties, " + songsLibrary.size() + " Songs.");
        System.out.println("=============================================\n");
    }
    
    private static void printMainMenu() {
        System.out.println("\n=== PARTY APP SYSTEM ===");
        System.out.println("1. Host Actions");
        System.out.println("2. Guest Actions");
        System.out.println("3. List All Parties");
        System.out.println("4. List All Guests");
        System.out.println("5. List All Hosts");
        System.out.println("0. Exit");
    }
    
    private static void hostMenu() {
        System.out.println("\n--- HOST ACTIONS ---");
        System.out.println("Select host:");
        for (int i = 0; i < hosts.size(); i++) {
            System.out.println((i+1) + ". " + hosts.get(i).getName());
        }
        System.out.println("0. Back");
        
        int choice = getIntInput("Choice: ");
        if (choice == 0) return;
        
        PartyHost selectedHost = hosts.get(choice - 1);
        
        boolean inHostMenu = true;
        while (inHostMenu) {
            printHostSubMenu(selectedHost.getName());
            int action = getIntInput("Action: ");
            
            switch (action) {
                case 1:
                    createNewParty(selectedHost);
                    break;
                case 2:
                    selectAndModifyPlaylist(selectedHost);
                    break;
                case 3:
                    viewAndApproveRequests(selectedHost);
                    break;
                case 4:
                    selectAndActivateClosingMode(selectedHost);
                    break;
                case 5:
                    manageHostSubscription(selectedHost);
                    break;
                case 6:
                    listHostParties(selectedHost);
                    break;

                case 0:
                    inHostMenu = false;
                    break;
                default:
                    System.out.println("Invalid action");
            }
        }
    }
    
    private static void printHostSubMenu(String hostName) {
        System.out.println("\n--- " + hostName + " (Host) ---");
        System.out.println("1. Create New Party");
        System.out.println("2. Modify Playlist of a Party");
        System.out.println("3. View & Approve Guest Song Requests");
        System.out.println("4. Activate Closing Mode for a Party");
        System.out.println("5. Manage Subscription");
        System.out.println("6. List My Parties");
        System.out.println("0. Back to Main Menu");
    }
    
    private static void createNewParty(PartyHost host) {
        System.out.println("\n--- Create New Party ---");
        System.out.print("Theme: ");
        String theme = scanner.nextLine();
        System.out.print("Location: ");
        String location = scanner.nextLine();
        System.out.print("Duration (hours): ");
        int duration = getIntInput("");
        
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(duration);
        
        System.out.print("Enable QR Code? (true/false): ");
        boolean qrEnabled = scanner.nextBoolean();
        scanner.nextLine(); // clear buffer
        
        Party newParty = host.createParty(theme, location, start, end, qrEnabled);
        parties.add(newParty);
        DBManager.saveParty(newParty);
        System.out.println("Party created successfully with ID: " + newParty.getPartyId());
        
        System.out.print("Start party now? (yes/no): ");
        String startNow = scanner.nextLine();
        if (startNow.equalsIgnoreCase("yes")) {
            newParty.startParty();
        }
    }

    private static void selectAndModifyPlaylist(PartyHost host) {
        List<Party> hostParties = host.getParties();
        if (hostParties.isEmpty()) {
            System.out.println("You have no parties to modify.");
            return;
        }

        System.out.println("\nSelect party to modify playlist:");
        for (int i = 0; i < hostParties.size(); i++) {
            System.out.println((i + 1) + ". Party #" + hostParties.get(i).getPartyId() + " - " + hostParties.get(i).getTheme() + " [" + hostParties.get(i).getStatus() + "]");
        }

        int choice = getIntInput("Choice: ") - 1;
        if (choice >= 0 && choice < hostParties.size()) {
            Party party = hostParties.get(choice);

            // ΔΗΜΙΟΥΡΓΙΑ ΥΠΟΜΕΝΟΥ ΓΙΑ ΧΕΙΡΟΚΙΝΗΤΕΣ ΕΝΕΡΓΕΙΕΣ
            System.out.println("\n--- Playlist Management (Party #" + party.getPartyId() + ") ---");
            System.out.println("1. Automatically Generate Playlist (Shuffle)");
            System.out.println("2. Add Song Manually");
            System.out.println("3. Remove Song Manually");
            System.out.println("0. Cancel");
            int subChoice = getIntInput("Action: ");

            switch (subChoice) {
                case 1:
                    // Αυτόματη παραγωγή (Shuffle)
                    System.out.println("\n🔄 Running PlaylistGenerator...");
                    party.generatePlaylist(songsLibrary);
                    break;

                case 2:
                    // Χειροκίνητη Προσθήκη Τραγουδιού από τη Βάση
                    System.out.println("\n--- Available Songs in Database ---");
                    for (int i = 0; i < songsLibrary.size(); i++) {
                        Song s = songsLibrary.get(i);
                        System.out.println((i + 1) + ". " + s.getTitle() + " - " + s.getArtist() + " [" + s.getGenre() + "]");
                    }
                    int songChoice = getIntInput("Select song number to add: ") - 1;
                    if (songChoice >= 0 && songChoice < songsLibrary.size()) {
                        Song selectedSong = songsLibrary.get(songChoice);
                        party.getPlaylist().addSong(selectedSong); // Κλήση της μεθόδου σου!

                        // Συγχρονισμός με τη MySQL live
                        DBManager.saveGeneratedPlaylist(party);
                    } else {
                        System.out.println("Invalid song choice.");
                    }
                    break;

                case 3:
                    // Χειροκίνητη Αφαίρεση Τραγουδιού από την τρέχουσα Playlist
                    List<Song> currentSongs = party.getPlaylist().getSongs();
                    if (currentSongs.isEmpty()) {
                        System.out.println("The playlist is currently empty.");
                        break;
                    }
                    System.out.println("\n--- Current Playlist Songs ---");
                    for (int i = 0; i < currentSongs.size(); i++) {
                        System.out.println((i + 1) + ". " + currentSongs.get(i).getTitle());
                    }
                    int removeChoice = getIntInput("Select song number to remove: ") - 1;
                    if (removeChoice >= 0 && removeChoice < currentSongs.size()) {
                        System.out.println("❌ Removed song: " + currentSongs.get(removeChoice).getTitle());
                        currentSongs.remove(removeChoice); // Αφαίρεση από τη Java

                        // Συγχρονισμός με τη MySQL live
                        DBManager.saveGeneratedPlaylist(party);
                    } else {
                        System.out.println("Invalid choice.");
                    }
                    break;

                case 0:
                    System.out.println("Modification canceled.");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } else {
            System.out.println("Invalid party choice.");
        }
    }
    
    private static void selectAndActivateClosingMode(PartyHost host) {
        List<Party> hostParties = host.getParties();
        if (hostParties.isEmpty()) {
            System.out.println("You have no parties yet.");
            return;
        }
        
        System.out.println("\nSelect party to close:");
        for (int i = 0; i < hostParties.size(); i++) {
            Party p = hostParties.get(i);
            System.out.println((i+1) + ". Party #" + p.getPartyId() + " - " + p.getTheme() + 
                             " [" + p.getStatus() + "]");
        }
        
        int choice = getIntInput("Choice: ") - 1;
        if (choice >= 0 && choice < hostParties.size()) {
            host.activateClosingMode(hostParties.get(choice));
        }
    }
    
    private static void manageHostSubscription(PartyHost host) {
        System.out.println("\n--- Subscription Management ---");
        System.out.println("Current plan: " + host.getSubscriptionLevel());
        System.out.println("1. Upgrade to Premium");
        System.out.println("2. Downgrade to Basic");
        System.out.println("3. Cancel Subscription");
        System.out.println("0. Back");
        
        int choice = getIntInput("Choice: ");
        switch (choice) {
            case 1:
                host.manageSubscription("Premium");
                break;
            case 2:
                host.manageSubscription("Basic");
                break;
            case 3:
                host.manageSubscription("Canceled");
                break;
        }
    }
    
    private static void listHostParties(PartyHost host) {
        System.out.println("\n--- " + host.getName() + "'s Parties ---");
        List<Party> hostParties = host.getParties();
        if (hostParties.isEmpty()) {
            System.out.println("No parties found.");
        } else {
            for (Party party : hostParties) {
                System.out.println("- Party #" + party.getPartyId() + 
                                 ": " + party.getTheme() + 
                                 " | Status: " + party.getStatus() +
                                 " | Guests: " + party.getGuests().size());
            }
        }
    }
    
    private static void guestMenu() {
        System.out.println("\n--- GUEST ACTIONS ---");
        System.out.println("Select guest:");
        for (int i = 0; i < guests.size(); i++) {
            System.out.println((i+1) + ". " + guests.get(i).getName() + 
                             " (" + guests.get(i).getNickname() + ")");
        }
        System.out.println("0. Back");
        
        int choice = getIntInput("Choice: ");
        if (choice == 0) return;
        
        PartyGuest selectedGuest = guests.get(choice - 1);
        
        boolean inGuestMenu = true;
        while (inGuestMenu) {
            printGuestSubMenu(selectedGuest.getNickname());
            int action = getIntInput("Action: ");
            
            switch (action) {
                case 1:
                    joinAParty(selectedGuest);
                    break;
                case 2:
                    submitSongRequest(selectedGuest);
                    break;
                case 3:
                    scanQRCodeForParty(selectedGuest);
                    break;
                case 4:
                    leaveAParty(selectedGuest);
                    break;
                case 5:
                    listGuestParties(selectedGuest);
                    break;
                case 6:
                    selectedGuest.showMyRequests();
                    break;
                case 0:
                    inGuestMenu = false;
                    break;
                default:
                    System.out.println("Invalid action");
            }
        }
    }
    
    private static void printGuestSubMenu(String nickname) {
        System.out.println("\n--- " + nickname + " (Guest) ---");
        System.out.println("1. Join a Party");
        System.out.println("2. Submit Song Request");
        System.out.println("3. Scan QR Code");
        System.out.println("4. Leave a Party");
        System.out.println("5. List My Parties");
        System.out.println("6. Show My Song Requests");
        System.out.println("0. Back to Main Menu");
    }
    
    private static void joinAParty(PartyGuest guest) {
        System.out.println("\n--- Available Parties ---");
        boolean hasActive = false;
        for (int i = 0; i < parties.size(); i++) {
            Party p = parties.get(i);
            if (p.getStatus().equals("active")) {
                System.out.println((i+1) + ". Party #" + p.getPartyId() + 
                                 " - " + p.getTheme() + 
                                 " at " + p.getLocation());
                hasActive = true;
            }
        }
        
        if (!hasActive) {
            System.out.println("No active parties available.");
            return;
        }
        
        int choice = getIntInput("Select party: ") - 1;
        if (choice >= 0 && choice < parties.size()) {
            Party party = parties.get(choice);
            
            // Check if payment is needed (simplified)
            System.out.print("Entry fee is €15. Process payment? (yes/no): ");
            String pay = scanner.nextLine();
            if (pay.equalsIgnoreCase("yes")) {
                Payment payment = new Payment(100 + parties.size(), 15.0, party, guest);
                payment.processPayment();
                payment.generateReceipt();
            }
            
            guest.joinParty(party);
        }
    }
    
    private static void submitSongRequest(PartyGuest guest) {
        List<Party> joinedParties = guest.getJoinedParties();
        if (joinedParties.isEmpty()) {
            System.out.println("You haven't joined any party yet.");
            return;
        }
        
        System.out.println("\nSelect party:");
        for (int i = 0; i < joinedParties.size(); i++) {
            Party p = joinedParties.get(i);
            System.out.println((i+1) + ". Party #" + p.getPartyId() + " - " + p.getTheme());
        }
        
        int partyChoice = getIntInput("Choice: ") - 1;
        if (partyChoice < 0 || partyChoice >= joinedParties.size()) return;
        
        Party selectedParty = joinedParties.get(partyChoice);
        
        System.out.println("\nAvailable songs:");
        for (int i = 0; i < songsLibrary.size(); i++) {
            System.out.println((i+1) + ". " + songsLibrary.get(i).getTitle() + 
                             " by " + songsLibrary.get(i).getArtist());
        }
        
        int songChoice = getIntInput("Select song: ") - 1;
        if (songChoice >= 0 && songChoice < songsLibrary.size()) {
            guest.submitSongRequest(songsLibrary.get(songChoice), selectedParty);
        }
    }
    
    private static void scanQRCodeForParty(PartyGuest guest) {
        System.out.print("\nEnter QR Code value: ");
        String qrValue = scanner.nextLine();
        System.out.print("Enter Party ID: ");
        int partyId = getIntInput("");
        
        Party targetParty = null;
        for (Party p : parties) {
            if (p.getPartyId() == partyId) {
                targetParty = p;
                break;
            }
        }
        
        if (targetParty == null) {
            System.out.println("Party not found.");
            return;
        }
        
        QRCode qrCode = new QRCode(999, qrValue, 24, partyId);
        guest.scanQRCode(qrCode, targetParty);
    }
    
    private static void leaveAParty(PartyGuest guest) {
        List<Party> joinedParties = guest.getJoinedParties();
        if (joinedParties.isEmpty()) {
            System.out.println("You haven't joined any party.");
            return;
        }
        
        System.out.println("\nSelect party to leave:");
        for (int i = 0; i < joinedParties.size(); i++) {
            Party p = joinedParties.get(i);
            System.out.println((i+1) + ". Party #" + p.getPartyId() + " - " + p.getTheme());
        }
        
        int choice = getIntInput("Choice: ") - 1;
        if (choice >= 0 && choice < joinedParties.size()) {
            guest.leaveParty(joinedParties.get(choice));
        }
    }
    
    private static void listGuestParties(PartyGuest guest) {
        System.out.println("\n--- " + guest.getNickname() + "'s Parties ---");
        List<Party> joined = guest.getJoinedParties();
        if (joined.isEmpty()) {
            System.out.println("No parties joined.");
        } else {
            for (Party p : joined) {
                System.out.println("- Party #" + p.getPartyId() + 
                                 ": " + p.getTheme() + 
                                 " | Status: " + p.getStatus());
            }
        }
    }
    
    private static void listAllParties() {
        System.out.println("\n=== ALL PARTIES ===");
        if (parties.isEmpty()) {
            System.out.println("No parties found.");
        } else {
            for (Party p : parties) {
                System.out.println("Party #" + p.getPartyId() + 
                                 " | Theme: " + p.getTheme() +
                                 " | Host: " + p.getHost().getName() +
                                 " | Status: " + p.getStatus() +
                                 " | Guests: " + p.getGuests().size());
            }
        }
    }
    
    private static void listAllGuests() {
        System.out.println("\n=== ALL GUESTS ===");
        for (PartyGuest g : guests) {
            System.out.println("- " + g.getName() + " (" + g.getNickname() + 
                             ") | Total spent: €" + g.getTotalSpent());
        }
    }
    
    private static void listAllHosts() {
        System.out.println("\n=== ALL HOSTS ===");
        for (PartyHost h : hosts) {
            System.out.println("- " + h.getName() + 
                             " | Rating: " + h.getHostRating() +
                             " | Plan: " + h.getSubscriptionLevel() +
                             " | Parties hosted: " + h.getHostedPartiesCount());
        }
    }
    
    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // clear buffer
        return value;
    }

    private static void viewAndApproveRequests(PartyHost host) {
        List<Party> hostParties = host.getParties();
        if (hostParties.isEmpty()) {
            System.out.println("You have no parties to manage.");
            return;
        }

        System.out.println("\nSelect party to view guest requests:");
        for (int i = 0; i < hostParties.size(); i++) {
            System.out.println((i + 1) + ". Party #" + hostParties.get(i).getPartyId() + " - " + hostParties.get(i).getTheme());
        }

        int choice = getIntInput("Choice: ") - 1;
        if (choice >= 0 && choice < hostParties.size()) {
            Party party = hostParties.get(choice);
            List<SongRequest> requests = party.getSongRequests();

            List<SongRequest> pendingRequests = new java.util.ArrayList<>();
            for (SongRequest r : requests) {
                if (r.getStatus().equalsIgnoreCase("pending")) {
                    pendingRequests.add(r);
                }
            }

            if (pendingRequests.isEmpty()) {
                System.out.println("No pending song requests from guests for this party.");
                return;
            }

            System.out.println("\n--- Pending Guest Song Requests ---");
            for (int i = 0; i < pendingRequests.size(); i++) {
                SongRequest req = pendingRequests.get(i);
                System.out.println((i + 1) + ". [Guest: " + req.getRequestedBy().getName() + "] requested \"" +
                        req.getRequestedSong().getTitle() + "\" by " + req.getRequestedSong().getArtist());
            }

            int reqChoice = getIntInput("Select request number to APPROVE (or 0 to cancel): ") - 1;
            if (reqChoice >= 0 && reqChoice < pendingRequests.size()) {
                SongRequest selectedRequest = pendingRequests.get(reqChoice);

                selectedRequest.setStatus("approved");
                party.getPlaylist().addSong(selectedRequest.getRequestedSong());
                System.out.println("✅ Approved! Song \"" + selectedRequest.getRequestedSong().getTitle() + "\" added to the playlist.");

                DBManager.updateSongRequestStatus(selectedRequest.getRequestId(), "approved");
                DBManager.saveGeneratedPlaylist(party);

            } else {
                System.out.println("Action canceled.");
            }
        } else {
            System.out.println("Invalid party choice.");
        }
    }
}