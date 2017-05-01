package strategy;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.gdax.Gdax;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;

import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 4/23/17.
 */
public class SlowArbitrageStrategy extends Strategy {
    private static final String POLONIEX_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final String GDAX_KEYS = "/Users/Timothy/Documents/Keys/gdax_key.txt";

    private static final double CURRENT_POLO_FEE = 0.0022;
    private static final double CURRENT_GDAX_FEE = 0.003;

    // TODO(stfinancial): We will expand to more pairs as we hook up the WAMP and socket endpoints.
    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.ETH, Currency.BTC);

    private static final boolean DRY_RUN = true;
    Poloniex polo;
    Gdax gdax;

    private double poloTakerFee;
    private double poloBaseBalance;
    private double poloQuoteBalance;
    private double gdaxTakerFee;
    private double gdaxBaseBalance;
    private double gdaxQuoteBalance;

    public static void main(String[] args) {
        SlowArbitrageStrategy strategy = new SlowArbitrageStrategy();
        strategy.run();
    }

    @Override
    public void run() {
        polo = new Poloniex(Credentials.fromFileString(POLONIEX_KEYS));
        gdax = new Gdax(Credentials.fromFileString(GDAX_KEYS));
        // TODO(stfinancial): Do this on a timer.
        refreshFees();
        if (!refreshBalances()) {
            sleep(10000);
        }

        OrderBookRequest orderBookRequest = new OrderBookRequest(PAIR, 20, 2, 1);
        OrderBookResponse orderBookResponse;
        List<Trade> poloBids;
        List<Trade> poloAsks;
        List<Trade> gdaxBids;
        List<Trade> gdaxAsks;
        MarketResponse response;
        while (true) {
            response = polo.processMarketRequest(orderBookRequest);
            if (!response.isSuccess()) {
                System.out.println("error: " + response.getJsonResponse());
                sleep(10000);
            }
            orderBookResponse = (OrderBookResponse) response;
            poloBids = orderBookResponse.getBids().get(PAIR);
            if (poloBids == null) System.out.println("Null 1WTF");
            poloAsks = orderBookResponse.getAsks().get(PAIR);
            if (poloAsks == null) System.out.println("Null 2WTF");
            response = gdax.processMarketRequest(orderBookRequest);
            if (!response.isSuccess()) {
                System.out.println("error: " + response.getJsonResponse());
                sleep(10000);
            }
            orderBookResponse = (OrderBookResponse) response;
            gdaxBids = orderBookResponse.getBids().get(PAIR);
            if (gdaxBids == null) System.out.println("Null 3WTF");
            gdaxAsks = orderBookResponse.getAsks().get(PAIR);
            if (gdaxAsks == null) System.out.println("Null 4WTF");

            // Test buy on poloniex and sell on GDAX
            if (isArbitrage(gdaxBids.get(0), gdaxTakerFee, poloAsks.get(0), poloTakerFee)) {
                System.out.println("Arbitrage found!!!");
                System.out.println("Polo (Buy): " + poloAsks.get(0).getRate() + " - " + poloAsks.get(0).getAmount());
                System.out.println("Gdax (Sell): " + gdaxBids.get(0).getRate() + " - " + gdaxBids.get(0).getAmount());
                System.exit(1);
            }
            // Test sell on poloniex and buy on GDAX
            if (isArbitrage(poloBids.get(0), poloTakerFee, gdaxAsks.get(0), gdaxTakerFee)) {
                System.out.println("Arbitrage found!!!");
                System.out.println("Gdax (Buy): " + gdaxAsks.get(0).getRate() + " - " + gdaxAsks.get(0).getAmount());
                System.out.println("Polo (Sell): " + poloBids.get(0).getRate() + " - " + poloBids.get(0).getAmount());
                System.exit(1);
            }
            sleep(500);
        }
    }

    // TODO(stfinancial): We can probably precompute the fee coefficient and stuff.
    private boolean isArbitrage(Trade bid, double buyFee, Trade ask, double sellFee) {
        // Say both fees are 20%
        // Buy 1 BTC at 100 dollars. I get 1 * (1- 0.2) = 0.8 BTC
        // Sell 0.8 BTC at what price to get 100 dollars?
        // Say 200 bucks... I get 0.8 * 200 = 160 -> 160 * (1 - 0.2) = 128 bucks
        // The actual price would be (100 / (1 - 0.2)) / (1 - 0.2) = 156.25
        // Lets test
        // 0.8 * 156.25 * (1- 0.2) = 100 √
        // So that means give buy price B... arbitrage exists if sell price S > B / (1 - buyfee) / (1 - sellfee)

        double buyingPrice = ask.getRate();
        double requiredSellingPrice = buyingPrice / ((1 - buyFee) * (1 - sellFee));
        System.out.println("Buying Price (Lowest Ask): " + buyingPrice);
        System.out.println("Selling Price (Highest Bid): " + bid.getRate());
        System.out.println("Required Selling Price: " + requiredSellingPrice);
        return requiredSellingPrice < bid.getRate();
    }

    private boolean refreshBalances() {
        AccountBalanceRequest request = new AccountBalanceRequest(AccountType.EXCHANGE, 1, 1);
        MarketResponse response;
        Map<Currency, Double> balances;
        response = polo.processMarketRequest(request);
        // TODO(stfinancial): How do we handle failure here... for now we will return boolean...?
        if (!response.isSuccess()) {
            System.out.println(response.getJsonResponse());
            return false;
        }
        // TODO(stfinancial): What about the case where the account is totally empty? There still should be an empty map...
        balances = ((AccountBalanceResponse) response).getBalances().get(AccountType.EXCHANGE);
        poloBaseBalance = balances.containsKey(PAIR.getBase()) ? balances.get(PAIR.getBase()) : 0;
        poloQuoteBalance = balances.containsKey(PAIR.getQuote()) ? balances.get(PAIR.getQuote()) : 0;

        response = gdax.processMarketRequest(request);
        // TODO(stfinancial): How do we handle failure here... for now we will return boolean...?
        if (!response.isSuccess()) {
            System.out.println(response.getJsonResponse());
            return false;
        }
        // TODO(stfinancial): What about the case where the account is totally empty? There still should be an empty map...
        balances = ((AccountBalanceResponse) response).getBalances().get(AccountType.EXCHANGE);
        gdaxBaseBalance = balances.containsKey(PAIR.getBase()) ? balances.get(PAIR.getBase()) : 0;
        gdaxQuoteBalance = balances.containsKey(PAIR.getQuote()) ? balances.get(PAIR.getQuote()) : 0;
        return true;
    }

    private boolean refreshFees() {
        // WE can gracefully recover from failure here... we just be as conservative as needed.
        FeeRequest feeRequest = new FeeRequest(PAIR, 1, 1);
        MarketResponse response;
        response = polo.processMarketRequest(feeRequest);
        if (!response.isSuccess()) {
            System.out.println(response.getJsonResponse());
            poloTakerFee = CURRENT_POLO_FEE;
            gdaxTakerFee = CURRENT_GDAX_FEE;
            return false;
        }
        poloTakerFee = ((FeeResponse) response).getFeeInfo().getTakerFee();
        response = gdax.processMarketRequest(feeRequest);
        if (!response.isSuccess()) {
            System.out.println(response.getJsonResponse());
            poloTakerFee = CURRENT_POLO_FEE;
            gdaxTakerFee = CURRENT_GDAX_FEE;
            return false;
        }
        // TODO(stfinancial): Gotta handle GDAX's stupid way of handling fees.
        gdaxTakerFee = ((FeeResponse) response).getFeeInfo().getTakerFee();
        return true;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
