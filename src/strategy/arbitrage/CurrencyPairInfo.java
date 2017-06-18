package strategy.arbitrage;

import api.tmp_trade.Trade;

import java.util.List;

/**
 * Created by Timothy on 5/31/17.
 */
class CurrencyPairInfo {
    List<Trade> asks;
    List<Trade> bids;
    double minAmount; // What are the units of this?
    double takerFee;
}
