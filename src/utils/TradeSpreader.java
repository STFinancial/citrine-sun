package utils;

import static api.Currency.*;

import api.Credentials;
import api.CurrencyPair;
import api.Ticker;
import api.request.MarketResponse;
import api.request.TickerRequest;
import api.request.TickerResponse;
import api.tmp_trade.Trade;
import api.request.TradeRequest;
import api.tmp_trade.TradeType;
import api.poloniex.Poloniex;

import java.math.BigDecimal;
import java.util.*;

public class TradeSpreader {
    private static final Random random = new Random();

    // The maximum amount that is allowed of the primary currency for a run of this. This flag prevents accidentally mispricing, or selling the wrong asset.
    private static final double PRIMARY_LIMIT = 12.4;
    // If true, allows the spreader (at trade calculation time) to run even though a resulting trade will be a market taker
    // Setting this to false is a safeguard against mispricings.
    private static final boolean ALLOW_MARKET_TAKES = true;
    private static final double RANDOMIZER_RATE = 0.02;
    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
//    private static final String API_KEYS = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";

//    private static final double PRICE = 0.0504;
//    private static final double RANGE = 0.0045;
//    private static final double AMOUNT = 13.65;
//    private static final CurrencyPair PAIR = CurrencyPair.of(DASH, BTC);

//    private static final double PRICE = 0.0000254;
//    private static final double RANGE = 0.0000005;
//    private static final double AMOUNT = 125770;
//    private static final CurrencyPair PAIR = CurrencyPair.of(XRP, BTC);

//    private static final double PRICE = 0.01062;
//    private static final double RANGE = 0.00033;
//    private static final double AMOUNT = 846;
//    private static final CurrencyPair PAIR = CurrencyPair.of(LTC, BTC);

    private static final double PRICE = 0.0565;
    private static final double RANGE = 0.0013;
    private static final double AMOUNT = 25;
    private static final CurrencyPair PAIR = CurrencyPair.of(ETH, BTC);

//    private static final double PRICE = 0.00425;
//    private static final double RANGE = 0.0004;
//    private static final double AMOUNT = 1228;
//    private static final CurrencyPair PAIR = CurrencyPair.of(FCT, BTC);

//    private static final double PRICE = 0.0001532;
//    private static final double RANGE = 0.000006;
//    private static final double AMOUNT = 40200;
//    private static final CurrencyPair PAIR = CurrencyPair.of(MAID, BTC);

//    private static final double PRICE = 0.01585;
//    private static final double RANGE = 0.0002;
//    private static final double AMOUNT = 30;
//    private static final CurrencyPair PAIR = CurrencyPair.of(XMR, BTC);

    private static final int BUCKETS = 53;
    private static final TradeType TYPE = TradeType.BUY;
    private static final boolean IS_MARGIN = true;

    // TODO(stfinancial): Analyze trade order timestamps to construct a tree to tell me how many of a given order have been sold, so I can rebuy the same amount, for example.

    // TODO(stfinancial): If our order differs significantly from the price, do a check to make sure I didn't fuck up the typing.

    // This technique is useful for more reliably catching dips, minimizing risk that your order does not get hit at all
    public static void main(String[] args) {
        TradeSpreader s = new TradeSpreader();
        s.run();
    }

    // TODO(stfinancial): Decide on direction to place trades, closer to market value or farther first.
    private void run() {
        System.out.println("Total amount: " + PRICE * AMOUNT);
        if (PRICE * AMOUNT > PRIMARY_LIMIT) {
            System.out.println(PRICE * AMOUNT + " Greater than primary limit");
            return;
        }
        if (RANGE < 0) {
            System.out.println("Negative range not allowed");
            return;
        }
        Credentials c = Credentials.fromFileString(API_KEYS);
        Poloniex polo = new Poloniex(c);
        if (!ALLOW_MARKET_TAKES) {
            MarketResponse r = polo.processMarketRequest(new TickerRequest(1, 1));
            if (!r.isSuccess()) {
                System.out.println("Failed to obtain ticker data.");
                System.out.println(r.getJsonResponse().toString());
                return;
            }
            Ticker t = ((TickerResponse) r).getTickers().get(PAIR);
            // If the highest buy is higher than the lowest sell, then we are going to be a taker.
            if (TYPE == TradeType.BUY && t.getLowestAsk() <= PRICE + RANGE) {
                System.out.println("Highest buy higher than lowest sell.");
                return;
            }
            // If the lowest sell is lower than the highest buy, then we are going to be a taker.
            if (TYPE == TradeType.SELL && t.getHighestBid() >= PRICE - RANGE) {
                System.out.println("Lowest sell lower than highest buy.");
                return;
            }
        }
        getTrades().forEach((req)->{
            // TODO(stfinancial): Do something if it fails?
            polo.processMarketRequest(req);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {}
        });
    }

    private List<TradeRequest> getTrades() {
        if (BUCKETS < 1) {
            System.out.println("Invalid bucket values: " + BUCKETS);
            return Collections.emptyList();
        }
        double amountPerBucket = ((1 - RANDOMIZER_RATE) * AMOUNT) / BUCKETS;
        double startPrice = PRICE - RANGE;
        double endPrice = PRICE + RANGE;
        double priceIncrement;
        if (BUCKETS == 1) {
            priceIncrement = 0;
        } else {
            priceIncrement = (endPrice - startPrice) / (BUCKETS - 1);
        }
        LinkedList<TradeRequest> requests = new LinkedList<>();
        Trade t;
        TradeRequest r;
        List<Double> randomizationRates = getTradeRandomization(BUCKETS);
        double randAmount = AMOUNT * RANDOMIZER_RATE;
        for (int bucket = 0; bucket < BUCKETS; bucket++) {
            System.out.println(randomizationRates.get(bucket));

            // TODO(stfinancial): Switch to BigDecimalStringConverter.
            t = new Trade(amountPerBucket + (randomizationRates.get(bucket) * randAmount),
                          startPrice + (bucket * priceIncrement),
                          PAIR,
                          TYPE);
            r = new TradeRequest(t, 1, System.currentTimeMillis());
            r.setIsMargin(IS_MARGIN);
            requests.add(r);
        }
        return requests;
    }

    // TODO(stfinancial): Check that this is actually working properly. Seems to produce large outliers.
    // This method essentially gives the fraction to multiply RANDOMIZER_PCT * amountPerBucket by.  These numbers are added to each
    // trade to "cloak" the orders as not being uniform.
    private List<Double> getTradeRandomization(int numBuckets) {
        ArrayList<Double> nums = new ArrayList<>(numBuckets + 1);
        for (int i = 0; i < numBuckets - 1; i++) {
            nums.add(random.nextDouble());
        }
        nums.add(0.0);
        nums.add(1.0);
        Collections.sort(nums);
        ArrayList<Double> randFactors = new ArrayList<>(numBuckets);
        for (int i = 0; i < numBuckets; i++) {
            randFactors.add(nums.get(i+1) - nums.get(i));
        }
        return randFactors;
    }
}
