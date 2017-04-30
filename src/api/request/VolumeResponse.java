package api.request;

import api.Currency;
import api.CurrencyPair;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Timothy on 3/25/17.
 */
public class VolumeResponse extends MarketResponse {
    private final Map<CurrencyPair, Double> baseVolumes;
    private final Map<CurrencyPair, Double> quoteVolumes;
    private final Map<Currency, Double> currencyVolumes;

    // TODO(stfinancial): Get support for "primary currencies" later.

    public VolumeResponse(Map<CurrencyPair, Double> baseVolumes, Map<CurrencyPair, Double> quoteVolumes, Map<Currency, Double> currencyVolumes, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.baseVolumes = baseVolumes;
        this.quoteVolumes = quoteVolumes;
        this.currencyVolumes = currencyVolumes;
    }

    public Map<CurrencyPair, Double> getBaseVolumes() {
        return Collections.unmodifiableMap(baseVolumes);
    }

    public Map<CurrencyPair, Double> getQuoteVolumes() {
        return Collections.unmodifiableMap(quoteVolumes);
    }

    public Map<Currency, Double> getCurrencyVolumes() {
        return Collections.unmodifiableMap(currencyVolumes);
    }
}
