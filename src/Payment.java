import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {
    private int paymentId;
    private double amount;
    private LocalDateTime paymentDate;
    private String paymentStatus; // "pending", "completed", "failed", "refunded"
    private Party party;
    private PartyGuest guest;

    public Payment(int paymentId, double amount, Party party, PartyGuest guest) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.party = party;
        this.guest = guest;
        this.paymentDate = LocalDateTime.now();
        this.paymentStatus = "pending";
    }

    public boolean processPayment() {
        System.out.println("💳 Processing payment of €" + amount + " from " + guest.getNickname());
        
        // Προσομοίωση επεξεργασίας πληρωμής (95% επιτυχία)
        boolean success = Math.random() < 0.95;
        
        if (success) {
            this.paymentStatus = "completed";
            guest.addSpending(amount);
            party.addPayment(this);
            party.getHost().addRevenue(amount);
            System.out.println("✅ Payment completed successfully!");

            DBManager.savePayment(this);
        } else {
            this.paymentStatus = "failed";
            System.out.println("❌ Payment failed. Please try again.");
        }
        
        return success;
    }

    public void generateReceipt() {
        String receiptNumber = "RCPT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        System.out.println("\n=================================");
        System.out.println("          PAYMENT RECEIPT         ");
        System.out.println("=================================");
        System.out.println("Receipt #: " + receiptNumber);
        System.out.println("Payment ID: " + paymentId);
        System.out.println("Date: " + paymentDate);
        System.out.println("Guest: " + guest.getNickname() + " (" + guest.getName() + ")");
        System.out.println("Party: #" + party.getPartyId() + " - " + party.getTheme());
        System.out.println("Amount: €" + amount);
        System.out.println("Status: " + paymentStatus);
        System.out.println("=================================\n");
    }

    public void refundPayment() {
        if (paymentStatus.equals("completed")) {
            this.paymentStatus = "refunded";
            guest.addSpending(-amount);
            System.out.println("✅ Payment refunded: €" + amount);
        } else {
            System.out.println("❌ Cannot refund payment with status: " + paymentStatus);
        }
    }

    // Getters
    public int getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public String getPaymentStatus() { return paymentStatus; }
    public Party getParty() { return party; }
    public PartyGuest getGuest() { return guest; }
}