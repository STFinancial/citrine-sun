package api.bittrex;

import api.Currency;
import api.CurrencyPair;

/**
 * Created by Timothy on 5/31/17.
 */
final class BittrexUtils {

    static String formatCurrencyPair(CurrencyPair pair) {
        // Bittrex does this backwards.
        return getCurrencyString(pair.getQuote()) + "-" + getCurrencyString(pair.getBase());
    }

    static String getCurrencyString(Currency c) {
        switch (c) {
            default:
                return c.toString();
        }
    }
}
