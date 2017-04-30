package api.request.tmp_loan;

import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Timothy on 1/28/17.
 */
public class GetActiveLoansResponse extends MarketResponse {
    public GetActiveLoansResponse(JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
    }
}
