package api.request;

import api.CurrencyPair;

/**
 * A {@link MarketRequest} to obtain the order book for all markets or for a given {@link CurrencyPair CurrencyPair}.
 */
public class TradeOrderBookRequest extends MarketRequest {
    private CurrencyPair currencyPair;

    public TradeOrderBookRequest(int priority, long timestamp) {
        super(priority, timestamp);
    }

    public TradeOrderBookRequest(int priority, long timestamp, CurrencyPair currencyPair) {
        this(priority, timestamp);
        this.currencyPair = currencyPair;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }
}
