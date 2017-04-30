package api.request.tmp_loan;

import api.request.MarketRequest;

/**
 * Created by Timothy on 1/28/17.
 */
public class GetActiveLoansRequest extends MarketRequest {
    // TODO(stfinancial): Potentially take a currency here.
    protected GetActiveLoansRequest(int priority, long timestamp) {
        super(priority, timestamp);
    }
}
