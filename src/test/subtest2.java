//package test;
//
//import api.Credentials;
//import api.Ticker;
//import api.gdax.Gdax;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import rx.Subscription;
//import rx.functions.Action1;
//import ws.wamp.jawampa.PubSubData;
//import ws.wamp.jawampa.Reply;
//import ws.wamp.jawampa.WampClient;
//import ws.wamp.jawampa.WampClientBuilder;
//import ws.wamp.jawampa.connection.IWampClientConnectionConfig;
//import ws.wamp.jawampa.connection.IWampConnectorProvider;
//import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;
//import ws.wamp.jawampa.transport.netty.NettyWampConnectionConfig;
//
//import java.io.IOException;
//import java.util.Observable;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by Zarathustra on 4/19/2017.
// */
//public class subtest2 extends Thread implements
//        //Action1<PubSubData>
//        Action1<Reply>
//{
//    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
//    static Subscription sub;
//    WampClient client;
//
//    public static void main(String[] args) {
//        subtest2 subtest = new subtest2();
//        try {
//            subtest.test();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void test() throws IOException {
//        try {
//            final subtest2 s = this;
//            try {
//                WampClientBuilder builder = new WampClientBuilder();
//                IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
//                // TODO(stfinancial): Adding headers to NettyWampConnectConfig should be possible if a new release comes out.
////                IWampClientConnectionConfig config = (new NettyWampConnectionConfig.Builder()).w
//                builder.withConnectorProvider(connectorProvider)
//                        .withUri("wss://ws-feed.gdax.com")
//                        .withRealm("realm1")
//                        .withInfiniteReconnects()
//                        .withReconnectInterval(1, TimeUnit.SECONDS);
////                        .withConnectionConfiguration(IWampClientConnectionConfig );
//                        //.withAuthId("{\"type\":\"subscribe\", \"product_ids\": [\"BTC-USD\"]}");
//                // Test
//                // .withAuthId
//                // .withConnectionConfiguration
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
//            JsonNode node = mapper.readTree("{\"type\":\"subscribe\", \"product_ids\": [\"BTC-USD\"]}");
//
////            final Observable<PubSubData> subscription;subscription
//            final Subscription subscription1;
//            client.statusChanged().subscribe(new Action1<WampClient.State>() {
//                @Override
//                public void call(WampClient.State t1) {
//                    if (t1 instanceof WampClient.ConnectedState) {
//                        System.out.println("Connected.");
//                        sub = client.makeSubscription("subscribe").subscribe((data) -> { System.out.println("blah"); });
//                        client.call("subscribe", node).subscribe(s);
////                        sub = client.makeSubscription("ticker").map((d) -> { return new BigDecimal(55.5); } ).subscribe(s);
//                    }
//                    if (t1 instanceof WampClient.DisconnectedState) {
//                        System.out.println("Disconnected.");
//                    }
//                    if (t1 instanceof WampClient.ConnectingState) {
//                        System.out.println("Connecting.");
////                        client.
//                    }
//                }
//            });
//            Runtime.getRuntime().addShutdownHook(this);
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
//
//    @Override
//    public void run() {
//        client.close().toBlocking().last();
//    }
//
//    @Override
//    public void call(Reply r) {
//        System.out.println("Gotcha");
//        System.out.println(r.arguments());
//    }
//
////    @Override
////    public void call(PubSubData data) {
////        System.out.println("Gotcha");
////        System.out.println(data.arguments());
////    }
//}
