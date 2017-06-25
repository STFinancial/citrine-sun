package utils;

import static api.Currency.*;

import api.Credentials;
import api.CurrencyPair;
import api.Market;
import api.Ticker;
import api.gdax.Gdax;
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
    private static final double PRIMARY_LIMIT = 31;
    // If true, allows the spreader (at trade calculation time) to run even though a resulting trade will be a market taker
    // Setting this to false is a safeguard against mispricings.
    private static final boolean ALLOW_MARKET_TAKES = true;
    private static final double RANDOMIZER_RATE = 0.02;
    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final String EXCHANGE = "Poloniex";
//    private static final String API_KEYS = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";
//    private static final int ROUND_DECIMALS = 8;
//    private static final String EXCHANGE = "Gdax";
//    private static final String API_KEYS = "F:\\Users\\Zarathustra\\Documents\\gdax_key.txt";
    private static final int ROUND_DECIMALS = 8;

//    private static final double PRICE = 0.0504;
//    private static final double RANGE = 0.0045;
//    private static final double AMOUNT = 13.65;
//    private static final CurrencyPair PAIR = CurrencyPair.of(DASH, BTC);

//    private static final double PRICE = 2180;
//    private static final double RANGE = 200;
//    private static final double AMOUNT = 5.6;
//    private static final CurrencyPair PAIR = CurrencyPair.of(BTC, USDT);

//    private static final double PRICE = 2300;
//    private static final double RANGE = 300;
//    private static final double AMOUNT = 10.75;
//    private static final CurrencyPair PAIR = CurrencyPair.of(BTC, USD);

//    private static final double PRICE = 25.4;
//    private static final double RANGE = 3;
//    private static final double AMOUNT = 1175;
//    private static final CurrencyPair PAIR = CurrencyPair.of(LTC, USD);

//    private static final double PRICE = 250;
//    private static final double RANGE = 85;
//    private static final double AMOUNT = 240;
//    private static final CurrencyPair PAIR = CurrencyPair.of(ETH, USD);

//    private static final double PRICE = 0.000095;
//    private static final double RANGE = 0.000003;
//    private static final double AMOUNT = 73800;
//    private static final CurrencyPair PAIR = CurrencyPair.of(XRP, BTC);

//    private static final double PRICE = 0.0158;
//    private static final double RANGE = 0.001;
//    private static final double AMOUNT = 698;
//    private static final CurrencyPair PAIR = CurrencyPair.of(LTC, BTC);

    private static final double PRICE = 0.1263;
    private static final double RANGE = 0.003;
    private static final double AMOUNT = 242;
    private static final CurrencyPair PAIR = CurrencyPair.of(ETH, BTC);

//    private static final double PRICE = 0.0026;
//    private static final double RANGE = 0.0002;
//    private static final double AMOUNT = 1239;
//    private static final CurrencyPair PAIR = CurrencyPair.of(CLAM, BTC);

//    private static final double PRICE = 0.00425;
//    private static final double RANGE = 0.0004;
//    private static final double AMOUNT = 1228;
//    private static final CurrencyPair PAIR = CurrencyPair.of(FCT, BTC);

//    private static final double PRICE = 0.000164;
//    private static final double RANGE = 0.000007;
//    private static final double AMOUNT = 12900;
//    private static final CurrencyPair PAIR = CurrencyPair.of(MAID, BTC);

//    private static final double PRICE = 0.01327;
//    private static final double RANGE = 0.001;
//    private static final double AMOUNT = 250;
//    private static final CurrencyPair PAIR = CurrencyPair.of(XMR, BTC);

//    private static final double PRICE = 0.00000067;
//    private static final double RANGE = 0.00000008;
//    private static final double AMOUNT = 2450000;
//    private static final CurrencyPair PAIR = CurrencyPair.of(DOGE, BTC);

//    private static final double PRICE = 0.00001950;
//    private static final double RANGE = 0.000001;
//    private static final double AMOUNT = 179300;
//    private static final CurrencyPair PAIR = CurrencyPair.of(STR, BTC);

    private static final int BUCKETS = 121;
    private static final TradeType TYPE = TradeType.SELL;
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
        Market market;
        switch (EXCHANGE) {
            case "Gdax":
                market = new Gdax(Credentials.fromFileString(API_KEYS));
                break;
            case "Poloniex":
                market = new Poloniex(Credentials.fromFileString(API_KEYS));
                break;
            default:
                System.out.println("Invalid exchange name: " + EXCHANGE);
                return;
        }
        if (!ALLOW_MARKET_TAKES) {
            MarketResponse r = market.processMarketRequest(new TickerRequest(Arrays.asList(PAIR), 1, 1));
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
            // TODO(stfinancial): Fix null ptr exception here.
            // If the lowest sell is lower than the highest buy, then we are going to be a taker.
            if (TYPE == TradeType.SELL && t.getHighestBid() >= PRICE - RANGE) {
                System.out.println("Lowest sell lower than highest buy.");
                return;
            }
        }
        MarketResponse r;
        for (TradeRequest req : getTrades()) {
            // TODO(stfinancial): Do something if it fails?
            while (!(r = market.processMarketRequest(req)).isSuccess()) {
                try {
                    // TODO(stfinancial): Need to make sure that if this fails with jsonMappingException that the trade didn't go through and the error wasn't somewhere else.
                    System.out.println("Failed request, sleeping... : " + r.getJsonResponse());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {}
        }
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
            priceIncrement = ((int) (((endPrice - startPrice) / (BUCKETS - 1)) * 100000000)) / 100000000.0;
            System.out.println("Price increment: " + priceIncrement);
        }
        LinkedList<TradeRequest> requests = new LinkedList<>();
        Trade t;
        TradeRequest r;
        List<Double> randomizationRates = getTradeRandomization(BUCKETS);
        double randAmount = AMOUNT * RANDOMIZER_RATE;
        for (int bucket = 0; bucket < BUCKETS; bucket++) {
            System.out.println(randomizationRates.get(bucket));

            // TODO(stfinancial): Switch to BigDecimalStringConverter.
            double amount = ((long) ((amountPerBucket + (randomizationRates.get(bucket) * randAmount)) * 100000000)) / 100000000.0;
            double unroundedPrice = startPrice + (bucket * priceIncrement);
            System.out.println("Unrounded: " + unroundedPrice);
            double roundedPrice = ((long) (unroundedPrice * ((long) Math.pow(10, ROUND_DECIMALS)))) / Math.pow(10, ROUND_DECIMALS);
            System.out.println("Rounded: " + roundedPrice);
            t = new Trade(amount,
                          roundedPrice,
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
