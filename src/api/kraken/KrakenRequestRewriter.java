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

    // TODO(stfinancial): Prices can be preceded by +, -, or # to signify the price as a relative amount (with the exception of trailing stops, which are always relative). + adds the amount to the current offered price. - subtracts the amount from the current offered price. # will either add or subtract the amount to the current offered price, depending on the type and order type used. Relative prices can be suffixed with a % to signify the relative amount as a percentage of the offered price.
    private static RequestArgs rewriteTradeRequest(TradeRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        // TODO(stfinancial): The shitton of flags there are.
        // TODO(stfinancial): NOTE**** WE ARE ASSUMING ONLY LIMIT ORDERS FOR NOW.
        if (request.isMarket() || request.isStopLimit() || request.isPostOnly() || request.isMargin()) {
            return RequestArgs.unsupported();
        }
        builder.withParam("pair", KrakenUtils.formatCurrencyPair(request.getPair()), true, true);
        builder.withParam("type", KrakenUtils.getCommandForTradeType(request.getType()), true, true);
        builder.withParam("price", String.valueOf(request.getRate()), true, true);
        builder.withParam("volume", String.valueOf(request.getAmount()), true, true);
        builder.withResource("0");
        builder.withResource("private");
        builder.withResource("AddOrder");
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }
}
