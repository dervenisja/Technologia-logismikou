import java.util.ArrayList;
import java.util.List;

public class PartyGuest extends User {
    private String nickname;
    private int requestLimit;
    private double totalSpent;
    private List<Party> joinedParties;
    private List<SongRequest> songRequests;
    private List<QRCode> scannedQRCodes;

    public PartyGuest(int userid, String name, String email, String password,
                      String nickname, int requestLimit) {
        super(userid, name, email, password);
        this.nickname = nickname;
        this.requestLimit = requestLimit;
        this.totalSpent = 0.0;
        this.joinedParties = new ArrayList<>();
        this.songRequests = new ArrayList<>();
        this.scannedQRCodes = new ArrayList<>();
    }

    public boolean joinParty(Party party) {
        if (!joinedParties.contains(party)) {
            joinedParties.add(party);
            party.addGuest(this);
            return true;
        }
        return false;
    }

    public void leaveParty(Party party) {
        if (joinedParties.contains(party)) {
            joinedParties.remove(party);
            System.out.println("🚪 [UML Sync] Ο Guest " + nickname + " αποχώρησε από το πάρτι.");
        }
    }

    public SongRequest submitSongRequest(Song song, Party party) {
        SongRequest request = new SongRequest(songRequests.size() + 1, song, this, party);
        songRequests.add(request);
        party.addSongRequest(request);
        return request;
    }

    public boolean scanQRCode(QRCode qrCode, Party party) {
        if (qrCode.validateQR() && qrCode.getPartyId() == party.getPartyId()) {
            scannedQRCodes.add(qrCode);
            joinParty(party);
            return true;
        }
        return false;
    }

    public void addSpending(double amount) {
        this.totalSpent += amount;
    }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public int getRequestLimit() { return requestLimit; }
    public void setRequestLimit(int requestLimit) { this.requestLimit = requestLimit; }
    public double getTotalSpent() { return totalSpent; }
    public List<Party> getJoinedParties() { return joinedParties; }
    public List<SongRequest> getSongRequests() { return songRequests; }
    public List<QRCode> getScannedQRCodes() { return scannedQRCodes; }
}