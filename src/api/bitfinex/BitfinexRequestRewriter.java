package api.bitfinex;

import api.RequestArgs;
import api.request.MarketRequest;
import api.request.TickerRequest;
import api.request.TradeRequest;

/**
 * Created by Timothy on 4/13/17.
 */
final class BitfinexRequestRewriter {
    private static final String PUBLIC_ENDPOINT = "https://api.bitfinex.com/v2";

    RequestArgs rewriteRequest(MarketRequest request) {
        if (request instanceof TradeRequest) {
            return rewriteTradeRequest((TradeRequest) request);
        } else if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        }
        return RequestArgs.unsupported();
    }

    private RequestArgs rewriteTickerRequest(TickerRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PUBLIC_ENDPOINT);
        builder.withResource("tickers");
        // TODO(stfinancial): Maybe return all tickers if it is empty?
        StringBuilder pairs = new StringBuilder();
        request.getPairs().forEach((pair) -> pairs.append(BitfinexUtils.formatCurrencyPair(pair)).append(","));
        // TODO(stfinancial): Gross, find another way.
        pairs.deleteCharAt(pairs.length() - 1);
        builder.withParam("symbols", pairs.toString());
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private RequestArgs rewriteTradeRequest(TradeRequest request) {
        return RequestArgs.unsupported();
    }

}
