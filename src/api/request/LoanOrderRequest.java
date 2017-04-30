package api.request;

import api.tmp_loan.PrivateLoanOrder;

/**
 * Created by Timothy on 1/3/17.
 */
public final class LoanOrderRequest extends MarketRequest {
    // Currently calling it loanOrder although poloniex uses offer, I believe loanOrder is more consistent.

    private final PrivateLoanOrder order;
    private final boolean isAutoRenew = false;

    protected LoanOrderRequest(PrivateLoanOrder order, int priority, long timestamp) {
        super(priority, timestamp);
        this.order = order;
    }


}
