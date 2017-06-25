package api.kraken;

import api.CurrencyPair;
import api.RequestArgs;
import api.request.*;

/**
 * Created by Timothy on 3/7/17.
 */
final class KrakenRequestRewriter {
    private static final String API_ENDPOINT = "https://api.kraken.com";
    // TODO(stfinancial): Should we have public and private endpoint? What are the implications for the api calls.

    static RequestArgs rewriteRequest(MarketRequest request) {
        if (request instanceof OrderBookRequest) {
            return rewriteOrderBookRequest((OrderBookRequest) request);
        } else if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        } else if (request instanceof AccountBalanceRequest) {
            return rewriteAccountBalanceRequest((AccountBalanceRequest) request);
        }
        return RequestArgs.unsupported();
    }

    private static RequestArgs rewriteTickerRequest(TickerRequest request) {
        if (!request.getPairs().isPresent()) {
            // TODO(stfinancial): Probably need to throw something here.
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        StringBuilder pairs = new StringBuilder();
        for (CurrencyPair pair : request.getPairs().get()) {
            pairs.append(KrakenUtils.formatCurrencyPair(pair)).append(",");
        }
        // TODO(stfinancial): Gross, find another way.
        pairs.deleteCharAt(pairs.length() - 1);
        builder.withParam("pair", pairs.toString(), true, true);
        builder.withResource("0");
        builder.withResource("public");
        builder.withResource("Ticker");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private static RequestArgs rewriteOrderBookRequest(OrderBookRequest request) {
        if (!request.getCurrencyPair().isPresent()) {
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withParam("pair", KrakenUtils.formatCurrencyPair(request.getCurrencyPair().get()), true, true);
        builder.withParam("count", String.valueOf(request.getDepth()), true, true);
        builder.withResource("0");
        builder.withResource("public");
        builder.withResource("Depth");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private static RequestArgs rewriteAccountBalanceRequest(AccountBalanceRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        // TODO(stfinancial): Check account types.
        builder.withResource("0");
        builder.withResource("private");
        builder.withResource("Balance");
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }
}
