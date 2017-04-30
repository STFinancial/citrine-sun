package api.request;

import api.CurrencyPair;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;

/**
 * A request to place a {@link Trade} for a given {@link api.Market Market}.
 */
public final class TradeRequest extends MarketRequest {
    // TODO(stfinancial): Is there a way to do interest rate for margin?
    // TODO(stfinancial): Builder pattern?

    // TODO(stfinancial): Self trade prevention flag.

    private Trade trade;
    // TODO(stfinancial): Should we just make these public?
    private boolean isMargin = false;
    private boolean isFillOrKill = false;
    private boolean isImmediateOrCancel = false;
    private boolean isPostOnly = false;
    private boolean isMarket = false;

    private boolean isStopLimit = false;
    private double stop;

    // TODO(stfinancial): Do we want a builder, a long constructor, some options, or just setters?
    // TODO(stfinancial): Go through and rename all of these timestamp to requestTimestamp
    // TODO(stfinancial): Clarify what this timestamp, I'm not even sure that I know.
    public TradeRequest(Trade trade, int priority, long timestamp) {
        super(priority, timestamp);
        this.trade = trade;
    }

    public Trade getTrade() { return trade; }
    public boolean isMargin() { return isMargin; }
    public boolean isFillOrKill() { return isFillOrKill; }
    public boolean isImmediateOrCancel() { return isImmediateOrCancel; }
    public boolean isPostOnly() { return isPostOnly; }
    public boolean isMarket() { return isMarket; }
    public boolean isStopLimit() { return isStopLimit; }
    public double getStop() { return stop; }

    /* Delegation Methods */
    public double getAmount() { return trade.getAmount(); }
    public double getRate() { return trade.getRate(); }
    public CurrencyPair getPair() { return trade.getPair(); }
    public TradeType getType() { return trade.getType(); }

    public void setIsMargin(boolean isMargin) { this.isMargin = isMargin; }
    public void setIsFillOrKill(boolean isFillOrKill) { this.isFillOrKill = isFillOrKill; }
    public void setIsImmediateOrCancel(boolean isImmediateOrCancel) { this.isImmediateOrCancel = isImmediateOrCancel; }
    public void setIsPostOnly(boolean isPostOnly) { this.isPostOnly = isPostOnly; }
    public void setIsMarket(boolean isMarket) { this.isMarket = isMarket; }
    public void setIsStopLimit(boolean isStopLimit) { this.isStopLimit = isStopLimit; }
    public void setStop(double stop) { isStopLimit = true; this.stop = stop; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("\ttrade " + trade.toString().replace("\n","\n\t")).append("\n");
        sb.append("\tisMargin: " + (isMargin ? "true" : "false")).append("\n");
        sb.append("\tisFillOrKill: " + (isFillOrKill ? "true" : "false")).append("\n");
        sb.append("\tisImmediateOrCancel: " + (isImmediateOrCancel ? "true" : "false")).append("\n");
        sb.append("\tisPostOnly: " + (isPostOnly ? "true" : "false")).append("\n");
        sb.append("\tisMarket: " + (isMarket ? "true" : "false")).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
