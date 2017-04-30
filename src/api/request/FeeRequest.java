package api.request;

import api.CurrencyPair;

import java.util.Optional;

/**
 * A request to obtain fee information from a market. This could be maker fees, taker fees, tier levels, etc.
 */
public final class FeeRequest extends MarketRequest {
    // TODO(stfinancial): Maybe make this into something the exchange calls directly instead?

    private final Optional<CurrencyPair> pair;

    public FeeRequest(int priority, long timestamp) {
        super(priority, timestamp);
        pair = Optional.empty();
    }

    public FeeRequest(CurrencyPair pair, int priority, long timestamp) {
        super(priority, timestamp);
        this.pair = Optional.of(pair);
    }

    public Optional<CurrencyPair> getCurrencyPair() {
        return pair;
    }
}
