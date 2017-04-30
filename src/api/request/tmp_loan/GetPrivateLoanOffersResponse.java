package api.request.tmp_loan;

import api.Currency;
import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import api.tmp_loan.PrivateLoanOrder;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 1/28/17.
 */
public class GetPrivateLoanOffersResponse extends MarketResponse {
    private final Map<Currency, List<PrivateLoanOrder>> offers;

    public GetPrivateLoanOffersResponse(Map<Currency, List<PrivateLoanOrder>> offers, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.offers = offers;
    }

    public Map<Currency, List<PrivateLoanOrder>> getOffers() {
        return offers;
    }
}
