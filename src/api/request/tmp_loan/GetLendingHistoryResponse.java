package api.request.tmp_loan;

import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import api.tmp_loan.CompletedLoan;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;

/**
 * Created by Timothy on 1/28/17.
 */
public final class GetLendingHistoryResponse extends MarketResponse {
    private final List<CompletedLoan> loans;

    public GetLendingHistoryResponse(List<CompletedLoan> loans, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.loans = loans;
    }

    public List<CompletedLoan> getLoans() {
        return Collections.unmodifiableList(loans);
    }
}
