package api.tmp_trade;

public final class TradeOrder {

    // TODO(stfinancial): Order status: pending, open, closed, canceled, expired. Maybe unify all of these into a single class.

    private final Trade trade;
    private final String orderId;
    private final long timestamp; // TODO(stfinancial): Specify that this is a creation/placed timestamp.
    private final boolean isMargin;

    // TODO(stfinancial): Probably also want to have isStop here as well, as well as the stop limit?

    // TODO(stfinancial): Simplify constructor.
    public TradeOrder(Trade trade, String orderId, long timestamp, boolean isMargin) {
        this.trade = trade;
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.isMargin = isMargin;
    }

    public Trade getTrade() { return trade; }
    public String getOrderId() { return orderId; }
    public long getTimestamp() { return timestamp; }
    public boolean isMargin() { return isMargin; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("\torderNumber: " + orderId).append("\n");
        sb.append("\ttrade " + trade.toString().replace("\n","\n\t")).append("\n");
        sb.append("}");
        return sb.toString();
    }

    // TODO(stfinancial): Not sure how this should work. If the order number is the same, is the trade?
    @Override
    public boolean equals(Object o) {
        if (o instanceof TradeOrder) {
            TradeOrder t = (TradeOrder) o;
            return t.trade.equals(trade) && t.orderId == orderId && t.timestamp == timestamp && t.isMargin == isMargin;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + trade.hashCode();
        hash = 31 * hash + orderId.hashCode();
        hash = 31 * hash + Long.hashCode(timestamp);
        hash = 31 * hash + Boolean.hashCode(isMargin); // TODO(stfinancial): Try isMargin ? 1 : 0
        return hash;
    }

}