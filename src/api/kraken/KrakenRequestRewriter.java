package api.kraken;

import api.RequestArgs;
import api.request.*;

/**
 * Created by Timothy on 3/7/17.
 */
final class KrakenRequestRewriter {
    private static final String API_ENDPOINT = "https://api.kraken.com";

    static RequestArgs rewriteRequest(MarketRequest request) {
        return RequestArgs.unsupported();
    }
}
