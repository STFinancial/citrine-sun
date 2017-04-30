package api.request.tmp_trade;

import api.request.MarketRequest;

/**
 * Created by Timothy on 2/15/17.
 */
public class MoveOrderRequest extends MarketRequest {
    private final long orderNumber;
    private final double rate;
    private double amount; // Default is 0, make sure this doesn't screw us over.
    // TODO(stfinancial): Check if isFillOrKill actually works.
    private boolean isImmediateOrCancel = false;
    // TODO(stfinancial): What is behavior here if we set this true and move the order to be immediately filled?
    private boolean isPostOnly = false;


    // TODO(stfinancial):
    public MoveOrderRequest(long orderNumber, double rate, int priority, long timestamp) {
        super(priority, timestamp);
        this.orderNumber = orderNumber;
        this.rate = rate;
    }

    public long getOrderNumber() { return orderNumber; }
    public double getRate() { return rate; }
    public double getAmount() { return amount; }
    public boolean isPostOnly() { return isPostOnly; }
    public boolean isImmediateOrCancel() { return isImmediateOrCancel; }

    public void setAmount(double amount) { this.amount = amount; }
    public void setIsPostOnly(boolean isPostOnly) { this.isPostOnly = isPostOnly; }
    public void setIsImmediateOrCancel(boolean isImmediateOrCancel) { this.isImmediateOrCancel = isImmediateOrCancel; }
}
