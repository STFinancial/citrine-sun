package utils;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.*;
import api.request.tmp_trade.MoveOrderRequest;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import keys.KeyManager;

/**
 * Created by Zarathustra on 7/28/2017.
 */
public class UndercutOvercut implements Runnable {
    // TODO(stfinancial): Need a way to handle when someone places an order really far away from the next lowest ask/highest bid.

    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.XMR, Currency.BTC);
    private static final TradeType ORIENTATION = TradeType.SELL;
//    private static final boolean IS_MARGIN = true;

    // NOTE!!! We are currently assuming the quote currency is BTC and we are using margin.

    public static void main(String[] args) {
        UndercutOvercut u = new UndercutOvercut();
        u.run();
    }

    public void run() {
        Poloniex p = new Poloniex(KeyManager.getCredentialsForMarket("Poloniex", KeyManager.Machine.DESKTOP));
        OrderBookRequest o = new OrderBookRequest(PAIR, 1);

        MarketResponse resp;
        OrderBookResponse obr;
        double rate;
        String orderNumber;
        while (true) {
            do {
                resp = p.processMarketRequest(o);
            } while (!resp.isSuccess());
            obr = (OrderBookResponse) resp;
            if (ORIENTATION == TradeType.SELL) {
                Trade lA = obr.getAsks().get(PAIR).get(0);
                rate = lA.getRate() - 0.00000001;
                TradeRequest t = new TradeRequest(new Trade(0.0201 / rate, rate, PAIR, TradeType.SELL));
                t.setIsMargin(true);
                do {
                    resp = p.processMarketRequest(t);
                } while (!resp.isSuccess());
                orderNumber = ((TradeResponse) resp).getOrderNumber();
                OpenOrderRequest oor = new OpenOrderRequest(PAIR);
                do { resp = p.processMarketRequest(oor); } while (!resp.isSuccess());
                // While the trade is still active
                while (((OpenOrderResponse) resp).getOpenOrdersById().containsKey(orderNumber)) {
                    double remainingQuoteAmount = ((OpenOrderResponse) resp).getOpenOrdersById().get(orderNumber).getTrade().getAmount() * rate;
                    // Sleep
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {}
                    // Check if we need to move it.
                    do {
                        resp = p.processMarketRequest(o);
                    } while (!resp.isSuccess());
                    obr = (OrderBookResponse) resp;
                    if (obr.getAsks().get(PAIR).get(0).getRate() != rate) {
                        System.out.println("Current Rate: " + obr.getAsks().get(PAIR).get(0).getRate() + " - " + "Our Rate: " + rate);
                        rate = obr.getAsks().get(PAIR).get(0).getRate() - 0.00000001;
                        MoveOrderRequest m = new MoveOrderRequest(orderNumber, rate);
                        m.setAmount(remainingQuoteAmount / rate);
                        do { resp = p.processMarketRequest(m); } while (!resp.isSuccess());
                        orderNumber = ((MoveOrderResponse) resp).getOrderNumber();
                    }
                }

            }
        }


    }
}
