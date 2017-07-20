package api.queue;

import api.Market;
import api.request.MarketRequest;
import api.request.MarketResponse;

import java.util.concurrent.Callable;

/**
 * Created by Timothy on 7/9/17.
 */
public class WorkItem implements Callable<MarketResponse>, Comparable<WorkItem> {
    // TODO(stfinancial): Add priority and timestamp here, we can probably remove these as mandatory parts of the MarketRequest constructor, adding them only as desired.

    private final Market market;
    private final MarketRequest request;
    private boolean rateBlocked = true;

    // TODO(stfinancial): Should these be final?
    private int priority;
    private long timestamp;
    private boolean hasPriorityOverride;
    private long maxWaitTime;

    WorkItem(Market market, MarketRequest request) {
        this.market = market;
        this.request = request;
        this.priority = request.getPriority();
        // TODO(stfinancial): I'm thinking we should just generate the timestamp here.
        this.timestamp = request.getTimestamp();
        this.hasPriorityOverride = request.hasPriorityOverride();
        this.maxWaitTime = request.getMaxWaitTime();
    }

    public void setRateBlocked(boolean isBlocked) {
        rateBlocked = isBlocked;
    }

//    public boolean isRateBlocked() {
//        return rateBlocked;
//    }

    @Override
    public int compareTo(WorkItem o) {
        if (hasPriorityOverride && System.currentTimeMillis() > timestamp + maxWaitTime) {
            return 1;
        }
        if (priority > o.priority) {
            return 1;
        } else if (priority < o.priority) {
            return -1;
        } else if (timestamp > o.timestamp) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public MarketResponse call() throws Exception {
        System.out.println("Waiting...");
        synchronized (this) {
            while (rateBlocked) {
                // TODO(stfinancial): Do we put the try/catch here instead of throwing the exception?
                wait();
            }
        }
        return market.processMarketRequest(request);
    }
}
