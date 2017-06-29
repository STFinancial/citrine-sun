package api.kraken;

import api.CurrencyPair;
import api.Ticker;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import com.fasterxml.jackson.databind.JsonNode;
import util.PriceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 6/3/17.
 */
final class KrakenResponseParser {

    // TODO(stfinancial): Take in isError for now until we switch to using the http response.
    static MarketResponse constructMarketResponse(JsonNode jsonResponse, MarketRequest request, long timestamp, boolean isError) {
        // TODO(stfinancial): Check "error" field to see if the result is an empty array.
        if (jsonResponse.isNull()) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE));
        }
        // TODO(stfinancial): Get the request status here.

        if (isError) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, jsonResponse.asText()));
        }
        if (request instanceof OrderBookRequest) {
            return createOrderBookResponse(jsonResponse, (OrderBookRequest) request, timestamp);
        } else if (request instanceof TickerRequest) {
            return createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
        }
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private static TickerResponse createTickerResponse(JsonNode jsonResponse, TickerRequest request, long timestamp) {
        System.out.println(jsonResponse);
        // TODO(stfinancial): We check that there is a currency pair in the request, does it make sense to be defensive and check here as well?
        Map<CurrencyPair, Ticker> tickers = new HashMap<>();
        request.getPairs().forEach((pair) -> {
            JsonNode j = jsonResponse.get("result").get(KrakenUtils.formatCurrencyPair(pair));
            Ticker.Builder b = new Ticker.Builder(pair, j.get("c").get(0).asDouble(), j.get("a").get(0).asDouble(), j.get("b").get(0).asDouble());
            b.percentChange(PriceUtil.getPercentChange(j.get("o").asDouble(), j.get("c").get(0).asDouble()));
            b.baseVolume(j.get("v").get(1).asDouble());
            // TODO(stfinancial): I think that this is right...
            b.quoteVolume(j.get("v").get(1).asDouble() * j.get("p").get(1).asDouble());
            tickers.put(pair, b.build());
        });
        return new TickerResponse(tickers, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static OrderBookResponse createOrderBookResponse(JsonNode jsonResponse, OrderBookRequest request, long timestamp) {
        System.out.println(jsonResponse);
        CurrencyPair pair = request.getCurrencyPair().get();
        // TODO(stfinancial): We check that there is a currency pair in the request, does it make sense to be defensive and check here as well?
        JsonNode j = jsonResponse.get("result").get(KrakenUtils.formatCurrencyPair(pair));
        Map<CurrencyPair, List<Trade>> askMap = new HashMap<>();
        List<Trade> asks = new ArrayList<>();
        j.get("asks").elements().forEachRemaining((order) -> {
            asks.add(new Trade(order.get(1).asDouble(), order.get(0).asDouble(), pair, TradeType.SELL));
        });
        askMap.put(request.getCurrencyPair().get(), asks);
        Map<CurrencyPair, List<Trade>> bidMap = new HashMap<>();
        List<Trade> bids = new ArrayList<>();
        j.get("bids").elements().forEachRemaining((order) -> {
            bids.add(new Trade(order.get(1).asDouble(), order.get(0).asDouble(), pair, TradeType.BUY));
        });
        bidMap.put(pair, bids);
        return new OrderBookResponse(askMap, bidMap, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createAccountBalanceResponse(JsonNode jsonResponse, AccountBalanceRequest request, long timestamp) {
        System.out.println(jsonResponse);
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private static MarketResponse createTradeResponse(JsonNode jsonResponse, TradeRequest request, long timestamp) {
        System.out.println(jsonResponse);
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }
}
