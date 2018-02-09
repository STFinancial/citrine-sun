package strategy.arbitrage;

import api.Currency;
import api.CurrencyPair;
import api.Market;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Timothy on 5/31/17.
 */
class MarketInfo {
    Market market;
    Map<CurrencyPair, CurrencyPairInfo> currencyPairInfos = new HashMap<>();
    Map<Currency, Double> balances = new HashMap<>();
    int priority;
    CurrencyPair focusPair; // Set to whatever we've recently been arbitraging on. Checking other order books less frequently.
    Currency usdArbitrageCurrency; // Whether this exchange uses USD or USDT when arbitraging
}
