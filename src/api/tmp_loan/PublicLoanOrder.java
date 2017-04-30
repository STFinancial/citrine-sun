package api.tmp_loan;

import api.Currency;

import java.math.BigDecimal;

/**
 * Created by Timothy on 1/20/17.
 */
public class PublicLoanOrder {
    private final Currency currency;
    private final double rate;
    private final double amount;

    // Currently in days.
    private final int rangeMin;
    private final int rangeMax;

    public PublicLoanOrder(Currency currency, double rate, double amount, int rangeMin, int rangeMax) {
        this.currency = currency;
        this.rate = rate;
        this.amount = amount;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
    }

    public Currency getCurrency() {
        return currency;
    }

    public double getRate() {
        return rate;
    }

    public double getAmount() {
        return amount;
    }

    public int getRangeMin() {
        return rangeMin;
    }

    public int getRangeMax() {
        return rangeMax;
    }
}
