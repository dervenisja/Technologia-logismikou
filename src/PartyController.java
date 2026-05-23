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
    // Extend Duration (UCS6)
    public void processExtendDuration(Party party, int extraMinutes) {
        System.out.println("⚙️ [PartyController] Εφαρμογή νέας διάρκειας...");


        party.extendDuration(extraMinutes);


        PersistenceController.updatePartyDuration(party.getPartyId(), party.getEndTime());

        System.out.println("✅ [PartyController] Η διάρκεια του πάρτι άλλαξε επιτυχώς και ενημερώθηκε η MySQL!");
    }

    // Manage Subscription (UCS11)
    public void processSubscription(PartyHost host, String newPlan) {
        System.out.println("⚙️ [PartyController] Validating payment plan...");
        host.manageSubscription(newPlan);
        System.out.println("✅ [PartyController] Subscription updated to: " + newPlan);
    }
    //  επεξεργασία και αποθήκευση των ρυθμίσεων ήχου
    public void processAudioSettings(Party party, int volume, int bass, int treble) {
        System.out.println("⚙️ [PartyController] Επικύρωση ρυθμίσεων ήχου...");

        // 1. Ενημέρωση στη μνήμη (Entity)
        party.updateAudioSettings(volume, bass, treble);

        // 2. Ενημέρωση στη Βάση Δεδομένων (Persistence)
        PersistenceController.updatePartyAudio(party.getPartyId(), volume, bass, treble);

        System.out.println("✅ [PartyController] Οι ρυθμίσεις ήχου εφαρμόστηκαν επιτυχώς!");
    }
}