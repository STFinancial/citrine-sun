package api;

/**
 * Created by Timothy on 3/3/17.
 */
public class MarginAccountSummary {

    private double totalValue;
    private double pl;
    private double netValue; // Currently in BTC
    private double lendingFees;

    private double totalBorrowedValue;
    private double currentMargin; // rate, not percent

    public MarginAccountSummary(double totalValue, double pl, double netValue, double lendingFees, double totalBorrowedValue, double currentMargin) {
        this.totalValue = totalValue;
        this.pl = pl;
        this.netValue = netValue;
        this.lendingFees = lendingFees;
        this.totalBorrowedValue = totalBorrowedValue;
        this.currentMargin = currentMargin;
    }

    public double getTotalValue() { return totalValue; }
    public double getPl() { return pl; }
    public double getNetValue() { return netValue; }
    public double getLendingFees() { return lendingFees; }
    public double getTotalBorrowedValue() { return totalBorrowedValue; }
    public double getCurrentMargin() { return currentMargin; }
}
