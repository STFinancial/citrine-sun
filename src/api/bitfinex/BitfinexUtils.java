package api.bitfinex;

import api.Currency;
import api.CurrencyPair;

/**
 * Created by Timothy on 4/13/17.
 */
final class BitfinexUtils {

    static String formatCurrencyPair(CurrencyPair pair) {
        // TODO(stfinancial): How do we handle the f vs. t used by finex?
        return getCurrencyString(pair.getBase()) + getCurrencyString(pair.getQuote());
    }

    static String getCurrencyString(Currency c) {
        // TODO(stfinancial): There is probably a better way to do it than this.
        switch (c) {
            default:
                return c.toString();
        }
    }

}
