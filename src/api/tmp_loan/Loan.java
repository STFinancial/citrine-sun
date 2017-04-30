package api.tmp_loan;

import api.Currency;

import java.math.BigDecimal;

/**
 * Represents the "kernel" of a loan. Any type of loan, whether completed, active, on order, will have this.
 */
public final class Loan {
    // TODO(stfinancial): Does every loan have a duration?

    // TODO(stfinancial): Another constructor/static factory method for double

    // TODO(stfinancial): Consider if these actually should be final.
    private final double amount;
    private final double rate; // TODO(stfinancial): Will this continue to be rate per day, or normalized somehow?
    private final Currency currency;
    private final LoanType type;

    public Loan(double amount, double rate, Currency currency, LoanType type) {
        this.amount = amount;
        this.rate = rate;
        this.currency = currency;
        this.type = type;
    }

    /** @return The quantity of the quoted {@link api.Currency}. */
    public double getAmount() { return amount; }
    /** @return The rate, currently per day, of the {@code Loan}. */
    public double getRate() { return rate; }
    /** @return The {@code Currency} in which this {@code Loan} is set. */
    public Currency getCurrency() { return currency; }
    /** @return The type of loan, whether an OFFER of the {@link Currency} at the given amount and rate, or a DEMAND. */
    public LoanType getType() { return type; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("\tamount: " + amount).append("\n");
        sb.append("\trate: " + rate).append("\n");
        sb.append("\ttype: " + type.toString()).append("\n");
        sb.append("\tcurrency: " + currency.toString()).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Loan) {
            Loan l = (Loan) o;
            // TODO(stfinancial): Decide which check should be first. Will we often be comparing loans of the same currency?
            return amount == l.amount && rate == l.rate && currency == l.currency && type == l.type;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Double.valueOf(amount).hashCode();
        hash = 31 * hash + Double.valueOf(rate).hashCode();
        hash = 31 * hash + currency.hashCode();
        hash = 31 * hash + type.ordinal();
        return hash;
    }

}