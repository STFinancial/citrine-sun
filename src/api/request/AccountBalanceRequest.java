package api.request;

import api.AccountType;

/**
 * Created by Timothy on 1/30/17.
 */
public class AccountBalanceRequest extends MarketRequest {
    // TODO(stfinancial): Do we want to bother with this when we can just fetch all information, see returnCompleteBalances




    private final AccountType type;
    // TODO(stfinancial): Allow optional account specifying.

    public AccountBalanceRequest(AccountType type, int priority, long timestamp) {
        super(priority, timestamp);
        this.type = type;
    }

    public AccountType getType() {
        return type;
    }
}
