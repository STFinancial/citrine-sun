package api.request.tmp_loan;

import api.Currency;
import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import api.tmp_loan.ActiveLoan;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Returns the sets of {@link ActiveLoan}, both as the lender and the borrower.
 */
public class GetActiveLoansResponse extends MarketResponse {
    private final Map<Currency, List<ActiveLoan>> provided;
    private final Map<Currency, List<ActiveLoan>> used;

    public GetActiveLoansResponse(Map<Currency, List<ActiveLoan>> provided, Map<Currency, List<ActiveLoan>> used, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.provided = Collections.unmodifiableMap(provided);
        this.used = Collections.unmodifiableMap(used);
    }

    /** @return {@code Map} of {@code Currency} to the list of {@code ActiveLoan} for which we are a lender */
    public Map<Currency, List<ActiveLoan>> getProvided() { return provided; }
    /** @return {@code Map} of {@code Currency} to the list of {@code ActiveLoan} for which we are a borrower */
    public Map<Currency, List<ActiveLoan>> getUsed() { return used; }
}
