package api;

/**
 * Created by Timothy on 3/3/17.
 */
public class MarginPosition {

    private final CurrencyPair pair;
    private final MarginType type;
    private double amount; // Borrowed amount, in quote currency.
    // TODO(stfinancial): Figure out which price this is based on.
    private double total; // Value, in non quote currency.
    private double basePrice;
    private double liquidationPrice;
    private double pl;
    private double lendingFees;

    public MarginPosition(CurrencyPair pair, MarginType type, double amount, double total, double basePrice, double liquidationPrice, double pl, double lendingFees) {
        this.pair = pair;
        this.type = type;
        this.amount = amount;
        this.total = total;
        this.basePrice = basePrice;
        this.liquidationPrice = liquidationPrice;
        this.pl = pl;
        this.lendingFees = lendingFees;
    }

    public CurrencyPair getPair() { return pair; }
    public MarginType getType() { return type; }
    public double getAmount() { return amount; }
    public double getTotal() { return total; }
    public double getBasePrice() { return basePrice; }
    public double getLiquidationPrice() { return liquidationPrice; }
    public double getPl() { return pl; }
    public double getLendingFees() { return lendingFees; }

}
