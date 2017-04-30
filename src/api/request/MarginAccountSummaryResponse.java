package api.request;

import api.MarginAccountSummary;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Timothy on 3/3/17.
 */
public class MarginAccountSummaryResponse extends MarketResponse {
    private final MarginAccountSummary summary;

    public MarginAccountSummaryResponse(MarginAccountSummary summary, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus
            error) {
        super(jsonResponse, request, timestamp, error);
        this.summary = summary;
    }

    public MarginAccountSummary getSummary() {
        return summary;
    }
}
