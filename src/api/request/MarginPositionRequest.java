package api.request;

import api.CurrencyPair;

/**
 * Created by Timothy on 3/3/17.
 */
public class MarginPositionRequest extends MarketRequest {
    private final CurrencyPair pair;

    public MarginPositionRequest(CurrencyPair pair, int priority, long timestamp) {
        super(priority, timestamp);
        this.pair = pair;
    }

    public CurrencyPair getPair() {
        return pair;
    }
}
