package api.request;

import api.CurrencyPair;

import java.util.Optional;

/**
 * Created by Timothy on 4/27/17.
 */
public class OrderBookRequest extends MarketRequest {
    // TODO(stfinancial): SHOULD WE ALLOW AN EMPTY CURRENCY HERE? Is Poloniex the only market that has this?

    private final Optional<CurrencyPair> currencyPair;
    private final int depth;

    public OrderBookRequest(int depth) {
        this.currencyPair = Optional.empty();
        this.depth = depth;
    }

    public OrderBookRequest(CurrencyPair pair, int depth) {
        this.currencyPair = Optional.of(pair);
        this.depth = depth;
    }

    // TODO(stfinancial): These optionals are kind of annoying. Do we want to get rid of them?
    public Optional<CurrencyPair> getCurrencyPair() {
        return currencyPair;
    }

    public int getDepth() {
        return depth;
    }
}
