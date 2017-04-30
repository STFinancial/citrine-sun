package api.tmp_loan;

/**
 * Refers to the orientation of a {@link Loan}. A loan of type OFFER means we are viewing the side providing the loan,
 * while a loan of type DEMAND means we are viewing the side consuming the loan.
 */
public enum LoanType {
    OFFER, DEMAND;
}
