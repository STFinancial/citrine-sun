package test;


import api.Credentials;
import api.Ticker;
import api.poloniex.Poloniex;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action;
import rx.functions.Action1;
import ws.wamp.jawampa.PubSubData;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Created by Timothy on 1/14/17.
 */
public class subtest extends Thread
        implements
//        Action1<PubSubData>
        Action1<Ticker>
{
    static Subscription sub;
    WampClient client;

    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";

    public static void main(String[] args) {
        subtest s = new subtest();
//        Runtime.getRuntime().addShutdownHook(s);
//        s.test();
        s.test2();
    }

    private void test2() {
        Credentials c = Credentials.fromFileString(API_KEYS);
        Poloniex p = new Poloniex(c);
//        try {
//            sleep(5000);
//        } catch (Exception e) {
//
//        }
        p.registerTickerSubscription(this);
        while(true) {
            try {
                sleep(1000);
            } catch (Exception e) {

            }
        }
    }

//    private void test() {
//        try {
//            final subtest s = this;
//            try {
//                WampClientBuilder builder = new WampClientBuilder();
//                IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
//                builder.withConnectorProvider(connectorProvider)
//                        .withUri("wss://api.poloniex.com")
//                        .withRealm("realm1")
//                        .withInfiniteReconnects()
//                        .withReconnectInterval(1, TimeUnit.SECONDS);
//                client = builder.build();
//
//            } catch (Exception e) {
//                client = null;
//                System.out.println(e);
//                return;
//            }
//            System.out.println(this);
//
//            ObjectMapper mapper = new ObjectMapper();
//            final Observable<Ticker> subscription;
//            final Subscription subscription1;
//            client.statusChanged().subscribe(new Action1<WampClient.State>() {
//                @Override
//                public void call(WampClient.State t1) {
//                    if (t1 instanceof WampClient.ConnectedState) {
//                        System.out.println(Thread.currentThread().toString());
//                        sub = client.makeSubscription("ticker").subscribe(s);
////                        sub = client.makeSubscription("ticker").map((d) -> { return new BigDecimal(55.5); } ).subscribe(s);
//                    }
//                    if (t1 instanceof WampClient.DisconnectedState) {
//                        System.out.println("Disconnected.");
//                    }
//                }
//            });
//            client.open();
//            while (true) {
//                try {
//                    sleep(100);
//                } catch (Exception e) {
//                    client.close();
//                }
//            }
//
//            // ...
//            // use the client here
//            // ...
//
//            // Wait synchronously for the client to close
//            // On environments like UI thread asynchronous waiting should be preferred
////        client.close().toBlocking().last();
//        } finally {
//        }
//
//    }

    @Override
    public void run() {
        System.out.println("Here.");
        System.out.println(this);
//        client.close();
        try {
            sleep(200);
        } catch (Exception e) {

        }

    }

    @Override
    public void call(Ticker t) {
        System.out.println(t.getPair().toString());
    }

//    @Override
//    public void call(PubSubData d) {
////        for (JsonNode j : d.arguments()) {
////            System.out.println(j.asText());
////        }
//        System.out.println(d.arguments().get(0).asText());
////        System.out.println(d.keywordArguments().toString());
////        if (d.arguments().get(0).toString().equals("\"BTC_ETC\"")) {
////            System.out.println("Gotcha!!");
////        }
//    }
}
