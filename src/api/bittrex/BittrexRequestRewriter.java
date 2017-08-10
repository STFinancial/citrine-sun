package api.bittrex;

import api.RequestArgs;
import api.request.MarketRequest;
import api.request.TickerRequest;
import api.request.TradeRequest;
import api.tmp_trade.TradeType;

/**
 * Rewrites a {@link MarketRequest} into a {@link RequestArgs} usable by {@link Bittrex} to access the API of the website.
 */
final class BittrexRequestRewriter {
    private static final String API_ENDPOINT = "https://bittrex.com/api/v1.1";
    private final Bittrex bittrex;

    BittrexRequestRewriter(Bittrex bittrex) {
        this.bittrex = bittrex;
    }

    RequestArgs rewriteRequest(MarketRequest request) {
        if (request instanceof TradeRequest) {
            return rewriteTradeRequest((TradeRequest) request);
        } else if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        }
        return RequestArgs.unsupported();
    }

    private RequestArgs rewriteTradeRequest(TradeRequest request) {
        if (request.isMargin() || request.isPostOnly() || request.isMarket() || request.getTimeInForce() != TradeRequest.TimeInForce.GOOD_TIL_CANCELLED) {
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("market");
        builder.withResource(request.getType() == TradeType.BUY ? "buylimit" : "selllimit");
        builder.withParam("market", BittrexUtils.formatCurrencyPair(request.getPair()));
        builder.withParam("quantity", String.valueOf(request.getAmount()));
        builder.withParam("rate", String.valueOf(request.getRate()));
        builder.withParam("apikey", bittrex.getApiKey());
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
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
