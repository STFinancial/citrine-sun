package api.kraken;

import api.CurrencyPair;
import api.RequestArgs;
import api.request.AssetPairRequest;
import api.request.*;

/**
 * Created by Timothy on 3/7/17.
 */
final class KrakenRequestRewriter {
    private static final String API_ENDPOINT = "https://api.kraken.com";
    private final Kraken kraken;

    // TODO(stfinancial): Does it even make sense to do this?
    KrakenRequestRewriter(Kraken kraken) {
        this.kraken = kraken;
    }

    RequestArgs rewriteRequest(MarketRequest request) {
        if (request instanceof TradeRequest) {
            return rewriteTradeRequest((TradeRequest) request);
        } else if (request instanceof OrderBookRequest) {
            return rewriteOrderBookRequest((OrderBookRequest) request);
        } else if (request instanceof OpenOrderRequest) {
            return rewriteOpenOrderRequest((OpenOrderRequest) request);
        } else if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        } else if (request instanceof AccountBalanceRequest) {
            return rewriteAccountBalanceRequest((AccountBalanceRequest) request);
        } else if (request instanceof OrderTradesRequest) {
            return rewriteOrderTradesRequest((OrderTradesRequest) request);
        } else if (request instanceof CancelRequest) {
            return rewriteCancelRequest((CancelRequest) request);
        } else if (request instanceof TradeHistoryRequest) {
            return rewriteTradeHistoryRequest((TradeHistoryRequest) request);
        } else if (request instanceof FeeRequest) {
            // TODO(stfinancial): I don't think we ever programmed FeeResponse.
            return rewriteFeeRequest((FeeRequest) request);
        } else if (request instanceof AssetPairRequest) {
            return rewriteAssetPairRequest((AssetPairRequest) request);
        }
        return RequestArgs.unsupported();
    }

    private RequestArgs rewriteTickerRequest(TickerRequest request) {
        if (request.getPairs().isEmpty()) {
            System.out.println("Kraken requires that ticker pairs be specified.");
            // TODO(stfinancial): Probably need to throw something here.
            // TODO(stfinancial): Maybe return all tickers?
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        StringBuilder pairs = new StringBuilder();
        for (CurrencyPair pair : request.getPairs()) {
            pairs.append(KrakenUtils.formatCurrencyPair(pair, true)).append(",");
        }
        // TODO(stfinancial): Gross, find another way.
        pairs.deleteCharAt(pairs.length() - 1);
        builder.withParam("pair", pairs.toString());
        builder.withResource("0");
        builder.withResource("public");
        builder.withResource("Ticker");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private RequestArgs rewriteOrderBookRequest(OrderBookRequest request) {
        if (!request.getCurrencyPair().isPresent()) {
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withParam("pair", KrakenUtils.formatCurrencyPair(request.getCurrencyPair().get(), true));
        builder.withParam("count", String.valueOf(request.getDepth()));
        builder.withResource("0");
        builder.withResource("public");
        builder.withResource("Depth");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private RequestArgs rewriteOpenOrderRequest(OpenOrderRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        // TODO(stfinancial): trades and userref
        builder.withResource("0");
        builder.withResource("private");
        builder.withResource("OpenOrders");
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }

    private RequestArgs rewriteOrderTradesRequest(OrderTradesRequest request) {
        // TODO(stfinancial): trades and userref
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("0");
        builder.withResource("private");
        builder.withResource("QueryOrders");
        // TODO(stfinancial): Support for multiple requests.
        builder.withParam("txid", request.getId());
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }

    private RequestArgs rewriteCancelRequest(CancelRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("0");
        builder.withResource("private");
        builder.withResource("CancelOrder");
        builder.withParam("txid", request.getId());
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }

    private RequestArgs rewriteTradeHistoryRequest(TradeHistoryRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("0");
        builder.withResource("private");
        builder.withResource("TradesHistory");
        // TODO(stfinancial): Do we want to specify "all" or "closed" for type.
        // TODO(stfinancial): What does the "trades" option do?
        if (request.getEnd() != 0 || request.getStart() != 0) {
            builder.withParam("start", String.valueOf(request.getStart()));
            builder.withParam("end", String.valueOf(request.getEnd()));
        }
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }

    private RequestArgs rewriteAccountBalanceRequest(AccountBalanceRequest request) {
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
    private RequestArgs rewriteTradeRequest(TradeRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        // TODO(stfinancial): The shitton of flags there are.
        // TODO(stfinancial): NOTE**** WE ARE ASSUMING ONLY LIMIT ORDERS FOR NOW.
        if (request.isMarket() || request.isStopLimit() || request.isPostOnly() || request.isMargin()) {
            return RequestArgs.unsupported();
        }
        builder.withParam("pair", KrakenUtils.formatCurrencyPair(request.getPair(), true));
        builder.withParam("type", KrakenUtils.getCommandForTradeType(request.getType()));
        builder.withParam("price", String.valueOf(request.getRate()));
        builder.withParam("volume", String.valueOf(request.getAmount()));
        builder.withParam("ordertype", "limit");
        builder.withResource("0");
        builder.withResource("private");
        builder.withResource("AddOrder");
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }

    private RequestArgs rewriteFeeRequest(FeeRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        if (request.getPairs().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            kraken.getData().getAssetPairs().forEach(pair -> sb.append(kraken.getData().getAssetPairKeys().get(pair)).append(","));
            // TODO(stfinancial): Gross, find another way.
            sb.deleteCharAt(sb.length() - 1);
            builder.withParam("pair", sb.toString());
        } else {
            StringBuilder sb = new StringBuilder();
            request.getPairs().forEach(pair -> sb.append(kraken.getData().getAssetPairKeys().get(pair)).append(","));
            // TODO(stfinancial): Gross, find another way.
            sb.deleteCharAt(sb.length() - 1);
            builder.withParam("pair", sb.toString());
        }
        builder.withParam("fee-info", "1"); // TODO(stfinancial): Need to verify if this is how to do it. "1" or "true"
        builder.withResource("0");
        builder.withResource("private");
        builder.withResource("TradeVolume");
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }

    private RequestArgs rewriteAssetPairRequest(AssetPairRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("0");
        builder.withResource("public");
        builder.withResource("AssetPairs");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }
}
