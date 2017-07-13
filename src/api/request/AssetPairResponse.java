package api.request;

import api.CurrencyPair;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 7/8/17.
 */
// TODO(stfinancial): Rename to CurrencyPairResponse?
public class AssetPairResponse extends MarketResponse {
    // TODO(stfinancial): Explain what these actually are in javadoc
    final List<CurrencyPair> assetPairs; // List of tradable currency pairs
    final Map<String, CurrencyPair> assetPairNames; // Map of the string used to identify a currency pair in the API, to the pair itself
    final Map<CurrencyPair, String> assetPairKeys; // Map of the currencypair to the string used to identify it in the API.

    public AssetPairResponse(List<CurrencyPair> assetPairs, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.assetPairs = assetPairs;
        this.assetPairNames = Collections.emptyMap();
        this.assetPairKeys = Collections.emptyMap();
    }

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
