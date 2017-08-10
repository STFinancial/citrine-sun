package api.bittrex;

import api.CurrencyPair;
import api.Ticker;
import api.request.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Timothy on 8/3/17.
 */
final class BittrexResponseParser {

    MarketResponse constructMarketResponse(JsonNode jsonResponse, MarketRequest request, long timestamp) {
        if (!jsonResponse.get("success").asBoolean()) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, jsonResponse.get("message").asText("")));
        }
        System.out.println(jsonResponse);
        if (request instanceof TradeRequest) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
        } else if (request instanceof TickerRequest) {
            createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
        }

        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private MarketResponse createTickerResponse(JsonNode jsonResponse, TickerRequest request, long timestamp) {
        if (request.getPairs().size() != 1) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "Cannot request ticker for multiple markets."));
        }
        Ticker t = new Ticker.Builder(request.getPairs().get(0), jsonResponse.get("Last").asDouble(), jsonResponse.get("Ask").asDouble(), jsonResponse.get("Bid").asDouble()).build();
        return new TickerResponse(new HashMap<CurrencyPair, Ticker>(){{ put(request.getPairs().get(0), t);}}, jsonResponse, request, timestamp, RequestStatus.success());
    }

}
