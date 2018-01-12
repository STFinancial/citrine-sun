package api.poloniex;

import api.AssetPair;
import api.CurrencyPair;
import com.sun.istack.internal.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 8/11/17.
 */
final class PoloniexData {
    // TODO(stfinancial): Maybe allow direct access to this for speed.
    // TODO(stfinancial): Not happy with the naming of these, nor of the objects in the constructor.
    private final Map<CurrencyPair, AssetPair> assets;
    private final Map<Integer, AssetPair> ids;

    PoloniexData(List<AssetPair> assetPairs) {
        Map<CurrencyPair, AssetPair> a = new HashMap<>();
        Map<Integer, AssetPair> i = new HashMap<>();
        assetPairs.forEach((ap) -> {
            a.put(ap.getPair(), ap);
            i.put(ap.getId(), ap);
        });
        this.assets = Collections.unmodifiableMap(a);
        this.ids = Collections.unmodifiableMap(i);
    }
    // TODO(stfinancial): Maybe just get the asset pairs instead and let them deal with it?

    int getIdForAsset(CurrencyPair pair) {
        return assets.containsKey(pair) ? assets.get(pair).getId() : -1;
    }

    @Nullable
    CurrencyPair getAssetForId(int id) {
        return ids.containsKey(id) ? ids.get(id).getPair() : null;
    }
}
