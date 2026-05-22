import java.time.LocalDateTime;

public class PartyController {
    public Party processCreateParty(PartyHost host, String theme, String location, LocalDateTime start, LocalDateTime end, boolean qrEnabled) {
        System.out.println("⚙️ [PartyController] Validating party details...");
        Party newParty = host.createParty(theme, location, start, end, qrEnabled);
        PersistenceController.saveParty(newParty);
        return newParty;
    }

    public void processClosingMode(Party party, PartyHost host) {
        System.out.println("⚙️ [PartyController] Verifying host privileges...");
        if (party.getHost().getUserid() == host.getUserid()) {
            host.activateClosingMode(party);
            PersistenceController.updatePartyStatus(party.getPartyId(), "closing");
        } else {
            System.out.println("❌ [PartyController] Access Denied.");
        }
    }
    // Εξυπηρετεί το Robustness: Extend Duration (UCS6)
    public void processExtendDuration(Party party, int extraMinutes) {
        System.out.println("⚙️ [PartyController] Validating duration extension...");
        if (party.getStatus().equals("active") || party.getStatus().equals("closing")) {
            party.extendDuration(extraMinutes);
            // PersistenceController.updatePartyDuration(...) // (Σε ένα πλήρες σύστημα θα γινόταν Update στη Βάση)
            System.out.println("✅ [PartyController] Duration applied successfully.");
        } else {
            System.out.println("❌ [PartyController] Invalid duration / Party is not active.");
        }
    }

    // Εξυπηρετεί το Robustness: Manage Subscription (UCS11)
    public void processSubscription(PartyHost host, String newPlan) {
        System.out.println("⚙️ [PartyController] Validating payment plan...");
        host.manageSubscription(newPlan);
        System.out.println("✅ [PartyController] Subscription updated to: " + newPlan);
    }
}