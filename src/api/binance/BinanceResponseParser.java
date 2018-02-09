package api.binance;

import api.*;
import api.request.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 12/23/17.
 */
final class BinanceResponseParser implements ResponseParser {
    // TODO(stfinancial): We need to figure out a better way to handle getting the necessary data.
    private final Binance binance;

    BinanceResponseParser(Binance binance) {
        this.binance = binance;
    }

    @Override
    public MarketResponse constructMarketResponse(JsonNode json, MarketRequest request, long timestamp) {
        if (json.isNull()) {
            return new MarketResponse(json, request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE));
        }
        if (request instanceof TradeRequest) {
            return createTradeResponse(json, (TradeRequest) request, timestamp);
        } else if (request instanceof TickerRequest) {
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

    private MarketResponse createTradeResponse(JsonNode json, TradeRequest request, long timestamp) {

//        return new TradeResponse()
        return new MarketResponse(json, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private MarketResponse createTickerResponse(JsonNode json, TickerRequest request, long timestamp) {
        Map<CurrencyPair, Ticker> tickers = new HashMap<>();
        if (request.getPairs().isEmpty()) {
            json.elements().forEachRemaining((t) -> {
                CurrencyPair pair = binance.getData().getAssetPair(t.get("symbol").asText()).getPair();
                Ticker.Builder b = new Ticker.Builder(pair, t.get("lastPrice").asDouble(), t.get("askPrice").asDouble(), t.get("bidPrice").asDouble());
                b.baseVolume(t.get("volume").asDouble());
                b.quoteVolume(t.get("quoteVolume").asDouble());
                b.high24hr(t.get("highPrice").asDouble());
                b.low24hr(t.get("lowPrice").asDouble());
                tickers.put(pair, b.build());
            });
        } else {
            // TODO(stfinancial): Do we assume the input has already been validated by the request parser?
            AssetPair pair = binance.getData().getAssetPair(json.get("symbol").asText());
            if (pair == null) {
                return new MarketResponse(json, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "Market does not support the requested Asset Pair"));
            }
            Ticker.Builder b = new Ticker.Builder(pair.getPair(), json.get("lastPrice").asDouble(), json.get("askPrice").asDouble(), json.get("bidPrice").asDouble());
            b.baseVolume(json.get("volume").asDouble()).quoteVolume(json.get("quoteVolume").asDouble());
            b.high24hr(json.get("highPrice").asDouble());
            b.low24hr(json.get("lowPrice").asDouble());
            tickers.put(pair.getPair(), b.build());
        }
        return new TickerResponse(tickers, json, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createAssetPairResponse(JsonNode json, AssetPairRequest request, long timestamp) {
        List<AssetPair> assetPairs = new ArrayList<>();
        json.get("symbols").elements().forEachRemaining((s) -> {
            if ("123456".equals(s.get("symbol").asText())) { return; } // Binance has a junk/practice market called 123456
            CurrencyPair pair = CurrencyPair.of(Currency.getCanonicalName(s.get("baseAsset").asText()), Currency.getCanonicalName(s.get("quoteAsset").asText()));
            AssetPair.Builder ap = new AssetPair.Builder(pair, s.get("symbol").asText());
            s.get("filters").elements().forEachRemaining((filter) -> {
                if (filter.get("filterType").asText().equals("LOT_SIZE")) {
                    ap.baseMinSize(filter.get("minQty").asDouble(0.0));
                    ap.baseMaxSize(filter.get("maxQty").asDouble(0.0));
                }
            });
            assetPairs.add(ap.build());
        });
        return new AssetPairResponse(assetPairs, json, request, timestamp, RequestStatus.success());
    }
}
