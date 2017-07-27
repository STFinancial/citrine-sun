package api.request;

import api.CurrencyPair;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;

/**
 * A request to place a {@link Trade} for a given {@link api.Market Market}.
 */
public final class TradeRequest extends MarketRequest {
    // TODO(stfinancial): Dark order integration (.d assets on Kraken, also on Bitfinex)

    public enum TimeInForce {
        GOOD_TIL_CANCELLED,
        IMMEDIATE_OR_CANCEL,
        FILL_OR_KILL;
        // TODO(stfinancial): Good til time.
    }
    // TODO(stfinancial): Don't require rate for market orders.

    // TODO(stfinancial): Is there a way to do interest rate for margin?
    // TODO(stfinancial): Builder pattern?

    // TODO(stfinancial): Self trade prevention flag.

    private Trade trade;
    // TODO(stfinancial): Should we just make these public?
    private boolean isMargin = false;
    private TimeInForce timeInForce = TimeInForce.GOOD_TIL_CANCELLED;
    private boolean isPostOnly = false;
    private boolean isMarket = false;

    private boolean isStopLimit = false;
    private double stop;

    // TODO(stfinancial): Do we want a builder, a long constructor, some options, or just setters?
    // TODO(stfinancial): Go through and rename all of these timestamp to requestTimestamp
    // TODO(stfinancial): Clarify what this timestamp, I'm not even sure that I know.
    public TradeRequest(Trade trade) {
        this.trade = trade;
    }

    public Trade getTrade() { return trade; }
    public boolean isMargin() { return isMargin; }
    public TimeInForce getTimeInForce() { return timeInForce; }
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
    public void setTimeInForce(TimeInForce timeInForce) { this.timeInForce = timeInForce; }
    public void setIsPostOnly(boolean isPostOnly) { this.isPostOnly = isPostOnly; }
    public void setIsMarket(boolean isMarket) { this.isMarket = isMarket; }
    public void setIsStopLimit(boolean isStopLimit) { this.isStopLimit = isStopLimit; }
    public void setStop(double stop) { isStopLimit = true; this.stop = stop; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("\ttrade " + trade.toString().replace("\n","\n\t")).append("\n");
        sb.append("\tisMargin: " + (isMargin ? "true" : "false")).append("\n");
        sb.append("\tisPostOnly: " + (isPostOnly ? "true" : "false")).append("\n");
        sb.append("\tisMarket: " + (isMarket ? "true" : "false")).append("\n");
        sb.append("\ttimeInForce: " + timeInForce.toString()).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
