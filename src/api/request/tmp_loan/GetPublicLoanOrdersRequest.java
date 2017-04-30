package api.request.tmp_loan;

import api.Currency;
import api.request.MarketRequest;

/**
 * Created by Timothy on 1/28/17.
 */
public class GetPublicLoanOrdersRequest extends MarketRequest {
    // TODO(stfinancial): Optional number to control number of orders.
    private final Currency currency;

    public GetPublicLoanOrdersRequest(Currency currency, int priority, long timestamp) {
        super(priority, timestamp);
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }
}
