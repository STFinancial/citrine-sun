package api.gdax;

import api.AccountType;
import api.request.*;

/**
 * Created by Timothy on 3/7/17.
 */
final class GdaxRequestRewriter {

    static RestArgs rewriteRequest(MarketRequest request) {
        if (request instanceof TradeRequest) {
            return rewriteTradeRequest((TradeRequest) request);
        } else if (request instanceof CancelRequest) {
            return rewriteCancelRequest((CancelRequest) request);
        } else if (request instanceof OrderBookRequest) {
            return rewriteOrderBookRequest((OrderBookRequest) request);
        } else if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        } else if (request instanceof OrderTradesRequest) {
            return rewriteOrderTradesRequest((OrderTradesRequest) request);
        } else if (request instanceof AccountBalanceRequest) {
            return rewriteAccountBalanceRequest((AccountBalanceRequest) request);
        } else if (request instanceof FeeRequest) {
            return rewriteFeeRequest((FeeRequest) request);
        }
        return RestArgs.unsupported();
    }

    private static RestArgs rewriteTickerRequest(TickerRequest request) {
        if (!request.getPairs().isPresent() || request.getPairs().get().size() != 1) {
            // TODO(stfinancial): Probably need to throw something here.
            return RestArgs.unsupported();
        }
        RestArgs.Builder builder = new RestArgs.Builder();
        builder.withResource("products");
        // TODO(stfinancial): Awful...
        builder.withResource(GdaxUtils.formatCurrencyPair(request.getPairs().get().get(0)));
        builder.withResource("ticker");
        builder.isPublic(true);
        builder.httpRequestType(RestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private static RestArgs rewriteCancelRequest(CancelRequest request) {
        RestArgs.Builder builder = new RestArgs.Builder();
        builder.withResource("orders");
        builder.withResource(request.getId());
        builder.isPublic(false);
        builder.httpRequestType(RestArgs.HttpRequestType.DELETE);
        return builder.build();
    }

    private static RestArgs rewriteOrderBookRequest(OrderBookRequest request) {
        if (!request.getCurrencyPair().isPresent()) {
            return RestArgs.unsupported();
        }
        RestArgs.Builder builder = new RestArgs.Builder();
        builder.withResource("products");
        // TODO(stfinancial): Also awful.
        builder.withResource(GdaxUtils.formatCurrencyPair(request.getCurrencyPair().get()));
        builder.withResource("book");
        if (request.getDepth() == 1) {
            builder.withParam("level", "1");
        } else if (request.getDepth() <= 50) {
            builder.withParam("level", "2");
        } else {
            // TODO(stfinancial): Need to inform client somehow that these are non aggregated (every order listed, even at same price)
            builder.withParam("level", "3");
        }
        builder.isPublic(true);
        builder.httpRequestType(RestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private static RestArgs rewriteTradeRequest(TradeRequest request) {
        if (request.isMargin() || request.isStopLimit() || request.isMarket()) {
            // TODO(stfinancial): Implement these, along with fill or kill, post only, immediate or cancel.
            return RestArgs.unsupported();
        }
        // TODO(stfinancial): This is currently only valid for limit orders.
        RestArgs.Builder builder = new RestArgs.Builder();
        builder.withResource("orders");
        // TODO(stfinancial): Add support for this.
//        builder.withParam("client_oid")
//        builder.withParam("type", "limit");
        builder.withParam("price", String.valueOf(request.getRate()));
        builder.withParam("size", String.valueOf(request.getAmount()));
        builder.withParam("side", GdaxUtils.getCommandForTradeType(request.getType()));
        builder.withParam("product_id", GdaxUtils.formatCurrencyPair(request.getPair()));
        builder.withParam("post_only", request.isPostOnly() ? "true" : "false");
        builder.isPublic(false);
        builder.httpRequestType(RestArgs.HttpRequestType.POST);
        return builder.build();
    }

    private static RestArgs rewriteOrderTradesRequest(OrderTradesRequest request) {
        RestArgs.Builder builder = new RestArgs.Builder();
        builder.withResource("fills");
        // TODO(stfinancial): Support for product_id
        builder.withParam("order_id", request.getId());
        builder.isPublic(false);
        builder.httpRequestType(RestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private static RestArgs rewriteAccountBalanceRequest(AccountBalanceRequest request) {
        if (request.getType() == AccountType.LOAN) {
            return RestArgs.unsupported();
        }
        RestArgs.Builder builder = new RestArgs.Builder();
        builder.withResource("accounts");
        builder.isPublic(false);
        builder.httpRequestType(RestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private static RestArgs rewriteFeeRequest(FeeRequest request) {
        // TODO(stfinancial): Apparently this is a cached value that is calculated every night at midnight.
        // TODO(stfinancial): Does this mean that if we go over the next fee tier, it will have to wait?
        if (!request.getCurrencyPair().isPresent()) {
            // TODO(stfinancial): This is actually supported... implement this.
            return RestArgs.unsupported();
        }
        RestArgs.Builder builder = new RestArgs.Builder();
        builder.withResource("users");
        builder.withResource("self");
        builder.withResource("trailing-volume");
        builder.httpRequestType(RestArgs.HttpRequestType.GET);
        builder.isPublic(false);
        return builder.build();
    }

}
