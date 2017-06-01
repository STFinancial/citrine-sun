package strategy.arbitrage;

import api.tmp_trade.Trade;

import java.util.List;

/**
 * Created by Timothy on 5/31/17.
 */
class CurrencyInfo {
    List<Trade> asks;
    List<Trade> bids;
    double minAmount;
    double quoteBalance;
    double baseBalance;
    double takerFee;
}
