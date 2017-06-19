package api.kraken;

import api.request.*;
import com.fasterxml.jackson.databind.JsonNode;

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
        if (request instanceof TickerRequest) {
            return createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
        }
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private static MarketResponse createTickerResponse(JsonNode jsonResponse, TickerRequest request, long timestamp) {
        System.out.println(jsonResponse);
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }
}
