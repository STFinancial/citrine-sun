package api.request;

import api.tmp_loan.Loan;

/**
 * A request to place a {@link Loan} for a given {@link api.Market Market}.
 */
public class LoanRequest extends MarketRequest {
    // TODO(stfinancial): How can we properly handle tmp_loan duration here?
    private Loan loan;

    private boolean isAutoRenew = false;
    // TODO(stfinancial): Need to make sure this is handled properly in the case of a tmp_loan demand.
    private int loanDuration = 2; // TODO(stfinancial): What is a reasonable default here.

    protected LoanRequest(Loan loan, int priority, long timestamp) {
        super(priority, timestamp);
        this.loan = loan;
    }




}
