package api.request;

import api.MarginPosition;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Timothy on 3/3/17.
 */
public class MarginPositionResponse extends MarketResponse {
    private final MarginPosition position;

    public MarginPositionResponse(MarginPosition position, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.position = position;
    }

    public MarginPosition getPosition() {
        return position;
    }
}
