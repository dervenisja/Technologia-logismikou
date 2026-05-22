import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static List<PartyHost> hosts;
    private static List<PartyGuest> guests;
    private static List<Party> parties;
    private static List<Song> songsLibrary;

    // Ορισμός των Controllers βάσει της Αρχιτεκτονικής σας (Robustness)
    private static PartyController partyController = new PartyController();
    private static AccessController accessController = new AccessController();
    private static RequestController requestController = new RequestController();
    private static PaymentController paymentController = new PaymentController();

    public static void main(String[] args) {
        initializeData();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = getIntInput("Επίλεξε: ");
            switch (choice) {
                case 1: hostMenu(); break;
                case 2: guestMenu(); break;
                case 3: listAllParties(); break;
                case 4: listAllGuests(); break;
                case 5: listAllHosts(); break;
                case 0: running = false; System.out.println("Έξοδος..."); break;
                default: System.out.println("Μη έγκυρη επιλογή");
            }
        }
        scanner.close();
    }

    private static void initializeData() {
        System.out.println("=== PARTY APP SYSTEM: CONNECTING TO MYSQL ===");
        songsLibrary = PersistenceController.loadSongs();
        hosts = PersistenceController.loadHosts();
        guests = PersistenceController.loadGuests();
        parties = PersistenceController.loadParties(hosts);
        PersistenceController.loadPlaylistsForParties(parties, songsLibrary);
        PersistenceController.loadSongRequestsForParties(parties, guests, songsLibrary);
        System.out.println("=============================================");
        System.out.println(" SUCCESS: Data loaded from MySQL database!");
        System.out.println("=============================================\n");
    }

    private static void printMainMenu() {
        System.out.println("\n=== ΚΕΝΤΡΙΚΟ ΜΕΝΟΥ ===");
        System.out.println("1. Μενού Διοργανωτή (Host)");
        System.out.println("2. Μενού Καλεσμένου (Guest)");
        System.out.println("3. Προβολή Όλων των Πάρτι");
        System.out.println("4. Προβολή Όλων των Guests");
        System.out.println("5. Προβολή Όλων των Hosts");
        System.out.println("0. Έξοδος");
    }

    // =========================================================================
    //                            ΜΕΝΟΥ HOST
    // =========================================================================
    private static void hostMenu() {
        System.out.println("\n--- ΕΠΙΛΟΓΗ HOST ---");
        for (int i = 0; i < hosts.size(); i++) {
            System.out.println((i+1) + ". " + hosts.get(i).getName());
        }
        System.out.println("0. Πίσω");
        int choice = getIntInput("Επίλεξε: ");
        if (choice == 0) return;
        PartyHost selectedHost = hosts.get(choice - 1);

        boolean inHostMenu = true;
        while (inHostMenu) {
            System.out.println("\n--- Μενού Διοργανωτή: " + selectedHost.getName() + " ---");
            System.out.println("1. Δημιουργία Νέου Πάρτι (UCS1)");
            System.out.println("2. Διαχείριση / Τροποποίηση Playlist (UCS2)");
            System.out.println("3. Προβολή & Έγκριση Αιτημάτων Τραγουδιών (UCS4)");
            System.out.println("4. Ενεργοποίηση Closing Mode (UCS5)");
            System.out.println("5. Παράταση Διάρκειας Πάρτι (UCS6)");
            System.out.println("6. Διαχείριση Συνδρομής Host (UCS7 / UCS11)");
            System.out.println("7. Συγχρονισμός Φωτισμού - Smart Lighting (UCS10)");
            System.out.println("0. Πίσω στο Κεντρικό Μενού");
            int action = getIntInput("Ενέργεια: ");
            switch (action) {
                case 1: createNewParty(selectedHost); break;
                case 2: modifyPlaylistFlow(selectedHost); break;
                case 3: viewAndApproveRequests(selectedHost); break;
                case 4: selectAndActivateClosing(selectedHost); break;
                case 5: extendDurationFlow(selectedHost); break;
                case 6: manageSubscriptionFlow(selectedHost); break;
                case 7: syncLightingFlow(); break;
                case 0: inHostMenu = false; break;
            }
        }
    }

    // Βοηθητική μέθοδος για την επιλογή πάρτι
    private static Party selectHostParty(PartyHost host) {
        if (host.getParties().isEmpty()) {
            System.out.println("❌ Δεν έχεις δημιουργήσει κανένα πάρτι ακόμα.");
            return null;
        }
        System.out.println("\n--- Τα Πάρτι μου ---");
        for (int i = 0; i < host.getParties().size(); i++) {
            System.out.println((i+1) + ". Party #" + host.getParties().get(i).getPartyId() + " - " + host.getParties().get(i).getTheme());
        }
        int choice = getIntInput("Επίλεξε πάρτι για διαχείριση: ") - 1;
        if (choice >= 0 && choice < host.getParties().size()) {
            return host.getParties().get(choice);
        }
        System.out.println("❌ Άκυρη επιλογή.");
        return null;
    }

    private static void createNewParty(PartyHost host) {
        System.out.println("\n--- Δημιουργία Πάρτι ---");
        System.out.print("Θέμα: "); String theme = scanner.nextLine();
        System.out.print("Τοποθεσία: "); String location = scanner.nextLine();
        int duration = getIntInput("Διάρκεια (ώρες): ");

        Party p = partyController.processCreateParty(host, theme, location, LocalDateTime.now(), LocalDateTime.now().plusHours(duration), true);
        parties.add(p);

        System.out.print("Να ξεκινήσει το πάρτι τώρα; (yes/no): ");
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            p.startParty();
        }
    }

    private static void modifyPlaylistFlow(PartyHost host) {
        Party party = selectHostParty(host);
        if (party == null) return;

        System.out.println("\n--- Διαχείριση Playlist (" + party.getTheme() + ") ---");
        System.out.println("1. Αυτόματη Δημιουργία (Shuffle)");
        System.out.println("2. Προσθήκη Τραγουδιού Χειροκίνητα");
        System.out.println("3. Αφαίρεση Τραγουδιού Χειροκίνητα");
        int sub = getIntInput("Επιλογή: ");

        if (sub == 1) {
            party.generatePlaylist(songsLibrary);

        } else if (sub == 2) {
            System.out.println("\n--- Διαθέσιμα Τραγούδια ---");
            for (int i = 0; i < songsLibrary.size(); i++) {
                System.out.println((i+1) + ". " + songsLibrary.get(i).getTitle());
            }
            int sChoice = getIntInput("Επίλεξε τραγούδι για ΠΡΟΣΘΗΚΗ: ") - 1;
            if (sChoice >= 0 && sChoice < songsLibrary.size()) {
                party.getPlaylist().addSong(songsLibrary.get(sChoice));
                PersistenceController.saveGeneratedPlaylist(party);
            }

        } else if (sub == 3) {
            List<Song> currentSongs = party.getPlaylist().getSongs();
            if (currentSongs.isEmpty()) {
                System.out.println("❌ Η playlist αυτού του πάρτι είναι άδεια.");
                return;
            }
            System.out.println("\n--- Τραγούδια στην Playlist ---");
            for (int i = 0; i < currentSongs.size(); i++) {
                System.out.println((i+1) + ". " + currentSongs.get(i).getTitle());
            }
            int sChoice = getIntInput("Επίλεξε τραγούδι για ΑΦΑΙΡΕΣΗ: ") - 1;
            if (sChoice >= 0 && sChoice < currentSongs.size()) {
                int songIdToRemove = currentSongs.get(sChoice).getSongId();
                party.getPlaylist().removeSong(songIdToRemove);
                PersistenceController.saveGeneratedPlaylist(party);
            }
        }
    }

    private static void viewAndApproveRequests(PartyHost host) {
        Party party = selectHostParty(host);
        if (party == null) return;

        java.util.List<SongRequest> pending = new java.util.ArrayList<>();
        for (SongRequest r : party.getSongRequests()) {
            if (r.getStatus().equalsIgnoreCase("pending")) pending.add(r);
        }
        if (pending.isEmpty()) {
            System.out.println("Δεν υπάρχουν εκκρεμή αιτήματα για αυτό το πάρτι.");
            return;
        }

        for (int i = 0; i < pending.size(); i++) {
            System.out.println((i+1) + ". [Από: " + pending.get(i).getRequestedBy().getNickname() + "] Τραγούδι: " + pending.get(i).getRequestedSong().getTitle());
        }
        int reqChoice = getIntInput("Επίλεξε νούμερο για ΕΓΚΡΙΣΗ (0 για ακύρωση): ") - 1;
        if (reqChoice >= 0 && reqChoice < pending.size()) {
            requestController.processApproval(pending.get(reqChoice), party);
            System.out.println("✅ Το αίτημα εγκρίθηκε επιτυχώς μέσω των Controllers!");
        }
    }

    private static void selectAndActivateClosing(PartyHost host) {
        Party party = selectHostParty(host);
        if (party != null) {
            partyController.processClosingMode(party, host);
        }
    }

    private static void extendDurationFlow(PartyHost host) {
        Party party = selectHostParty(host);
        if (party != null) {
            int extra = getIntInput("Πόσα έξτρα λεπτά θέλεις να δώσεις; ");
            partyController.processExtendDuration(party, extra);
        }
    }

    private static void manageSubscriptionFlow(PartyHost host) {
        System.out.println("\nΤρέχον Πλάνο: " + host.getSubscriptionLevel());
        System.out.print("Πληκτρολόγησε νέο πλάνο (π.χ. Premium, Pro): ");
        String plan = scanner.nextLine();
        partyController.processSubscription(host, plan);
    }

    private static void syncLightingFlow() {
        System.out.println("\n🎛️ [System] Initiate Lighting Sync...");
        System.out.println("🎵 [System] Extract beat from current track...");
        System.out.println("🎨 [System] Map audio frequencies to RGB colors...");
        System.out.println("💡 [System] Send DMX commands to light fixtures... ✅ DONE!");
    }


    // =========================================================================
    //                            ΜΕΝΟΥ GUEST
    // =========================================================================
    private static void guestMenu() {
        System.out.println("\n--- ΕΠΙΛΟΓΗ GUEST ---");
        for (int i = 0; i < guests.size(); i++) {
            System.out.println((i+1) + ". " + guests.get(i).getName() + " (" + guests.get(i).getNickname() + ")");
        }
        System.out.println("0. Πίσω");
        int choice = getIntInput("Επίλεξε: ");
        if (choice == 0) return;
        PartyGuest selectedGuest = guests.get(choice - 1);

        boolean inGuestMenu = true;
        while (inGuestMenu) {
            System.out.println("\n--- Μενού Guest: " + selectedGuest.getNickname() + " ---");
            System.out.println("1. Είσοδος σε Πάρτι (Join & Πληρωμή) (UCS12)");
            System.out.println("2. Σκανάρισμα QR Code & Υποβολή Αιτήματος Τραγουδιού (UCS8 & UCS9)");
            System.out.println("3. Προβολή των Αιτημάτων μου");
            System.out.println("0. Πίσω");
            int action = getIntInput("Ενέργεια: ");
            switch (action) {
                case 1: joinPartyFlow(selectedGuest); break; // <--- Άλλαξε το όνομα εδώ
                case 2: scanQRAndSubmitRequestFlow(selectedGuest); break; // <--- Και εδώ
                case 3: selectedGuest.getSongRequests().forEach(r -> System.out.println("- " + r.getRequestedSong().getTitle() + " [" + r.getStatus() + "]")); break;
                case 0: inGuestMenu = false; break;
            }
        }
    }
    // 1. Η νέα μέθοδος μόνο για Είσοδο & Πληρωμή
    private static void joinPartyFlow(PartyGuest guest) {
        System.out.println("\n--- Διαθέσιμα Πάρτι για Είσοδο ---");
        for (int i = 0; i < parties.size(); i++) {
            System.out.println("ID: " + parties.get(i).getPartyId() + " - " + parties.get(i).getTheme());
        }

        int partyId = getIntInput("\nΠληκτρολόγησε το ID του πάρτι για είσοδο: ");
        Party targetParty = null;
        for (Party p : parties) {
            if (p.getPartyId() == partyId) targetParty = p;
        }

        if (targetParty != null) {
            System.out.print("Απαιτείται πληρωμή εισόδου €15. Να προχωρήσει; (yes/no): ");
            if (scanner.nextLine().equalsIgnoreCase("yes")) {
                if (paymentController.processPayment(guest, targetParty, 15.0)) {
                    accessController.processJoinParty(guest, targetParty); // Απλό Join
                    System.out.println("✅ Έγινες επιτυχώς μέλος στο πάρτι!");
                }
            } else {
                System.out.println("❌ Η πληρωμή ακυρώθηκε. Δεν επιτρέπεται η είσοδος.");
            }
        } else {
            System.out.println("❌ Το πάρτι δεν βρέθηκε.");
        }
    }

    //μέθοδος: QR Code + Song Request
    private static void scanQRAndSubmitRequestFlow(PartyGuest guest) {
        if (guest.getJoinedParties().isEmpty()) {
            System.out.println("❌ ΣΦΑΛΜΑ: Πρέπει πρώτα να έχεις κάνει είσοδο (Επιλογή 1) σε κάποιο πάρτι!");
            return;
        }

        Party party = guest.getJoinedParties().get(0);
        System.out.println("\nΒρίσκεσαι στο πάρτι: " + party.getTheme());

        // ΒΗΜΑ Α: ΣΚΑΝΑΡΙΣΜΑ QR CODE (UCS8)
        System.out.println("📷 Απαραίτητο Σκανάρισμα QR Code για να ζητήσεις τραγούδι...");
        int qrPartyId = getIntInput("Πληκτρολόγησε το ID του πάρτι που βλέπεις στο QR Code του μαγαζιού: ");

        QRCode qr = new QRCode(1, "QR_VALID_" + qrPartyId, 24, qrPartyId);

        if (accessController.processQRCode(guest, qr, party)) {
            System.out.println("✅ Επιβεβαίωση φυσικής παρουσίας! Το QR Code είναι έγκυρο.");

            // ΒΗΜΑ Β: ΥΠΟΒΟΛΗ ΑΙΤΗΜΑΤΟΣ (UCS9)
            System.out.println("\n--- Διαθέσιμα Τραγούδια ---");
            for (int i = 0; i < songsLibrary.size(); i++) {
                System.out.println((i+1) + ". " + songsLibrary.get(i).getTitle() + " - " + songsLibrary.get(i).getArtist());
            }
            int sChoice = getIntInput("Ποιο τραγούδι θέλεις να ζητήσεις; (Επίλεξε νούμερο): ") - 1;
            if (sChoice >= 0 && sChoice < songsLibrary.size()) {
                requestController.processSongRequest(guest, songsLibrary.get(sChoice), party);
            }
        } else {
            System.out.println("❌ Άκυρο QR Code! Φαίνεται να μην βρίσκεσαι στον χώρο του πάρτι. Αίτημα απορρίφθηκε.");
        }
    }


    // =========================================================================
    //                            ΒΟΗΘΗΤΙΚΕΣ
    // =========================================================================
    private static void listAllParties() {
        parties.forEach(p -> System.out.println("Party #" + p.getPartyId() + " | Theme: " + p.getTheme() + " | Status: " + p.getStatus()));
    }
    private static void listAllGuests() {
        guests.forEach(g -> System.out.println("- " + g.getName() + " | Total Spent: €" + g.getTotalSpent()));
    }
    private static void listAllHosts() {
        hosts.forEach(h -> System.out.println("- " + h.getName() + " | Rating: " + h.getHostRating()));
    }

    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) { scanner.next(); }
        int val = scanner.nextInt(); scanner.nextLine();
        return val;
    }
}