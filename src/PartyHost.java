import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PartyHost extends User {
    private double hostRating;
    private int hostedPartiesCount;
    private String subscriptionLevel;
    private double totalRevenue;
    private List<Party> parties;

    public PartyHost(int userid, String name, String email, String password,
                     double hostRating, String subscriptionLevel) {
        super(userid, name, email, password);
        this.hostRating = hostRating;
        this.hostedPartiesCount = 0;
        this.subscriptionLevel = subscriptionLevel;
        this.totalRevenue = 0.0;
        this.parties = new ArrayList<>();
    }

    public Party createParty(String theme, String location, LocalDateTime startTime, LocalDateTime endTime, boolean qrEnabled) {
        Party newParty = new Party(parties.size() + 1, theme, location, startTime, endTime, qrEnabled, this);
        parties.add(newParty);
        this.hostedPartiesCount++;
        System.out.println("🎉 [UML Sync] Το πάρτι δημιουργήθηκε από τον Host: " + name);
        return newParty;
    }

    public void modifyPlaylist(Party party, Playlist newPlaylist) {
        if (party.getStatus().equals("active")) {
            party.setPlaylist(newPlaylist);
            System.out.println("🎵 [UML Sync] Η playlist τροποποιήθηκε για το πάρτι: " + party.getPartyId());
        } else {
            System.out.println("❌ [UML Sync] Αδυναμία τροποποίησης. Το πάρτι δεν είναι ενεργό.");
        }
    }

    public void activateClosingMode(Party party) {
        if (party.getStatus().equals("active")) {
            party.setStatus("closing");
            System.out.println("⏳ [UML Sync] Ενεργοποιήθηκε το Closing Mode για το πάρτι: " + party.getPartyId());
        } else {
            System.out.println("❌ [UML Sync] Αδυναμία ενεργοποίησης Closing Mode.");
        }
    }

    public void manageSubscription(String newPlan) {
        this.subscriptionLevel = newPlan;
        System.out.println("CN 💳 [UML Sync] Η συνδρομή ενημερώθηκε σε: " + newPlan);
    }

    public void addParty(Party party) {
        if (!parties.contains(party)) {
            parties.add(party);
            this.hostedPartiesCount++;
        }
    }
    public void setSubscriptionLevel(String subscriptionLevel) {
        this.subscriptionLevel = subscriptionLevel;
    }
    public int getHostId() {
        return super.getUserid(); // Με μικρό 'i' !
    }

    public void addRevenue(double amount) {
        this.totalRevenue += amount;
    }

    public double getHostRating() { return hostRating; }
    public int getHostedPartiesCount() { return hostedPartiesCount; }
    public String getSubscriptionLevel() { return subscriptionLevel; }
    public double getTotalRevenue() { return totalRevenue; }
    public List<Party> getParties() { return parties; }
}