import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Party {
    private int partyId;
    private String theme;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // "scheduled", "active", "closing", "ended"
    private boolean qrEnabled;
    private Playlist playlist;
    private PartyHost host;
    private List<PartyGuest> guests;
    private List<SongRequest> songRequests;
    private List<Payment> payments;

    public Party(int partyId, String theme, String location, LocalDateTime startTime,
                 LocalDateTime endTime, boolean qrEnabled, PartyHost host) {
        this.partyId = partyId;
        this.theme = theme;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "scheduled";
        this.qrEnabled = qrEnabled;
        this.host = host;
        this.guests = new ArrayList<>();
        this.songRequests = new ArrayList<>();
        this.payments = new ArrayList<>();
        this.playlist = new Playlist(1, false);
    }

    public void generatePlaylist(List<Song> availableSongs) {
        if (playlist != null && availableSongs != null && !availableSongs.isEmpty()) {
            playlist.setGenerated(true);
            playlist.getSongs().clear(); // Καθαρισμός προηγούμενης λίστας

            // 1. Δημιουργούμε ένα αντίγραφο της λίστας για να μην χαλάσουμε την αρχική βιβλιοθήκη
            List<Song> shuffledSongs = new java.util.ArrayList<>(availableSongs);

            // 2. ΑΠΛΟ ΑΝΑΚΑΤΕΜΑ: Ανακατεύει όλα τα τραγούδια τυχαία
            java.util.Collections.shuffle(shuffledSongs);

            // 3. Επιλογή των 3 πρώτων τυχαίων τραγουδιών
            int songsToSelect = Math.min(3, shuffledSongs.size());
            for (int i = 0; i < songsToSelect; i++) {
                playlist.addSong(shuffledSongs.get(i));
            }

            System.out.println("✅ [UML Sync] Playlist generated dynamically with " + songsToSelect + " random songs!");

            // 4. Αποθήκευση της λίστας στη MySQL
            DBManager.saveGeneratedPlaylist(this);
        } else {
            System.out.println("❌ Cannot generate playlist. Database songs library is empty!");
        }
    }

    public void extendDuration(int extraMinutes) {
        if (status.equals("active") || status.equals("closing")) {
            this.endTime = this.endTime.plusMinutes(extraMinutes);
            System.out.println("✅ Party extended by " + extraMinutes + " minutes");
        } else {
            System.out.println("❌ Cannot extend duration now");
        }
    }

    public void closeParty() {
        if (!status.equals("ended")) {
            this.status = "ended";
            System.out.println("✅ Party " + partyId + " is now closed");
        }
    }

    public void addGuest(PartyGuest guest) {
        if (!guests.contains(guest)) {
            guests.add(guest);
            System.out.println("✅ " + guest.getNickname() + " added as guest to party " + partyId);
        }
    }

    public void addSongRequest(SongRequest request) {
        songRequests.add(request);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    public double calculateTotalRevenue() {
        double total = 0;
        for (Payment payment : payments) {
            if (payment.getPaymentStatus().equals("completed")) {
                total += payment.getAmount();
            }
        }
        return total;
    }

    // Getters & Setters
    public int getPartyId() { return partyId; }

    public void setPartyId(int partyId) {
        this.partyId = partyId;
    }

    public String getTheme() { return theme; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public PartyHost getHost() { return host; }
    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }
    public List<PartyGuest> getGuests() { return guests; }
    public List<SongRequest> getSongRequests() { return songRequests; }
    public boolean isQrEnabled() { return qrEnabled; }
    
    public void startParty() {
        if (status.equals("scheduled")) {
            this.status = "active";
            System.out.println("🎉 Party " + partyId + " has started!");
        }
    }
}

