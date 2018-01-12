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
        } else if (request instanceof AssetPairRequest) {
            return rewriteAssetPairRequest((AssetPairRequest) request);
        }
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
