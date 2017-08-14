package api.request;

import api.CurrencyPair;

import java.util.Collections;
import java.util.List;

/**
 * Issues a {@code MarketRequest} to obtain the {@link api.Ticker Ticker} for the specified
 * {@link CurrencyPair Currency Pairs}, if any.
 */
public class TickerRequest extends MarketRequest {
    private List<CurrencyPair> pairs;

    public TickerRequest() {
        pairs = Collections.emptyList();
    }

    public TickerRequest(List<CurrencyPair> pairs) {
        // TODO(stfinancial): Is this excessive?
        this.pairs = Collections.unmodifiableList(pairs);
    }

    public List<CurrencyPair> getPairs() {
        return pairs;
    }
}
