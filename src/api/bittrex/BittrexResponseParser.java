package api.bittrex;

import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import api.request.StatusType;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Timothy on 8/3/17.
 */
final class BittrexResponseParser {

    MarketResponse constructMarketResponse(JsonNode jsonResponse, MarketRequest request, long timestamp) {
        System.out.println(jsonResponse);

        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }
}
