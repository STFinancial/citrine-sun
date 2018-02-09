package api.binance;

import api.RequestArgs;
import api.RequestRewriter;
import api.request.*;

/**
 * Created by Timothy on 12/23/17.
 */
final class BinanceRequestRewriter implements RequestRewriter {
    private static final String API_ENDPOINT = "https://api.binance.com";

    private final Binance binance;

    BinanceRequestRewriter(Binance binance) {
        this.binance = binance;
    }

    @Override
    public RequestArgs rewriteRequest(MarketRequest request) {
        if (request instanceof TradeRequest) {
            return rewriteTradeRequest((TradeRequest) request);
        } else if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        } else if (request instanceof AssetPairRequest) {
            return rewriteAssetPairRequest((AssetPairRequest) request);
        }
        return RequestArgs.unsupported();
    }

    private RequestArgs rewriteTradeRequest(TradeRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        if (request.isMargin() || request.isPostOnly() || request.isMarket() || request.getTimeInForce() != TradeRequest.TimeInForce.GOOD_TIL_CANCELLED || request.isStopLimit()) {
            return RequestArgs.unsupported();
        }
        builder.withResource("api");
        builder.withResource("v3");
        builder.withResource("order");
        builder.withParam("symbol", BinanceUtils.formatCurrencyPair(request.getPair()));
        builder.withParam("side", BinanceUtils.getCommandForTradeType(request.getType()));
        // TODO(stfinancial): Support other types later.
        builder.withParam("type", "LIMIT");
        // TODO(stfinancial): Support other timeInForces later
        builder.withParam("timeInForce", "GTC");
        builder.withParam("quantity", String.valueOf(request.getAmount()));
        builder.withParam("price", String.valueOf(request.getRate()));
        builder.withParam("timestamp", String.valueOf(System.currentTimeMillis()));
        builder.withParam("recvWindow", "5000");
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.isPrivate(true);
        return builder.build();
    }

    private RequestArgs rewriteTickerRequest(TickerRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("api");
        builder.withResource("v1");
        builder.withResource("ticker");
        builder.withResource("24hr");
        if (request.getPairs().size() == 1) {
            builder.withParam("symbol", BinanceUtils.formatCurrencyPair(request.getPairs().get(0)));
        } else {
            System.out.println("Cannot ask for more than a single ticker");
            return RequestArgs.unsupported();
        }
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private RequestArgs rewriteAssetPairRequest(AssetPairRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("api");
        builder.withResource("v1");
        builder.withResource("exchangeInfo");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }
}
