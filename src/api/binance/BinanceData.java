package api.binance;

import api.AssetPair;
import api.Currency;
import api.CurrencyPair;
import com.sun.istack.internal.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 12/27/17.
 */
final class BinanceData {
    private final Map<String, AssetPair> assetPairs;
    private final Map<CurrencyPair, String> currencyPairNames;

    // TODO(stfinancial): Data shouldn't be doing work and have methods, utils should be doing this, we might need to change it away from a static class
    BinanceData(List<AssetPair> assetPairs) {
        Map<String, AssetPair> ap = new HashMap<>();
        Map<CurrencyPair, String> cpn = new HashMap<>();
        assetPairs.forEach((a) -> {
            ap.put(a.getName(), a);
            cpn.put(a.getPair(), a.getName());
        });
        this.assetPairs = Collections.unmodifiableMap(ap);
        this.currencyPairNames = Collections.unmodifiableMap(cpn);
    }

    @Nullable
    AssetPair getAssetPair(String name) {
        return assetPairs.get(name);
    }

    @Nullable
    String getAssetPairName(CurrencyPair pair) {
        return currencyPairNames.get(pair);
    }

//    private final assetPairNames
}
