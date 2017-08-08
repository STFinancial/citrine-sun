package strategy;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.CompletedTrade;
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

    private static final AccountType ACCOUNT_TYPE = AccountType.EXCHANGE;
    private static final CurrencyPair PAIR = CurrencyPair.of(LTC, BTC);
//    private static final double AMOUNT_PER_FRACTION = 1; // Amount in Quote currency.
    private static final List<Double> FRACTIONS = Arrays.asList(new Double[]{ 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1 });
    private static final double SATOSHI = 0.00000001;
    private final HashMap<String, Double> orders = new HashMap<>();


    // TODO(stfinancial): Handle edge case where order fills immediately when we place it (this may be handled automatically).
    // TODO(stfinancial): Update account balances every loop.

    public static void main(String[] args) {
        CandleCatcher c = new CandleCatcher();
        c.run();
    }

    @Override
    public void run() {
        MarketResponse response;
        Poloniex p = new Poloniex(KeyManager.getCredentialsForMarket("Poloniex", KeyManager.Machine.LAPTOP));

        // Get account balances
        AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(ACCOUNT_TYPE);
        do {
            response = p.processMarketRequest(accountBalanceRequest);
            sleep(350);
        } while (!response.isSuccess());
        double balance = ((AccountBalanceResponse) response).getBalances().getOrDefault(ACCOUNT_TYPE, Collections.emptyMap()).getOrDefault(PAIR.getQuote(), 0.0);


//        double amountPerFraction = Math.max(0.0, (balance / FRACTIONS.size()) - SATOSHI);

        // Get ticker and highest bid
        TickerRequest tickerRequest = new TickerRequest(Arrays.asList(PAIR));
        do {
            response = p.processMarketRequest(tickerRequest);
            sleep(350);
        } while (!response.isSuccess());
        double highestBid = ((TickerResponse) response).getTickers().get(PAIR).getHighestBid();

        // Get trades and place them
        double quoteAmountPerFraction = Math.max(0.0, (balance / FRACTIONS.size()) - SATOSHI);
        for (double fraction : FRACTIONS) {
            Trade t = getTradeForFraction(highestBid, fraction, quoteAmountPerFraction);
            TradeRequest r = new TradeRequest(t);
            MarketResponse resp;
            do {
                resp = p.processMarketRequest(r);
                sleep(350);
            } while (!resp.isSuccess());
            orders.put(((TradeResponse) resp).getOrderNumber(), fraction);
        }

        // Loop perpetually
        while (true) {
            System.out.println("Running...");
            MarketResponse r;
            do {
                r = p.processMarketRequest(tickerRequest);
                sleep(350);
            } while (!r.isSuccess());
            highestBid = ((TickerResponse) r).getTickers().get(PAIR).getHighestBid();
            System.out.println("New highest bid at: " + highestBid);

            // Check that none of our orders got filled.
            HashMap<String, Double> newOrders = new HashMap<>();
            for (Map.Entry<String, Double> order : orders.entrySet()) {
                OrderTradesRequest t = new OrderTradesRequest(order.getKey());
                r = p.processMarketRequest(t);
                sleep(350);
                System.out.println(r.getJsonResponse());
                if (r.isSuccess() && ((OrderTradesResponse) r).getTrades().size() != 0) {
                    System.out.println("Order was at least partially filled: " + r.getJsonResponse());
                    // Cancel the remainder of the order
                    CancelRequest c = new CancelRequest(order.getKey(), CancelRequest.CancelType.TRADE);
                    do {
                        r = p.processMarketRequest(c);
                        sleep(350);
                    } while (!r.isSuccess());
                    System.out.println(r.getJsonResponse());
                    // Now look at the trades executed by this order
                    do {
                        r = p.processMarketRequest(t);
                        sleep(350);
                    } while (!r.isSuccess());
                    double amountFilled = 0.0;
                    for (CompletedTrade trade : ((OrderTradesResponse) r).getTrades()) {
                        amountFilled += trade.getTrade().getAmount();
                    }
                    // Sell this amount at some specified level
                    // TODO(stfinancial): Figure out how best to do this. Make fraction a constant.
                    TradeRequest tr = new TradeRequest(new Trade(amountFilled, highestBid * (1 - 0.01), PAIR, TradeType.SELL));
                    do {
                        r = p.processMarketRequest(tr);
                        sleep(1000);
                    } while (!r.isSuccess());

                    // Then place a new trade.
                    tr = new TradeRequest(getTradeForFraction(highestBid, order.getValue(), quoteAmountPerFraction));
                    do {
                        r = p.processMarketRequest(tr);
                        sleep(350);
                    } while (!r.isSuccess());
                    newOrders.put(((TradeResponse) r).getOrderNumber(), order.getValue());
                } else {
                    System.out.println("No fills found, moving.");
                    // Move the trade
                    double movePrice = highestBid * (1 - order.getValue());
                    MoveOrderRequest m = new MoveOrderRequest(order.getKey(), movePrice);
                    m.setAmount(((quoteAmountPerFraction) / movePrice) - SATOSHI);
                    do {
                        r = p.processMarketRequest(m);
                        sleep(350);
                    } while (!r.isSuccess());
                    newOrders.put(((MoveOrderResponse) r).getOrderNumber(), order.getValue());
                }
            }
            orders.clear();
            orders.putAll(newOrders);
        }
    }

    private Trade getTradeForFraction(double highestBid, double fraction, double quoteAmountPerFraction) {
        double price = highestBid * (1 - fraction);
        double amount = (quoteAmountPerFraction / price) - SATOSHI;
        return new Trade(amount, price, PAIR, TradeType.BUY);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("Interrupted in CandleCatcher...");
        }
    }
}
