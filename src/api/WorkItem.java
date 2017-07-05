package api;

import api.request.MarketRequest;
import api.request.MarketResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by Timothy on 7/2/17.
 */
public class WorkItem implements Callable<MarketResponse>, Comparable<WorkItem> {
    private final MarketRequest req;
    private final Market market;
    private Future<MarketResponse> work;
    private long timestamp;

    // TODO(stfinancial): Submission timestamp?

    WorkItem(MarketRequest req, Market market) {
        this.req = req;
        this.market = market;
    }

    public MarketRequest getRequest() {
        return req;
    }

    // TODO(stfinancial): Better encapsulation than this.
    // TODO(stfinancial): How do we know when this is actually called?
    public void setWork(Future<MarketResponse> work) {
        this.work = work;
    }

    // TODO(stfinancial): Better encapsulation than this...
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public MarketResponse call() throws Exception {
        return market.sendRequest(req);
    }

    @Override
    public int compareTo(WorkItem o) {
        return req.compareTo(o.req);
    }
}
