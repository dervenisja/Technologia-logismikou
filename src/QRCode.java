public class QRCode {
    private int qrId;
    private String qrCodeValue;
    private int validityHours;
    private int partyId;

    public QRCode(int qrId, String qrCodeValue, int validityHours, int partyId) {
        this.qrId = qrId;
        this.qrCodeValue = qrCodeValue;
        this.validityHours = validityHours;
        this.partyId = partyId;
    }

    public boolean validateQR() {
        return qrCodeValue != null && !qrCodeValue.isEmpty();
    }

    public int getQrId() { return qrId; }
    public String getQrCodeValue() { return qrCodeValue; }
    public int getValidityHours() { return validityHours; }
    public int getPartyId() { return partyId; }
}