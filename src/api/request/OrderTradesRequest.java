package api.request;

/**
 * Request to obtain the {@link api.tmp_trade.CompletedTrade CompletedTrades} for a given order id.
 */
public final class OrderTradesRequest extends MarketRequest {
    private final String id;

    public OrderTradesRequest(String id, int priority, long timestamp) {
        super(priority, timestamp);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
