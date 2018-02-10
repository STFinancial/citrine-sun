package api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A {@code CurrencyPair} has a base and quote {@link Currency}. The price of an asset for a given currency pair
 * is always represented in terms of its quote currency. That is, the number of units of the quote currency to make a
 * single unit of the base currency.
 */
public final class CurrencyPair {
    private static final CurrencyPair[][] pairs;
    private static List<CurrencyPair> pairList;

    static {
        // Techincally it can be [len][len - 1] since we don't need CP(BTC, BTC) e.g. Kind of a hassle to access though.
        pairs = new CurrencyPair[Currency.values().length][Currency.values().length];
        for (Currency primary : Currency.values()) {
            for (Currency secondary : Currency.values()) {
                if (primary != secondary) {
                    pairs[primary.ordinal()][secondary.ordinal()] = new CurrencyPair(primary, secondary);
                }
            }
        }
    }

    private final Currency base;
    private final Currency quote;

    private CurrencyPair(Currency base, Currency quote) {
        this.base = base;
        this.quote = quote;
    }

    /**
     * Returns a {@code CurrencyPair} of its constituent {@link Currency Currencies}.
     *
     * @param base The base currency of this pair. Prices are relative to this currency.
     * @param quote The quote currency of this pair. Prices are measured in how much of the quote currency is needed to
     *              make a single unit of the base currency.
     */
    public static CurrencyPair of(Currency base, Currency quote) {
        // TODO(stfinancial): There may be a function that maps these ordinal values to something we can use as a map key.
        return pairs[base.ordinal()][quote.ordinal()];
    }

    public Currency getBase() { return base; }
    public Currency getQuote() { return quote; }

    public static List<CurrencyPair> getCurrencyPairSet() {
        // TODO(stfinancial): Other option is to let markets obtain these themselves by looking it up, e.g. returnCurrencies
        if (pairList == null) {
            ArrayList<CurrencyPair> l = new ArrayList<>();
            for (CurrencyPair[] c : pairs) {
                l.addAll(Arrays.asList(c));
            }
            pairList = Collections.unmodifiableList(l);
        }
        return pairList;
    }

    public boolean contains(Currency currency) { return base == currency || quote == currency; }

    @Override
    public String toString() {
        return base.toString() + "-" + quote.toString();
    }

    @Override
    public int hashCode() {
        // TODO(stfinancial): How does this function actually work? Will this cause collisions?
        // TODO(stfinancial): REALLY NEED TO GRAPH THIS FUNCTION TO SEE IF IT COLLIDES.
        int hash = 17;
        hash = 31 * hash + base.ordinal();
        hash = 31 * hash + quote.ordinal();
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CurrencyPair) {
            CurrencyPair c = (CurrencyPair) o;
            // The secondary currency is more likely to differ.
            return c.quote == quote && c.base == base;
        }
        return false;
    }
}
