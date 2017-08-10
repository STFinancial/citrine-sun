package api.bittrex;

import api.AccountType;
import api.RequestArgs;
import api.request.*;
import api.tmp_trade.TradeType;

/**
 * Converts a {@link MarketRequest} into a {@link api.RequestArgs} specific to {@link Bittrex} which can be used to construct an {@link org.apache.http.HttpRequest} and access the API of the website.
 */
final class BittrexRequestRewriter {
    private static final String API_ENDPOINT = "https://bittrex.com/api/v1.1";
    private final Bittrex bittrex;

    BittrexRequestRewriter(Bittrex bittrex) {
        this.bittrex = bittrex;
    }

    RequestArgs rewriteRequest(MarketRequest request) {
        // TODO(stfinancial): For Ticker request, can call getmarketsummaries if more than 1 ticker is requested.

        if (request instanceof TradeRequest) {
            return rewriteTradeRequest((TradeRequest) request);
        } else if (request instanceof CancelRequest) {
            return rewriteCancelRequest((CancelRequest) request);
        } else if (request instanceof OrderBookRequest) {
            return rewriteOrderBookRequest((OrderBookRequest) request);
        } else if (request instanceof OpenOrderRequest) {
            return rewriteOpenOrderRequest((OpenOrderRequest) request);
        } else if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        } else if (request instanceof AccountBalanceRequest) {
            return rewriteAccountBalanceRequest((AccountBalanceRequest) request);
        } else if (request instanceof FeeRequest) {
            // TODO(stfinancial): This is a constant 0.25% for every trade. There is actually no request we can send. How can we spoof this? Maybe a RequestArgs.noop()
            return RequestArgs.unsupported();
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

    private RequestArgs rewriteCancelRequest(CancelRequest request) {
        if (request.getType() != CancelRequest.CancelType.TRADE) {
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("market");
        builder.withResource("cancel");
        builder.withParam("uuid", request.getId());
        builder.withParam("apikey", bittrex.getApiKey());
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private RequestArgs rewriteOrderBookRequest(OrderBookRequest request) {
        if (request.getCurrencyPair() == null) {
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("public");
        builder.withResource("getorderbook");
        builder.withParam("market", BittrexUtils.formatCurrencyPair(request.getCurrencyPair()));
        builder.withParam("type", "both");
        builder.isPrivate(false);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private RequestArgs rewriteOpenOrderRequest(OpenOrderRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("market");
        builder.withResource("getopenorders");
        if (request.getCurrencyPair() != null) builder.withParam("market", BittrexUtils.formatCurrencyPair(request.getCurrencyPair()));
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

    private RequestArgs rewriteAccountBalanceRequest(AccountBalanceRequest request) {
        if (request.getType() == AccountType.LOAN || request.getType() == AccountType.MARGIN) {
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("account");
        builder.withResource("getbalances");
        builder.withParam("apikey", bittrex.getApiKey());
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
    }
}
