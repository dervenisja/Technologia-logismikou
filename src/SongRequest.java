import java.time.LocalDateTime;

public class SongRequest {
    private int requestId;
    private String status; // "pending", "approved", "queued", "rejected", "played"
    private LocalDateTime timestamp;
    private int queuePosition;
    private Song requestedSong;
    private PartyGuest requestedBy;
    private Party requestedParty;

    public SongRequest(int requestId, Song requestedSong, PartyGuest requestedBy, Party requestedParty) {
        this.requestId = requestId;
        this.requestedSong = requestedSong;
        this.requestedBy = requestedBy;
        this.requestedParty = requestedParty;
        this.status = "pending";
        this.timestamp = LocalDateTime.now();
        this.queuePosition = -1;
    }

    public boolean validateRequest() {
        if (requestedSong != null && status.equals("pending")) {
            System.out.println("✅ Request validated for song: " + requestedSong.getTitle());
            return true;
        }
        System.out.println("❌ Invalid request");
        return false;
    }

    public void approveRequest() {
        if (validateRequest()) {
            this.status = "approved";
            System.out.println("✅ Request approved for song: " + requestedSong.getTitle() + 
                               " (requested by " + requestedBy.getNickname() + ")");
        }
    }

    public void queueRequest(int position) {
        if (status.equals("approved")) {
            this.queuePosition = position;
            this.status = "queued";
            System.out.println("✅ Request queued at position " + position);
        }
    }

    public void rejectRequest() {
        this.status = "rejected";
        System.out.println("❌ Request rejected for song: " + requestedSong.getTitle());
    }

    public void markAsPlayed() {
        this.status = "played";
        System.out.println("✅ Song played: " + requestedSong.getTitle());
    }

    // Getters
    public int getRequestId() { return requestId; }
    public String getStatus() { return status; }
    public Song getRequestedSong() { return requestedSong; }
    public PartyGuest getRequestedBy() { return requestedBy; }
    public Party getRequestedParty() { return requestedParty; }
    public int getQueuePosition() { return queuePosition; }

    public void setStatus(String status) {
        this.status = status;
    }
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
}