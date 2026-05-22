import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Party {
    private int partyId;
    private String theme;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private boolean qrEnabled;
    private PartyHost host;
    private Playlist playlist;
    private List<PartyGuest> guests;
    private List<SongRequest> songRequests;
    private List<Payment> payments;

    public Party(int partyId, String theme, String location, LocalDateTime startTime, LocalDateTime endTime, boolean qrEnabled, PartyHost host) {
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
        this.playlist = new Playlist(partyId, false);
    }

    public void startParty() {
        this.status = "active";
        System.out.println("🎉 [UML Sync] Το πάρτι ξεκίνησε!");
    }

    public void extendDuration(int extraMinutes) {
        this.endTime = this.endTime.plusMinutes(extraMinutes);
        System.out.println("⏳ [UML Sync] Η διάρκεια του πάρτι παρατάθηκε κατά " + extraMinutes + " λεπτά.");
    }

    public void closeParty() {
        this.status = "ended";
        System.out.println("🛑 [UML Sync] Το πάρτι έκλεισε.");
    }

    public double calculateTotalRevenue() {
        double total = 0;
        for (Payment p : payments) {
            if (p.getPaymentStatus().equals("completed")) {
                total += p.getAmount();
            }
        }
        return total;
    }

    public void generatePlaylist(List<Song> availableSongs) {
        if (playlist != null && availableSongs != null && !availableSongs.isEmpty()) {
            playlist.setGenerated(true);
            playlist.getSongs().clear();
            List<Song> shuffledSongs = new ArrayList<>(availableSongs);
            java.util.Collections.shuffle(shuffledSongs);
            int songsToSelect = Math.min(3, shuffledSongs.size());
            for (int i = 0; i < songsToSelect; i++) {
                playlist.addSong(shuffledSongs.get(i));
            }
            System.out.println("✅ [UML Sync] Playlist generated dynamically with " + songsToSelect + " random songs!");
            PersistenceController.saveGeneratedPlaylist(this);
        }
    }

    public void addGuest(PartyGuest guest) {
        if (!guests.contains(guest)) {
            guests.add(guest);
        }
    }

    public void addSongRequest(SongRequest request) {
        songRequests.add(request);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    public int getPartyId() { return partyId; }
    public void setPartyId(int partyId) { this.partyId = partyId; }
    public String getTheme() { return theme; }
    public String getLocation() { return location; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isQrEnabled() { return qrEnabled; }
    public PartyHost getHost() { return host; }
    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }
    public List<PartyGuest> getGuests() { return guests; }
    public List<SongRequest> getSongRequests() { return songRequests; }
    public List<Payment> getPayments() { return payments; }
}