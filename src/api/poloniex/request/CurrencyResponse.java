package api.poloniex.request;

import api.Currency;
import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.istack.internal.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * {@code MarketResponse} containing mappings from {@link Currency} to an integer denoting its asset id, and vice versa.
 */
public final class CurrencyResponse extends MarketResponse {
    private final Map<Currency, Integer> currencyLabels;
    private final Map<Integer, Currency> labelMap;

    public CurrencyResponse(Map<Currency, Integer> currencyLabels, Map<Integer, Currency> labelMap, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.currencyLabels = Collections.unmodifiableMap(currencyLabels);
        this.labelMap = Collections.unmodifiableMap(labelMap);
    }

    public Map<Integer, Currency> getLabelMap() { return labelMap; }
}
