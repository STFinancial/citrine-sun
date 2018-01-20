package api.binance;

import api.*;
import api.request.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Timothy on 12/23/17.
 */
final class BinanceResponseParser implements ResponseParser {
    @Override
    public MarketResponse constructMarketResponse(JsonNode json, MarketRequest request, long timestamp) {
        if (json.isNull()) {
            return new MarketResponse(json, request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE));
        }
        if (request instanceof TickerRequest) {
            return createTickerResponse(json, (TickerRequest) request, timestamp);
        } else if (request instanceof AssetPairRequest) {
            return createAssetPairResponse(json, (AssetPairRequest) request, timestamp);
        }
//        // TODO(stfinancial): Get the request status here.
//        if (request instanceof TradeRequest) {
//            return createTradeResponse(jsonResponse, (TradeRequest) request, timestamp);
//        } else if (request instanceof OrderBookRequest) {
//            return createOrderBookResponse(jsonResponse, (OrderBookRequest) request, timestamp);
//        } else if (request instanceof CancelRequest) {
//            return createCancelResponse(jsonResponse, (CancelRequest) request, timestamp);
//        } else if (request instanceof TickerRequest) {
//            return createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
//        } else if (request instanceof OrderTradesRequest) {
//            return createOrderTradesResponse(jsonResponse, (OrderTradesRequest) request, timestamp);
//        } else if (request instanceof AccountBalanceRequest) {
//            return createAccountBalanceResponse(jsonResponse, (AccountBalanceRequest) request, timestamp);
//        } else if (request instanceof TradeHistoryRequest) {
//            return createTradeHistoryResponse(jsonResponse, (TradeHistoryRequest) request, timestamp);
//        } else if (request instanceof FeeRequest) {
//            return createFeeResponse(jsonResponse, (FeeRequest) request, timestamp);
//        } else if (request instanceof AssetPairRequest) {
//            return createAssetPairResponse(jsonResponse, (AssetPairRequest) request, timestamp);
//        }
        return new MarketResponse(json, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private MarketResponse createTickerResponse(JsonNode json, TickerRequest request, long timestamp) {
        if (request.getPairs().isEmpty()) {
            json.elements().forEachRemaining((t) -> {
                Ticker.Builder b = new Ticker.Builder()
            });
        } else {

        }
        System.out.println(json.toString());
        return new MarketResponse(json, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private MarketResponse createAssetPairResponse(JsonNode json, AssetPairRequest request, long timestamp) {
        List<AssetPair> assetPairs = new ArrayList<>();
        json.get("symbols").elements().forEachRemaining((s) -> {
            CurrencyPair pair = CurrencyPair.of(Currency.getCanonicalName(s.get("baseAsset").asText()), Currency.getCanonicalName(s.get("quoteAsset").asText()));
            AssetPair.Builder ap = new AssetPair.Builder(pair, s.get("symbol").asText());
            assetPairs.add(ap.build());
        });
        return new AssetPairResponse(assetPairs, json, request, timestamp, RequestStatus.success());
    }
}
