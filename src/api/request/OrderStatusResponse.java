package api.request;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Timothy on 5/5/17.
 */
public final class OrderStatusResponse extends MarketResponse {
    // TODO(stfinancial): Implement.

    public OrderStatusResponse(JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
    }
}
