package api.request;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Returns {@link api.tmp_trade.CompletedTrade CompletedTrades} associated with a given order id.
 */
public final class OrderTradeResponse extends MarketResponse {
    public OrderTradeResponse(JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
    }
}
