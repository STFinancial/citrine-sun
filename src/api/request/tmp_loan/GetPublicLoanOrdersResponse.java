package api.request.tmp_loan;

import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import api.tmp_loan.PublicLoanOrder;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Created by Timothy on 1/28/17.
 */
public class GetPublicLoanOrdersResponse extends MarketResponse {
    // TODO(stfinancial): Does it make more sense to have offers and demands, or just have a map?
    private final List<PublicLoanOrder> offers;
    private final List<PublicLoanOrder> demands;

    public GetPublicLoanOrdersResponse(List<PublicLoanOrder> offers, List<PublicLoanOrder> demands, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus
            error) {
        super(jsonResponse, request, timestamp, error);
        this.offers = offers;
        this.demands = demands;
    }

    public List<PublicLoanOrder> getOffers() {
        return offers;
    }

    public List<PublicLoanOrder> getDemands() {
        return demands;
    }
}
