package api.krakentest;

import api.Currency;
import api.CurrencyPair;
import api.tmp_trade.TradeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static api.Currency.BTC;
import static api.Currency.DASH;

/**
 * Created by Timothy on 6/3/17.
 */
final class KrakenUtilsTest {
    private static final String BUY_STRING = "buy";
    private static final String SELL_STRING = "sell";
    private static final String INVALID_STRING = "invalid";

    // TODO(stfinancial): There has to be a better way, perhaps
    private static final Map<String, CurrencyPair> pairs = Collections.unmodifiableMap(new HashMap<String, CurrencyPair>(){{
        put("DASHXBT", CurrencyPair.of(DASH, BTC));
    }});

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

    static CurrencyPair parseCurrencyPair(String pair) {
        // TODO(stfinancial): THIS IS NOT RIGHT.

        // TODO(stfinancial): Convert this to optional?
//        System.out.println(pair);
        String[] currencies = pair.split("-", 2);
        if (currencies.length != 2) {
            return null;
        }
        // TODO(stfinancial): Make sure that this is right.
        return CurrencyPair.of(Currency.getCanonicalRepresentation(currencies[0]), Currency.getCanonicalRepresentation(currencies[1]));
    }


    //  TODO(stfinancial): Update javadoc with ISO compliance stuff.
    /**
     * Converts a canonical {@code CurrencyPair} into a {@code String} representation for use by the Kraken API.
     * @param pair {@code CurrencyPair} to convert to {@code String} format.
     * @return Kraken's {@code String} representation of a {@code CurrencyPair}. Returns the Kraken-specific
     * representation of the {@code CurrencyPair}'s base and quote {@link Currency}, respectively, with a hyphen
     * between the two (i.e. XBT and XMR -> "BTCXMR").
     */
    static String formatCurrencyPair(CurrencyPair pair) {
        if (pair.getBase().getIsoNamespace().equals(pair.getQuote().getIsoNamespace())) {
            return String.format("%s%s", getCurrencyString(pair.getBase()), getCurrencyString(pair.getQuote()));
        } else {
            return String.format("%s%s", getIsoCompliantCurrencyString(pair.getBase()), getIsoCompliantCurrencyString(pair.getQuote()));
        }
    }

    // TODO(stfinancial): Could make this an abstract method for Market.
    static String getCurrencyString(Currency currency) {
        // TODO(stfinancial): There has to be a more elegant solution than a giant switch statement. Maybe an enummap or something.
        switch (currency) {
            case BTC:
                return "XBT";
            default:
                return currency.toString();
        }
    }

    // TODO(stfinancial): These should be created at compile time if possible.
    static String getIsoCompliantCurrencyString(Currency currency) {
        return currency.getIsoNamespace() + getCurrencyString(currency);
    }
}
