package api.request;

import api.AccountType;
import api.Currency;

/**
 * Created by Zarathustra on 3/24/2017.
 */
public final class TransferBalanceRequest extends MarketRequest {
    // TODO(stfinancial): Check that this is not a poloniex specific thing.

    private final Currency currency;
    private final double amount;
    private final AccountType from;
    private final AccountType to;


    public TransferBalanceRequest(Currency currency, double amount, AccountType from, AccountType to, int priority, long timestamp) {
        super(priority, timestamp);
        this.currency = currency;
        this.amount = amount;
        this.from = from;
        this.to = to;
    }

    public Currency getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    // TODO(stfinancial): Does this naming make sense? Or is getFrom and getTo better ... ?
    public AccountType from() {
        return from;
    }

    public AccountType to() {
        return to;
    }
}
