package api.request;

import api.CurrencyPair;
import api.FeeInfo;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.Map;

/**
 * An instance of {@code MarketResponse} returning the {@link FeeInfo} for a given {@link api.CurrencyPair} or an entire {@link api.Market}.
 */
public final class FeeResponse extends MarketResponse {
    private final FeeInfo feeInfo;

    public FeeResponse(FeeInfo feeInfo, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus status) {
        super(jsonResponse, request, timestamp, status);
        this.feeInfo = feeInfo;
    }

    public FeeInfo getFeeInfo() {
        return feeInfo;
    }

//    private final Map<CurrencyPair, FeeInfo> feeInfo;
//
//    public FeeResponse(Map<CurrencyPair, FeeInfo> feeInfo, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
//        super(jsonResponse, request, timestamp, error);
//        this.feeInfo = feeInfo;
//    }

//    public Map<CurrencyPair, FeeInfo> getFeeInfo() {
//        // TODO(stfinancial): All these unmodifiable map things should probably be done at creation time.
//        return Collections.unmodifiableMap(feeInfo);
//    }
}
