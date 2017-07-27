package api.request;

/**
 * Request to obtain the {@link api.tmp_trade.CompletedTrade CompletedTrades} for a given order id.
 */
public final class OrderTradesRequest extends MarketRequest {
    // TODO(stfinancial): Potentially rename this to OrderInfoRequest or something. Think about what this does. Compare poloniex/kraken.
    // TODO(stfinancial): Support for multiple order ids.
    private final String id;

    public OrderTradesRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
