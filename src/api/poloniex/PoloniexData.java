package api.poloniex;

import api.CurrencyPair;
import com.sun.istack.internal.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Timothy on 8/11/17.
 */
final class PoloniexData {
    // TODO(stfinancial): Maybe allow direct access to this for speed.
    private final Map<CurrencyPair, Integer> assetIds;
    private final Map<Integer, CurrencyPair> idAssets;

    PoloniexData(Map<CurrencyPair, Integer> assetIds, Map<Integer, CurrencyPair> idAssets) {
        this.assetIds = Collections.unmodifiableMap(assetIds);
        this.idAssets = Collections.unmodifiableMap(idAssets);
    }

    int getIdForAsset(CurrencyPair pair) {
        return assetIds.getOrDefault(pair, -1);
    }

    @Nullable
    CurrencyPair getAssetForId(int id) {
        return idAssets.getOrDefault(id, null);
    }
}
