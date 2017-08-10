package api.request;

import api.CurrencyPair;

/**
 * Obtain the ask (sell) and bid (buy) order books from a {@link api.Market Market}, generally requiring a {@link CurrencyPair} parameter.
 */
public class OrderBookRequest extends MarketRequest {
    // TODO(stfinancial): SHOULD WE ALLOW AN EMPTY CURRENCY HERE? Is Poloniex the only market that has this?

    private final CurrencyPair currencyPair;
    private final int depth;

    public OrderBookRequest(int depth) {
        this.currencyPair = null;
        this.depth = depth;
    }

    public OrderBookRequest(CurrencyPair pair, int depth) {
        this.currencyPair = pair;
        this.depth = depth;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public int getDepth() {
        return depth;
    }
}
