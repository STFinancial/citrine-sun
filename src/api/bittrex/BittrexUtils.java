package api.bittrex;

import api.Currency;
import api.CurrencyPair;

/**
 * Created by Timothy on 5/31/17.
 */
final class BittrexUtils {

    static String formatCurrencyPair(CurrencyPair pair) {
        return getCurrencyString(pair.getBase()) + "-" + getCurrencyString(pair.getQuote());
    }

    static String getCurrencyString(Currency c) {
        switch (c) {
            default:
                return c.toString();
        }
    }
}
