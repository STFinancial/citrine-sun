package api.kraken;

import api.CurrencyPair;
import api.RequestArgs;
import api.request.*;

/**
 * Created by Timothy on 3/7/17.
 */
final class KrakenRequestRewriter {
    private static final String API_ENDPOINT = "https://api.kraken.com/0";

    static RequestArgs rewriteRequest(MarketRequest request) {
        if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        }
        return RequestArgs.unsupported();
    }

    private static RequestArgs rewriteTickerRequest(TickerRequest request) {
        if (request.getPairs().isEmpty()) {
            System.out.println("Kraken requires that ticker pairs be specified.");
            // TODO(stfinancial): Probably need to throw something here.
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        StringBuilder pairs = new StringBuilder();
        for (CurrencyPair pair : request.getPairs()) {
            pairs.append(KrakenUtils.formatCurrencyPair(pair)).append(",");
        }
        // TODO(stfinancial): Gross, find another way.
        pairs.deleteCharAt(pairs.length() - 1);
        builder.withParam("pair", pairs.toString(), true, true);
        builder.withResource("public");
        builder.withResource("Ticker");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }
}
