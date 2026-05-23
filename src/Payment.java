import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private double amount;
    private LocalDateTime paymentDate;
    private String paymentStatus;
    private Party party;
    private PartyGuest guest;

    public Payment(int paymentId, double amount, Party party, PartyGuest guest) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentDate = LocalDateTime.now();
        this.paymentStatus = "pending";
        this.party = party;
        this.guest = guest;
    }

    public void processPayment() {
        this.paymentStatus = "completed";
    }

    public void generateReceipt() {
        System.out.println("\n=================================");
        System.out.println("          PAYMENT RECEIPT        ");
        System.out.println("=================================");
        System.out.println("Receipt #: RCPT_" + Integer.toHexString(paymentId).toUpperCase());
        System.out.println("Date: " + paymentDate);
        System.out.println("Guest: " + guest.getNickname() + " (" + guest.getName() + ")");
        System.out.println("Party: #" + party.getPartyId() + " - " + party.getTheme());
        System.out.println("Amount: €" + amount);
        System.out.println("Status: " + paymentStatus);
        System.out.println("=================================\n");
    }

    public int getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Party getParty() { return party; }
    public PartyGuest getGuest() { return guest; }
}