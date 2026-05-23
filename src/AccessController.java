public class AccessController {
    public boolean processJoinParty(PartyGuest guest, Party party) {
        System.out.println("⚙️ [AccessController] Checking party status for: " + party.getTheme());
        if (!party.getStatus().equals("active")) {
            System.out.println("❌ [AccessController] Validation failed: Party is not active.");
            return false;
        }
        if (guest.getJoinedParties().contains(party)) {
            System.out.println("❌ [AccessController] Validation failed: Guest is already in.");
            return false;
        }
        boolean joined = guest.joinParty(party);
        if (joined) {
            PersistenceController.saveGuestJoin(guest, party);
            System.out.println("✅ [AccessController] Access granted.");
        }
        return joined;
    }

    public boolean processQRCode(PartyGuest guest, QRCode qrCode, Party party) {
        System.out.println("⚙️ [AccessController] Decoding and validating QR Code...");
        boolean success = guest.scanQRCode(qrCode, party);
        if (success) {
            PersistenceController.saveGuestJoin(guest, party);
        }
        return success;
    }

}