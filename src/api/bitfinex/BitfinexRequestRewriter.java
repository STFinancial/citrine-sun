package api.bitfinex;

import api.request.MarketRequest;
import api.request.TickerRequest;

/**
 * Created by Timothy on 4/13/17.
 */
final class BitfinexRequestRewriter {

    static boolean rewriteRequest(MarketRequest request) {
        if (request instanceof TickerRequest) {
//            return rewriteTickerRequest((TickerRequest) request);
        }
        return false;
    }

//    private static Something rewriteTickerRequest(TickerRequest request) {
//
//    }

}
