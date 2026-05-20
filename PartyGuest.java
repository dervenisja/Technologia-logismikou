import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PartyGuest extends User {
    private String nickname;
    private int requestLimit;
    private List<Party> joinedParties;
    private List<SongRequest> songRequests;
    private List<QRCode> scannedQRCodes;
    private double totalSpent;

    public PartyGuest(int userid, String name, String email, String password, 
                      String nickname, int requestLimit) {
        super(userid, name, email, password);
        this.nickname = nickname;
        this.requestLimit = requestLimit;
        this.joinedParties = new ArrayList<>();
        this.songRequests = new ArrayList<>();
        this.scannedQRCodes = new ArrayList<>();
        this.totalSpent = 0.0;
    }

    public boolean joinParty(Party party) {
        if (!loggedIn) {
            System.out.println("❌ Please login first");
            return false;
        }
        
        if (!party.getStatus().equals("active")) {
            System.out.println("❌ Cannot join party. Party is not active (status: " + party.getStatus() + ")");
            return false;
        }

        if (joinedParties.contains(party)) {
            System.out.println("❌ " + nickname + " is already a member of party #" + party.getPartyId());
            return false;
        }

        joinedParties.add(party);
        party.addGuest(this);
        System.out.println("✅ " + nickname + " successfully joined party #" + party.getPartyId() + " - " + party.getTheme());
        return true;
    }

    public SongRequest submitSongRequest(Song song, Party party) {
        if (!loggedIn) {
            System.out.println("❌ Please login first");
            return null;
        }
        
        if (!joinedParties.contains(party)) {
            System.out.println("❌ Cannot submit song request. You are not a member of this party");
            return null;
        }

        if (!party.getStatus().equals("active")) {
            System.out.println("❌ Cannot submit song request. Party is not active");
            return null;
        }

        int currentRequests = countRequestsForParty(party);
        if (currentRequests >= requestLimit) {
            System.out.println("❌ Request limit reached (" + requestLimit + "). Cannot submit more song requests for this party");
            return null;
        }

        SongRequest request = new SongRequest(songRequests.size() + 1, song, this, party);
        request.setStatus("pending");
        songRequests.add(request);
        party.addSongRequest(request);
        
        System.out.println("✅ " + nickname + " submitted song request for: " + song.getTitle() + " at party #" + party.getPartyId());

        DBManager.saveSongRequest(request);

        return request;
    }

    public boolean scanQRCode(QRCode qrCode, Party party) {
        if (!loggedIn) {
            System.out.println("❌ Please login first");
            return false;
        }
        
        if (!qrCode.validateQR()) {
            System.out.println("❌ Invalid or expired QR code");
            return false;
        }

        if (qrCode.getPartyId() != party.getPartyId()) {
            System.out.println("❌ QR code does not match this party");
            return false;
        }

        scannedQRCodes.add(qrCode);
        
        if (!joinedParties.contains(party)) {
            joinParty(party);
        }
        
        System.out.println("✅ " + nickname + " scanned QR code and gained access to party #" + party.getPartyId());
        return true;
    }

    private int countRequestsForParty(Party party) {
        int count = 0;
        for (SongRequest request : songRequests) {
            if (request.getRequestedParty() != null && 
                request.getRequestedParty().getPartyId() == party.getPartyId() &&
                !request.getStatus().equals("rejected")) {
                count++;
            }
        }
        return count;
    }

    public void leaveParty(Party party) {
        if (joinedParties.contains(party)) {
            joinedParties.remove(party);
            System.out.println("✅ " + nickname + " left party #" + party.getPartyId());
        } else {
            System.out.println("❌ You are not a member of this party");
        }
    }

    public void addSpending(double amount) {
        this.totalSpent += amount;
        System.out.println("💰 " + nickname + " spent €" + amount + " | Total: €" + totalSpent);
    }

    public List<Party> getActiveParties() {
        List<Party> active = new ArrayList<>();
        for (Party party : joinedParties) {
            if (party.getStatus().equals("active") || party.getStatus().equals("closing")) {
                active.add(party);
            }
        }
        return active;
    }

    public void showMyRequests() {
        System.out.println("\n=== Song Requests by " + nickname + " ===");
        if (songRequests.isEmpty()) {
            System.out.println("No song requests submitted yet");
        } else {
            for (SongRequest request : songRequests) {
                System.out.println("- 🎵 " + request.getRequestedSong().getTitle() + 
                                   " | Status: " + request.getStatus() +
                                   " | Party: #" + request.getRequestedParty().getPartyId());
            }
        }
    }

    // Getters
    public String getNickname() { return nickname; }
    public int getRequestLimit() { return requestLimit; }
    public List<Party> getJoinedParties() { return joinedParties; }
    public List<SongRequest> getSongRequests() { return songRequests; }
    public double getTotalSpent() { return totalSpent; }
    
    // Setters
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setRequestLimit(int requestLimit) { this.requestLimit = requestLimit; }
}