package api.request;

import api.AccountType;
import api.Currency;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.Map;

/**
 * {@code MarketResponse} containing the available account balances for a {@link api.Market Market}.
 */
public class AccountBalanceResponse extends MarketResponse {
    // TODO(stfinancial): Abstraction for this?
    // TODO(stfinancial): Potentially convert to 3 maps.
    // TODO(stfinancial): Still need to fill in 0 values.
    // TODO(stfinancial): What about available vs. total?
    // TODO(stfinancial): Currently for bitfinex this is available balance.
    private final Map<AccountType, Map<Currency, Double>> balances;

    public AccountBalanceResponse(Map<AccountType, Map<Currency, Double>> balances, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.balances = Collections.unmodifiableMap(balances);
    }

    // TODO(stfinancial): Should they even have access to this directly?
    public Map<AccountType, Map<Currency, Double>> getBalances() {
        return balances;
    }

    public Double getBalance(AccountType type, Currency c) {
        return balances.getOrDefault(type, Collections.emptyMap()).getOrDefault(c, 0.0);
    }
}
