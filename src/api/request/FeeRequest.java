package api.request;

import api.CurrencyPair;

import java.util.Collections;
import java.util.Set;

/**
 * A request to obtain fee information from a market. This could be maker fees, taker fees, tier levels, etc.
 */
public final class FeeRequest extends MarketRequest {
    // TODO(stfinancial): Maybe make this into something the exchange calls directly instead?

    private final Set<CurrencyPair> pairs;

    public FeeRequest() {
        pairs = Collections.emptySet();
    }

    public FeeRequest(Set<CurrencyPair> pairs) {
        this.pairs = Collections.unmodifiableSet(pairs);
    }

    public Set<CurrencyPair> getPairs() {
        return pairs;
    }
}
