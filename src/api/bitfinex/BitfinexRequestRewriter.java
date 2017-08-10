package api.bitfinex;

import api.RequestArgs;
import api.request.*;

/**
 * Converts a {@link MarketRequest} into a {@link api.RequestArgs} specific to {@link Bitfinex} which can be used to construct an {@link org.apache.http.HttpRequest} and access the API of the website.
 */
final class BitfinexRequestRewriter {
    private static final String API_ENDPOINT = "https://api.bitfinex.com";

    RequestArgs rewriteRequest(MarketRequest request) {
        if (request instanceof TradeRequest) {
            return rewriteTradeRequest((TradeRequest) request);
        } else if (request instanceof OrderBookRequest) {
            return rewriteOrderBookRequest((OrderBookRequest) request);
        } else if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        } else if (request instanceof AccountBalanceRequest) {
            return rewriteAccountBalanceRequest((AccountBalanceRequest) request);
        }
        return RequestArgs.unsupported();
    }

    private RequestArgs rewriteTradeRequest(TradeRequest request) {
        return RequestArgs.unsupported();
    }

    private RequestArgs rewriteOrderBookRequest(OrderBookRequest request) {
        if (request.getCurrencyPair() == null) {
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("v2");
        builder.withResource("book");
        builder.withResource(BitfinexUtils.formatCurrencyPair(request.getCurrencyPair()));
        builder.withResource("P0");
        builder.withParam("len", String.valueOf(request.getDepth()));
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private RequestArgs rewriteTickerRequest(TickerRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("v2");
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

    private RequestArgs rewriteAccountBalanceRequest(AccountBalanceRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("v2");
        builder.withResource("auth");
        builder.withResource("r");
        builder.withResource("wallets");
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }
}
