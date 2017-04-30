package api.request.tmp_loan;

import api.request.MarketRequest;
import api.tmp_loan.PrivateLoanOrder;

/**
 * Created by Timothy on 1/28/17.
 */
public class CreateLoanOfferRequest extends MarketRequest {
    private final PrivateLoanOrder order;

    public CreateLoanOfferRequest(PrivateLoanOrder order, int priority, long timestamp) {
        super(priority, timestamp);
        this.order = order;
    }

    public PrivateLoanOrder getOrder() {
        return order;
    }
}
