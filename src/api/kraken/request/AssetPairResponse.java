package api.kraken.request;

import api.CurrencyPair;
import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 7/8/17.
 */
// TODO(stfinancial): Rename to CurrencyPairResponse?
public class AssetPairResponse extends MarketResponse {
    final List<CurrencyPair> assetPairs;
    final Map<String, CurrencyPair> assetPairNames;
    final Map<CurrencyPair, String> assetPairKeys;

    // TODO(stfinancial): Should this constructor exist?
//    public AssetPairResponse(List<CurrencyPair> assetPairs, Map<String, CurrencyPair> assetPairNames, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
//        super(jsonResponse, request, timestamp, error);
//        this.assetPairs = Collections.unmodifiableList(assetPairs);
//        this.assetPairNames = Collections.unmodifiableMap(assetPairNames);
//        this.assetPairKeys = Collections.emptyMap();
//    }

    public AssetPairResponse(List<CurrencyPair> assetPairs, Map<String, CurrencyPair> assetPairNames, Map<CurrencyPair, String> assetPairKeys, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.assetPairs = Collections.unmodifiableList(assetPairs);
        this.assetPairNames = Collections.unmodifiableMap(assetPairNames);
        this.assetPairKeys = Collections.unmodifiableMap(assetPairKeys);
    }
    // TODO(stfinancial): Add fee tier information from the json response.

    public List<CurrencyPair> getAssetPairs() {
        return assetPairs;
    }

    public Map<String, CurrencyPair> getAssetPairNames() {
        return assetPairNames;
    }

    public Map<CurrencyPair, String> getAssetPairKeys() { return assetPairKeys; }
}
