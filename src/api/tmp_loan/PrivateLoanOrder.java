package api.tmp_loan;


// TODO(stfinancial): Not sure happy with the Private/Public Dichotemy here, maybe we can merge these into one class?

import api.Currency;

public final class PrivateLoanOrder {
    private final Loan loan;
    private final String orderId; // TODO(stfinancial): Consider changing this to orderNumber to stay consistent with TradeOrder
    // TODO(stfinancial): Final?
    private long timestamp;
    private int duration;
    private boolean isAutoRenew;

    // TODO(stfinancial): orderId should not be a parameter to the constructor. What if we're trying to create a loan?
    public PrivateLoanOrder(Loan loan, String orderId, long timestamp, int duration, boolean isAutoRenew) {
        this.loan = loan;
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.duration = duration;
        this.isAutoRenew = isAutoRenew;
    }

    /* Getters */
    public Loan getLoan() { return loan; }
    public String getOrderId() { return orderId; }
    public long getTimestamp() { return timestamp; }
    public int getDuration() { return duration; }
    public boolean isAutoRenew() { return isAutoRenew; }

    /* Delegation Methods */
    public double getAmount() { return loan.getAmount(); }
    public double getRate() { return loan.getRate(); }
    public LoanType getType() { return loan.getType(); }
    public Currency getCurrency() { return loan.getCurrency(); }
}
