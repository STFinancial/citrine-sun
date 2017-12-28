package api.binance;

import api.RequestArgs;
import api.RequestRewriter;
import api.request.*;

/**
 * Created by Timothy on 12/23/17.
 */
final class BinanceRequestRewriter implements RequestRewriter {
    private static final String API_ENDPOINT = "https://api.binance.com";

    @Override
    public RequestArgs rewriteRequest(MarketRequest request) {
        if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        }
//        if (request instanceof TradeRequest) {
//            return rewriteTradeRequest((TradeRequest) request);
//        }
//        else if (request instanceof CancelRequest) {
//            return rewriteCancelRequest((CancelRequest) request);
//        } else if (request instanceof OrderBookRequest) {
//            return rewriteOrderBookRequest((OrderBookRequest) request);
//        } else if (request instanceof TickerRequest) {
//            return rewriteTickerRequest((TickerRequest) request);
//        } else if (request instanceof OrderTradesRequest) {
//            return rewriteOrderTradesRequest((OrderTradesRequest) request);
//        } else if (request instanceof AccountBalanceRequest) {
//            return rewriteAccountBalanceRequest((AccountBalanceRequest) request);
//        } else if (request instanceof TradeHistoryRequest) {
//            return rewriteTradeHistoryRequest((TradeHistoryRequest) request);
//        } else if (request instanceof FeeRequest) {
//            return rewriteFeeRequest((FeeRequest) request);
//        } else if (request instanceof AssetPairRequest) {
//            return rewriteAssetPairRequest((AssetPairRequest) request);
//        }
        return RequestArgs.unsupported();
    }

    private RequestArgs rewriteTickerRequest(TickerRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(API_ENDPOINT);
        builder.withResource("api");
        builder.withResource("v1");
        builder.withResource("ticker");
        builder.withResource("24hr");
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }
}
