package api;

import api.request.MarketRequest;
import api.request.MarketResponse;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Timothy on 8/10/17.
 */
public interface ResponseParser {
    // TODO(stfinancial): Potentially make a default implementation of this, it's a little awkward to have all the rest of the methods have a default implementation though.
    MarketResponse constructMarketResponse(JsonNode json, MarketRequest request, long timestamp);
}
