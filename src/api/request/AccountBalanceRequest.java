package api.request;

import api.AccountType;

/**
 * Fetch balances for each {@link api.Currency Currency}.
 */
public class AccountBalanceRequest extends MarketRequest {
    // TODO(stfinancial): Do we want to bother with this when we can just fetch all information, see returnCompleteBalances

    private final AccountType type;

    public AccountBalanceRequest() { this.type = null; }
    public AccountBalanceRequest(AccountType type) {
        this.type = type;
    }

    public AccountType getType() {
        return type;
    }
}
