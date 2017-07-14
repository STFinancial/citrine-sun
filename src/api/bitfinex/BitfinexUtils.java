package api.bitfinex;

import api.Currency;
import api.CurrencyPair;

/**
 * Useful helpful methods for {@link Bitfinex}.
 */
final class BitfinexUtils {

    static CurrencyPair parseCurrencyPair(String pair) {
        // TODO(stfinancial): Ignore the 'f' for now.
        String base = pair.substring(1, 4);
        String quote = pair.substring(4);
        return CurrencyPair.of(Currency.getCanonicalRepresentation(base), Currency.getCanonicalRepresentation(quote));
    }

    static String formatCurrencyPair(CurrencyPair pair) {
        // TODO(stfinancial): How do we handle the f vs. t used by finex?
        // TODO(stfinancial): Ignoring 'f' for now.
        return "t" + getCurrencyString(pair.getBase()) + getCurrencyString(pair.getQuote());
    }

    static String getCurrencyString(Currency c) {
        // TODO(stfinancial): There is probably a better way to do it than this.
        switch (c) {
            case DASH:
                return "DSH";
            case IOTA:
                return "IOT";
            default:
                return c.toString();
        }
    }

}
