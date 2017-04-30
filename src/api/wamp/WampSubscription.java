package api.wamp;

import rx.functions.Action1;
import rx.functions.Func1;
import ws.wamp.jawampa.PubSubData;
import ws.wamp.jawampa.SubscriptionFlags;
import ws.wamp.jawampa.WampClient;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Timothy on 4/17/17.
 */
public final class WampSubscription<T> {

    private final WampClientWrapper client;
    private final String name;
    private final Func1<PubSubData, T> mapper;
    private final List<Action1<T>> callbacks;

    // TODO(stfinancial): What if we don't want a mapper?
    // TODO(stfinancial): Could take in a WampClientwrapper and instead make a wrapper class for that with generic types.
    public WampSubscription(WampClientWrapper client, String name, Func1<PubSubData, T> mapper) {
        this.client = client;
        this.name = name;
        this.mapper = mapper;
        this.callbacks = new LinkedList<>();
        client.addSubscription(this);
    }

    public boolean registerCallback(Action1<T> callback) {
        if (!client.ok() || callback == null) {
            return false;
        }
        callbacks.add(callback);
        client.getClient().makeSubscription(name).map(mapper).subscribe(callback);
        return true;
    }

    // Used if the client has disconnected and is reconnecting.
    void resubscribeCallbacks(WampClient client) {
        for (Action1<T> callback : callbacks) {
            // TODO(stfinancial): Use client.addSubscription instead?
            client.makeSubscription(name).map(mapper).subscribe(callback);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
