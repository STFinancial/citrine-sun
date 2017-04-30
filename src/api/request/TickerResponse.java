package api.request;

import api.CurrencyPair;
import api.Ticker;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Created by Timothy on 2/12/17.
 */
public class TickerResponse extends MarketResponse {
    private final Map<CurrencyPair, Ticker> tickers;

    public TickerResponse(Map<CurrencyPair, Ticker> tickers, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.tickers = tickers;
    }

    public Map<CurrencyPair, Ticker> getTickers() {
        return tickers;
    }
}
