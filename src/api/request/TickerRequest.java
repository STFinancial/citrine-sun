package api.request;

import api.CurrencyPair;

import java.util.List;
import java.util.Optional;

/**
 * Created by Timothy on 2/12/17.
 */
public class TickerRequest extends MarketRequest {
    // TODO(stfinancial): If POloniex is the only market supporting getting every/multiple ticker, then consider maybe making this nonoptional and a single pair.
    // TODO(stfinancial): This is causing really obnoxious code.. probably remove it.
    private Optional<List<CurrencyPair>> pairs;

    public TickerRequest(int priority, long timestamp) {
        // TODO(stfinancial): How do we handle this for markets which do not support obtaining all tickers? Implicitly ask for all of them?
        super(priority, timestamp);
        pairs = Optional.empty();
    }

    public TickerRequest(List<CurrencyPair> pairs, int priority, long timestamp) {
        super(priority, timestamp);
        this.pairs = Optional.of(pairs);
    }

    public Optional<List<CurrencyPair>> getPairs() {
        return pairs;
    }

}
