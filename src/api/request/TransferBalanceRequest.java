package api.request;

import api.AccountType;
import api.Currency;

/**
 * Transfer a {@link Currency} from one account to the other.
 */
public final class TransferBalanceRequest extends MarketRequest {
    private final Currency currency;
    private final double amount;
    private final AccountType from;
    private final AccountType to;


    public TransferBalanceRequest(Currency currency, double amount, AccountType from, AccountType to) {
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
    public AccountType from() {
        return from;
    }
    public AccountType to() {
        return to;
    }
}
