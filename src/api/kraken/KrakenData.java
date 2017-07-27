package api.kraken;

import api.CurrencyPair;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 7/8/17.
 */
final class KrakenData {
    private final List<CurrencyPair> assetPairs;
    private final Map<String, CurrencyPair> assetPairNames;

    // Since there is no consistency in how they convert a currency pair to string, we need to maintain this mapping.
    // TODO(stfinancial): How do we handle dark order books in this case? Right now we just ignore them.
    private final Map<CurrencyPair, String> assetPairKeys;

    KrakenData(List<CurrencyPair> assetPairs, Map<String, CurrencyPair> assetPairNames, Map<CurrencyPair, String> assetPairKeys) {
        this.assetPairs = Collections.unmodifiableList(assetPairs);
        this.assetPairNames = Collections.unmodifiableMap(assetPairNames);
        this.assetPairKeys = Collections.unmodifiableMap(assetPairKeys);
    }

    List<CurrencyPair> getAssetPairs() {
        return assetPairs;
    }

    Map<String, CurrencyPair> getAssetPairNames() {
        return assetPairNames;
    }

    Map<CurrencyPair, String> getAssetPairKeys() {
        return assetPairKeys;
    }
}
