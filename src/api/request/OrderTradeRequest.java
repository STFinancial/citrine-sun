package api.request;

/**
 * Request to obtain the {@link api.tmp_trade.CompletedTrade CompletedTrades} for a given order id.
 */
public final class OrderTradeRequest extends MarketRequest {
    private final String id;

    public OrderTradeRequest(String id, int priority, long timestamp) {
        super(priority, timestamp);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
