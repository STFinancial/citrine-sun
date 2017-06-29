package api.gdax;

import api.AccountType;
import api.CurrencyPair;
import api.RequestArgs;
import api.request.*;

/**
 * Converts a {@link MarketRequest} into a {@link api.RequestArgs} which can be used to construct an {@link org.apache.http.HttpRequest}.
 */
final class GdaxRequestRewriter {
    private static final String API_ENDPOINT = "https://api.gdax.com";

    static RequestArgs rewriteRequest(MarketRequest request) {
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
        return RequestArgs.unsupported();
    }

    private static RequestArgs rewriteTickerRequest(TickerRequest request) {
        // TODO(stfinancial): Does gdax really not support getting all the tickers?
        if (request.getPairs().size() != 1) {
            // TODO(stfinancial): Probably need to throw something here.
            System.out.println("Gdax does not support multiple ticker pair requests.");
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("products");
        builder.withResource(GdaxUtils.formatCurrencyPair(request.getPairs().get(0)));
        builder.withResource("ticker");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private static RequestArgs rewriteCancelRequest(CancelRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("orders");
        builder.withResource(request.getId());
        builder.httpRequestType(RequestArgs.HttpRequestType.DELETE);
        builder.isPrivate(true);
        return builder.build();
    }

    private static RequestArgs rewriteOrderBookRequest(OrderBookRequest request) {
        if (!request.getCurrencyPair().isPresent()) {
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("products");
        // TODO(stfinancial): Also awful.
        builder.withResource(GdaxUtils.formatCurrencyPair(request.getCurrencyPair().get()));
        builder.withResource("book");
        if (request.getDepth() == 1) {
            builder.withParam("level", "1", true, true);
        } else if (request.getDepth() <= 50) {
            builder.withParam("level", "2", true, true);
        } else {
            // TODO(stfinancial): Need to inform client somehow that these are non aggregated (every order listed, even at same price)
            builder.withParam("level", "3", true, true);
        }
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private static RequestArgs rewriteTradeRequest(TradeRequest request) {
        // TODO(stfinancial): Self trade prevention behavior.
        if (request.isMargin() || request.isStopLimit() || request.isMarket()) {
            // TODO(stfinancial): Implement these, along with fill or kill, post only, immediate or cancel.
            return RequestArgs.unsupported();
        }
        // TODO(stfinancial): This is currently only valid for limit orders.
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("orders");
        // TODO(stfinancial): Add support for this.
//        builder.withParam("client_oid")
//        builder.withParam("type", "limit");
        builder.withParam("price", String.valueOf(request.getRate()));
        builder.withParam("size", String.valueOf(request.getAmount()));
        builder.withParam("side", GdaxUtils.getCommandForTradeType(request.getType()));
        builder.withParam("product_id", GdaxUtils.formatCurrencyPair(request.getPair()));
        builder.withParam("post_only", request.isPostOnly() ? "true" : "false", false, false);
        switch (request.getTimeInForce()) {
            // TODO(stfinancial): Move this into the utils class?
            case GOOD_TIL_CANCELLED:
                builder.withParam("time_in_force", "GTC");
                break;
            case IMMEDIATE_OR_CANCEL:
                builder.withParam("time_in_force", "IOC");
                break;
            case FILL_OR_KILL:
                builder.withParam("time_in_force", "FOK");
                break;
            default:
                System.out.println("Unsupported TimeInForce on GDAX: " + request.getTimeInForce());
                return RequestArgs.unsupported();
        }
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }

    private static RequestArgs rewriteOrderTradesRequest(OrderTradesRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("fills");
        // TODO(stfinancial): Support for product_id
        builder.withParam("order_id", request.getId());
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(true);
        return builder.build();
    }

    private static RequestArgs rewriteAccountBalanceRequest(AccountBalanceRequest request) {
        if (request.getType() == AccountType.LOAN) {
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("accounts");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(true);
        return builder.build();
    }

    private static RequestArgs rewriteFeeRequest(FeeRequest request) {
        // TODO(stfinancial): Apparently this is a cached value that is calculated every night at midnight.
        // TODO(stfinancial): Does this mean that if we go over the next fee tier, it will have to wait?
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("users");
        builder.withResource("self");
        builder.withResource("trailing-volume");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(true);
        return builder.build();
    }

}
