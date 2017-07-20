package api.queue;

import api.Market;
import api.QueueStrategy;
import api.request.MarketRequest;
import api.request.MarketResponse;

import java.util.concurrent.*;

// TODO(stfinancial): Perhaps offload some of the work from run() into the abstract class.
/**
 * Abstract queue of {@link MarketRequest} to accommodate for different API rate limits on a {@link Market}. Client
 * Markets should extend this class to handle their respective limits and {@link QueueStrategy queue strategies}. Once
 * constructed, the queue should be started by calling run(), unless the strategy is {@link QueueStrategy#DISABLED}, in
 * which case the queue need not be started.
 */
public abstract class RequestQueue implements Runnable {
    private final Market market;
    private final QueueStrategy strategy;

    // TODO(stfinancial): Seems weird for these to be protected maybe.
    protected BlockingQueue<Long> timestampQueue; // Contains a queue of timestamps so we know when we are allowed to submit another request to the ExecutorService
    protected PriorityBlockingQueue<WorkItem> workQueue;
    private ExecutorService queueWorkers;

    protected RequestQueue(Market market, QueueStrategy strategy, int numWorkers) {
        this.market = market;
        this.strategy = strategy;
        // TODO(stfinancial): Look into LinkedTransferQueue
        // TODO(stfinancial): Do we want this to be blocking?
        this.timestampQueue = new LinkedBlockingQueue<>();
        this.workQueue = new PriorityBlockingQueue<>();
        this.queueWorkers = Executors.newFixedThreadPool(numWorkers);
        // TODO(stfinancial): Does the object ever finish construction? What are the consequences of passing an unconstructed object to the thread?
            // TODO(stfinancial): How do we handle the case where queue strategy is DISABLED and the thread has been started? Client needs to deal with this logic.
//        (new Thread(this)).run();
    }

    // TODO(stfinancial): Should this also take in a strategy, then implement some sorting with strategy in the workQueue.
    Future<MarketResponse> submitRequest(MarketRequest request) {
        // Construct the Callable WorkItem.
        WorkItem item = new WorkItem(market, request);
        if (strategy == QueueStrategy.DISABLED) {
            item.setRateBlocked(false);
            return queueWorkers.submit(item);
        }
        // Submit the item to the workers, the item will run when the request manager has said it can.
        Future<MarketResponse> response = queueWorkers.submit(item);
        // The WorkItem is now fully constructed and waiting until it is notified, so we can add it to the work queue.
        workQueue.add(item);
        // TODO(stfinancial): Consider using a lock to ensure that this is returned as soon as possible.
        return response;
    }
}
