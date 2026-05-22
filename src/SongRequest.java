import java.time.LocalDateTime;

public class SongRequest {
    private int requestId;
    private String status;
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
            return true;
        }
        return false;
    }

    public void approveRequest() {
        if (validateRequest()) {
            this.status = "approved";
        }
    }

    public void queueRequest(int position) {
        if (status.equals("approved")) {
            this.queuePosition = position;
            this.status = "queued";
        }
    }

    public void rejectRequest() {
        this.status = "rejected";
    }

    public void markAsPlayed() {
        this.status = "played";
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getQueuePosition() { return queuePosition; }
    public Song getRequestedSong() { return requestedSong; }
    public PartyGuest getRequestedBy() { return requestedBy; }
    public Party getRequestedParty() { return requestedParty; }
}