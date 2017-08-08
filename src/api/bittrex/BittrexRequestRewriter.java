package api.bittrex;

import api.RequestArgs;
import api.request.MarketRequest;
import api.request.TickerRequest;
import api.request.TradeRequest;

/**
 * Created by Timothy on 8/3/17.
 */
public final class BittrexRequestRewriter {
    private static final String API_ENDPOINT = "https://bittrex.com/api/v1.1";

    RequestArgs rewriteRequest(MarketRequest request) {
        if (request instanceof TradeRequest) {
            return RequestArgs.unsupported();
        } else if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        }
        return RequestArgs.unsupported();
    }

    private RequestArgs rewriteTickerRequest(TickerRequest request) {
        if (request.getPairs().size() != 1) {
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("public");
        builder.withResource("getticker");
        builder.withParam("market", BittrexUtils.formatCurrencyPair(request.getPairs().get(0)));
        builder.isPrivate(false);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
    }

}
