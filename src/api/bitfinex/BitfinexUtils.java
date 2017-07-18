package api.bitfinex;

import api.AccountType;
import api.Currency;
import api.CurrencyPair;
import com.sun.istack.internal.Nullable;

/**
 * Useful helpful methods for {@link Bitfinex}.
 */
final class BitfinexUtils {
    private static final String EXCHANGE_STRING = "exchange";
    private static final String MARGIN_STRING = "margin";
    private static final String LENDING_STRING = "funding";

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

    @Nullable
    static AccountType parseAccountType(String type) {
        if (type.equals(EXCHANGE_STRING)) {
            return AccountType.EXCHANGE;
        } else if (type.equals(MARGIN_STRING)) {
            return AccountType.MARGIN;
        } else if (type.equals(LENDING_STRING)) {
            return AccountType.LOAN;
        } else {
            return null;
        }
    }

}
