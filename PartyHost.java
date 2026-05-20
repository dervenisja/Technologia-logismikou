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

    public Party createParty(String theme, String location, LocalDateTime startTime,
                             LocalDateTime endTime, boolean qrEnabled) {
        if (!loggedIn) {
            System.out.println(" Please login first");
            return null;
        }
        
        Party newParty = new Party(parties.size() + 1, theme, location, startTime, endTime, qrEnabled, this);
        parties.add(newParty);
        hostedPartiesCount++;
        System.out.println("✅ Party created with ID: " + newParty.getPartyId() + " by host: " + name);
        return newParty;
    }

    public void modifyPlaylist(Party party, Playlist newPlaylist) {
        if (!loggedIn) {
            System.out.println(" Please login first");
            return;
        }
        
        if (party.getStatus().equals("active")) {
            party.setPlaylist(newPlaylist);
            System.out.println(" Playlist modified for party: " + party.getPartyId());
        } else {
            System.out.println(" Cannot modify playlist. Party is not active");
        }
    }

    public void activateClosingMode(Party party) {
        if (!loggedIn) {
            System.out.println(" Please login first");
            return;
        }
        
        if (party.getStatus().equals("active")) {
            party.setStatus("closing");
            System.out.println(" Closing mode activated for party: " + party.getPartyId());
        } else {
            System.out.println(" Cannot activate closing mode");
        }
    }

    public void manageSubscription(String newPlan) {
        if (!loggedIn) {
            System.out.println(" Please login first");
            return;
        }
        
        this.subscriptionLevel = newPlan;
        System.out.println(" Subscription updated to: " + newPlan);
    }

    public void addRevenue(double amount) {
        this.totalRevenue += amount;
    }

    // Getters
    public double getHostRating() { return hostRating; }
    public int getHostedPartiesCount() { return hostedPartiesCount; }
    public String getSubscriptionLevel() { return subscriptionLevel; }
    public double getTotalRevenue() { return totalRevenue; }
    public List<Party> getParties() { return parties; }
}