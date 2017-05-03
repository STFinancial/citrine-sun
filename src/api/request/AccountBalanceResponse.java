package api.request;

import api.AccountType;
import api.Currency;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Timothy on 1/30/17.
 */
public class AccountBalanceResponse extends MarketResponse {
    // TODO(stfinancial): Abstraction for this?
    // TODO(stfinancial): Potentially convert to 3 maps.
    // TODO(stfinancial): Still need to fill in 0 values.
    // TODO(stfinancial): What about available vs. total?
    private final Map<AccountType, Map<Currency, Double>> balances;

    public AccountBalanceResponse(Map<AccountType, Map<Currency, Double>> balances, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.balances = balances;
    }

    public Map<AccountType, Map<Currency, Double>> getBalances() {
        return Collections.unmodifiableMap(balances);
    }

    // TODO(pallarino): Maybe allow specifying account type or account type/currency as getters.
}
