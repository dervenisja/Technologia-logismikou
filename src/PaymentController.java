public class PaymentController {
    public boolean processPayment(PartyGuest guest, Party party, double amount) {
        System.out.println("⚙️ [PaymentController] Formatting data and authorizing payment...");
        Payment payment = new Payment(guest.getUserid() + party.getPartyId() + (int)(Math.random()*100), amount, party, guest);
        payment.processPayment();
        payment.generateReceipt();
        guest.addSpending(amount);
        party.addPayment(payment);
        PersistenceController.savePayment(payment);
        return true;
    }
}