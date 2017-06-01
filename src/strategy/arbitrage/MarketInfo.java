package strategy.arbitrage;

import api.CurrencyPair;
import api.Market;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Timothy on 5/31/17.
 */
class MarketInfo {
    Market market;
    Map<CurrencyPair, CurrencyInfo> currencyInfos = new HashMap<>();
    int priority;
}
