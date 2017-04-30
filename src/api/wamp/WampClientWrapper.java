package api.wamp;

import ws.wamp.jawampa.WampClient;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Timothy on 4/15/17.
 */
public final class WampClientWrapper extends Thread {
    private final WampClient client;
    private List<WampSubscription<?>> subscriptions;
    private final boolean ok;

    public WampClientWrapper(WampClient client) {
        this.client = client;
        if (client == null) {
            System.out.println("Wrapper initialized with null client.");
            ok = false;
        } else {
            this.subscriptions = new LinkedList<>();
            client.statusChanged().subscribe((state) -> {
                if (state instanceof WampClient.ConnectedState) {
                    System.out.println("Connected.");
                    for (WampSubscription<?> sub : subscriptions) {
                        sub.resubscribeCallbacks(client);
                    }
                }
                if (state instanceof WampClient.DisconnectedState) {
                    System.out.println("Disconnected.");
                }
            });
            client.open();
            Runtime.getRuntime().addShutdownHook(this);
            ok = true;
        }
    }

    <T> void addSubscription(WampSubscription<T> subscription) {
        subscriptions.add(subscription);
    }

    WampClient getClient() {
        return client;
    }

    boolean ok() {
        return ok;
    }

    // TODO(stfinancial): Handle case where we never connect.
    // TODO(stfinancial): This is not closing properly at the moment.
    @Override
    public void run() {
        client.close().toBlocking().last();
    }
}
