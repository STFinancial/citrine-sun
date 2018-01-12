package api.kraken;

import api.AssetPair;
import api.CurrencyPair;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 7/8/17.
 */
final class KrakenData {
    private final List<AssetPair> assetPairs;
    private final Map<String, AssetPair> assetPairNames;
    private final Map<CurrencyPair, AssetPair> assetPairKeys;

    // Since there is no consistency in how they convert a currency pair to string, we need to maintain this mapping.
    // TODO(stfinancial): How do we handle dark order books in this case? Right now we just ignore them.

    KrakenData(List<AssetPair> assetPairs) {
        this.assetPairs = Collections.unmodifiableList(assetPairs);
        Map<String, AssetPair> apn = new HashMap<>();
        Map<CurrencyPair, AssetPair> apk = new HashMap<>();
        assetPairs.forEach((ap) -> {
            apn.put(ap.getName(), ap);
            apk.put(ap.getPair(), ap);
        });
        this.assetPairNames = Collections.unmodifiableMap(apn);
        this.assetPairKeys = Collections.unmodifiableMap(apk);
    }

    List<AssetPair> getAssetPairs() {
        return assetPairs;
    }

    Map<String, AssetPair> getAssetPairNames() {
        return assetPairNames;
    }

    Map<CurrencyPair, AssetPair> getAssetPairKeys() {
        return assetPairKeys;
    }
}
