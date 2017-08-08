package strategy;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeOrder;
import api.tmp_trade.TradeType;
import keys.KeyManager;

import java.util.*;

import static api.Currency.*;

/**
 * Created by Timothy on 2/12/17.
 */
public class CandleCatcher extends Strategy {
    // Construct Market and obtain account balances.
    // Get Tickers for each currency pair
    // Place trades below according to what fraction we're using.
    // Sleep and move the trades as needed.
    // Log the orderid of the trades we've made.

    private static final CurrencyPair PAIR = CurrencyPair.of(LTC, BTC);
    private static final double AMOUNT_PER_FRACTION = 1; // Amount in Quote currency.
    private static final List<Double> FRACTIONS = Arrays.asList(new Double[]{ 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1 });


    public static void main(String[] args) {
        CandleCatcher c = new CandleCatcher();
        c.run();
    }

    @Override
    public void run() {
        Poloniex p = new Poloniex(Credentials.fromFileString(KeyManager.getKeyForMarket("Poloniex", KeyManager.Machine.LAPTOP)));

    }
}
