package api.poloniex;

import api.Currency;
import api.MarketConstants;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static api.Currency.*;

/**
 * Since there are an enormous amount of constants for a given exchange, to avoid pollution of the poloniex class we
 * will put them here for now.
 */
// TODO(stfinancial): This should not be public but we need access somehow.
public final class PoloniexConstants extends MarketConstants {
    private static final PoloniexConstants INSTANCE = new PoloniexConstants();

    // TODO(stfinancial): Consider refactoring these into separate constants to allow shorter names.
    /* Lending */
    private static final double MIN_LENDING_AMOUNT = 0.001d;
    private static final double LENDING_RATE_INCREMENT = 0.00000001d;
    private static final double MIN_LENDING_RATE = 0.000001d;
    private static final double MAX_LENDING_RATE = 0.05d;
    private static final int MIN_LENDING_DURATION = 2;
    private static final int MAX_LENDING_DURATION = 60;

    // TODO(stfinancial): Don't we need to update this every so often?
    private static final List<Currency> LENDABLE_CURRENCIES = Collections.unmodifiableList(Arrays.asList(BTC, BTS, CLAM, DASH, DOGE, ETH, FCT, LTC, MAID, STR, XMR, XRP));
    // TODO(stfinancial): Do we want separate instances for public and private constants?

    private PoloniexConstants() {}

    public static PoloniexConstants getInstance() { return INSTANCE; }

    @Override
    public boolean canLend() {
        return true;
    }

    public List<Currency> getLendableCurrencies() {
        return LENDABLE_CURRENCIES;
    }

    public double getMinLendingAmount() {
        return MIN_LENDING_AMOUNT;
    }

    public double getLendingRateIncrement() {
        return LENDING_RATE_INCREMENT;
    }

    public double getMinLendingRate() {
        return MIN_LENDING_RATE;
    }

    public double getMaxLendingRate() {
        return MAX_LENDING_RATE;
    }

    public int getMinLendingDuration() {
        return MIN_LENDING_DURATION;
    }

    public int getMaxLendingDuration() {
        return MAX_LENDING_DURATION;
    }



    @Override
    public boolean canMarginTrade() {
        return true;
    }

    @Override
    public boolean canTrade() {
        return true;
    }
}
