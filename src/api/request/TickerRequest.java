package api.request;

import api.CurrencyPair;

import java.util.Collections;
import java.util.List;

/**
 * Issues a {@code MarketRequest} to obtain the {@link api.Ticker Ticker} for the specified
 * {@link CurrencyPair Currency Pairs}, if any.
 */
public class TickerRequest extends MarketRequest {
    // TODO(stfinancial): If Poloniex is the only market supporting getting every/multiple ticker, then consider maybe making this a single pair.
    private List<CurrencyPair> pairs;

    public TickerRequest(int priority, long timestamp) {
        // TODO(stfinancial): How do we handle this for markets which do not support obtaining all tickers? Implicitly ask for all of them?
        super(priority, timestamp);
        pairs = Collections.emptyList();
    }

    public TickerRequest(List<CurrencyPair> pairs, int priority, long timestamp) {
        super(priority, timestamp);
        this.pairs = Collections.unmodifiableList(pairs);
    }

    public List<CurrencyPair> getPairs() {
        return pairs;
    }
}
