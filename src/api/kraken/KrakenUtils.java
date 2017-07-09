package api.kraken;

import api.Currency;
import api.CurrencyPair;
import api.tmp_trade.TradeType;

/**
 * Created by Timothy on 6/3/17.
 */
final class KrakenUtils {
    private static final String BUY_STRING = "buy";
    private static final String SELL_STRING = "sell";
    private static final String INVALID_STRING = "invalid";

    static String getCommandForTradeType(TradeType type) {
        switch (type) {
            case BUY:
                return BUY_STRING;
            case SELL:
                return SELL_STRING;
            default:
                return INVALID_STRING;
        }
    }

    // TODO(stfinancial): Merge these formatting methods into something that doesn't suck. Make sure all the API calls work properly.
    //  TODO(stfinancial): Update javadoc with ISO compliance stuff.
    /**
     * Converts a canonical {@code CurrencyPair} into a {@code String} representation for use by the Kraken API.
     * @param pair {@code CurrencyPair} to convert to {@code String} format.
     * @param ignoreNamespace Whether to use the {@link Currency} namespace if the namespace differs.
     * @return Kraken's {@code String} representation of a {@code CurrencyPair}. Returns the Kraken-specific
     * representation of the {@code CurrencyPair}'s base and quote {@code Currency}, respectively, with a hyphen
     * between the two (i.e. XBT and XMR -> "BTCXMR").
     */
    static String formatCurrencyPair(CurrencyPair pair, boolean ignoreNamespace) {
        if (ignoreNamespace || pair.getBase().getIsoNamespace().equals(pair.getQuote().getIsoNamespace())) {
            return String.format("%s%s", getCurrencyString(pair.getBase()), getCurrencyString(pair.getQuote()));
        } else {
            return String.format("%s%s", getIsoCompliantCurrencyString(pair.getBase()), getIsoCompliantCurrencyString(pair.getQuote()));
        }
    }

//    static String formatCurrencyPairWithoutNamespace(CurrencyPair pair) {
//        return String.format("%s%s", getCurrencyString(pair.getBase()), getCurrencyString(pair.getQuote()));
//    }

    // TODO(stfinancial): Could make this an abstract method for Market.
    static String getCurrencyString(Currency currency) {
        // TODO(stfinancial): There has to be a more elegant solution than a giant switch statement. Maybe an enummap or something.
        switch (currency) {
            case BTC:
                return "XBT";
            case DOGE:
                return "XDG";
            case STR:
                return "XLM";
            default:
                return currency.toString();
        }
    }

    static Currency parseCurrencyString(String currency) {
        return Currency.getCanonicalRepresentation(currency.substring(1));
    }

    // TODO(stfinancial): These should be created at compile time if possible.
    static String getIsoCompliantCurrencyString(Currency currency) {
        return currency.getIsoNamespace() + getCurrencyString(currency);
    }
}
