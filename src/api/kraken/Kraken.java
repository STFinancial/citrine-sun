package api.kraken;

import api.Market;
import api.MarketConstants;
import api.request.MarketRequest;
import api.request.MarketResponse;

/**
 * Created by Timothy on 3/7/17.
 */
public class Kraken extends Market {
    private static final String NAME = "Kraken";

    @Override
    public MarketResponse processMarketRequest(MarketRequest request) {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }
}
