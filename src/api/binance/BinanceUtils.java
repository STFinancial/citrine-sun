package api.binance;

import api.Currency;
import api.CurrencyPair;
import com.sun.istack.internal.Nullable;

/**
 * Created by Timothy on 12/23/17.
 */
final class BinanceUtils {

    @Nullable
    static CurrencyPair parseCurrencyPair(String pair) {

        // TODO(stfinancial): Convert this to optional.
        String[] currencies = pair.split("_", 2);
        if (currencies.length != 2) {
            return null;
        }
        // Poloniex API does not conform to the CurrencyPair representation standard.
        System.out.println(pair);
        return CurrencyPair.of(Currency.getCanonicalName(currencies[1]), Currency.getCanonicalName(currencies[0]));
    }

//    @Nullable
//    static CurrencyPair parseCurrencyPair(String pair) {
//
//    }
}
