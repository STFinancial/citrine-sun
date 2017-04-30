package api.request;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Zarathustra on 3/24/2017.
 */
public class TransferBalanceResponse extends MarketResponse {
    public TransferBalanceResponse(JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
    }
}
