package api.request.tmp_loan;

import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Timothy on 1/28/17.
 */
public class CreateLoanOfferResponse extends MarketResponse {
    private final long orderNumber;

    public CreateLoanOfferResponse(long orderNumber, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.orderNumber = orderNumber;
    }

    public long getOrderNumber() {
        return orderNumber;
    }
}
