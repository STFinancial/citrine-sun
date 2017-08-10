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
    private final Map<CurrencyPair, FeeInfo> feeInfo;

    public FeeResponse(Map<CurrencyPair, FeeInfo> feeInfo, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus status) {
        super(jsonResponse, request, timestamp, status);
        this.feeInfo = Collections.unmodifiableMap(feeInfo);
    }

//    // TODO(stfinancial): Need a better way to do this. Another option is forcing
//    /** ONLY USE IF THIS FEE APPLIES TO ENTIRE MARKET */
//    public FeeResponse(FeeInfo marketWideFeeInfo, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus status) {
//
//    }
//
//    // TODO(stfinancial): Come up with a better way.

    public Map<CurrencyPair, FeeInfo> getFeeInfo() {
        return feeInfo;
    }

    public FeeInfo getFeeInfo(CurrencyPair pair) {
        return feeInfo.get(pair);
    }
}
