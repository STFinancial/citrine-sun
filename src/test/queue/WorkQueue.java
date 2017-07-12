package test.queue;

import api.Market;
import api.request.MarketRequest;
import api.request.MarketResponse;

import java.util.concurrent.*;

/**
 * Created by Timothy on 7/9/17.
 */
public class WorkQueue {
    private PriorityBlockingQueue<WorkItem> items;
    private ExecutorService queueWorkers;
    private RequestManager requestManager;
    private final Market market;

    // TODO(stfinancial): Remove runnable once integrated with markets.
    WorkQueue(Market market) {
        queueWorkers = Executors.newFixedThreadPool(5);
        items = new PriorityBlockingQueue<>();
        this.market = market;
        requestManager = new RequestManager(items);
        // TODO(stfinancial): Do we need to call a join here at some point?
        new Thread(requestManager);
    }


    // TODO(stfinancial): Should this be a callable?
    Future<MarketResponse> submitWork(MarketRequest req) {
        // TODO(stfinancial): If this is running on the same thread as the Market, is there deadlock issues?

        // Construct the work item, which adds itself to the work queue.
        WorkItem item = new WorkItem(req, market);
        System.out.println("Constructed work item.");
        // Submit the item to the workers, the item will run when the request manager has said it can.
        Future<MarketResponse> response = queueWorkers.submit(item);
        System.out.println("Submitted work item to workers.");
        // The WorkItem is now fully constructed and waiting until it is notified, so we can add it to the work queue.
        items.add(item);
        // TODO(stfinancial): Consider using a lock to ensure that this is returned as soon as possible.
        return response;
    }

    private class RequestManager implements Runnable {
        // TODO(stfinancial): Should this take the form of a semaphore? That way we can notify the WorkItems that are waiting.
        // TODO(stfinancial): This seems like the right strategy.
        // TODO(stfinancial): Check the method acquire(int permits)... this will be perfect for kraken where the methods are worth a different amount.

        // What do we need to maintain here?
        // Need to maintain a queue of requests
        BlockingQueue<Long> timestampQueue; // Contains a queue of timestamps so we know when we are allowed to submit another request to the executorservice
        PriorityBlockingQueue<WorkItem> workQueue;

        RequestManager(PriorityBlockingQueue<WorkItem> workQueue) {
            // TODO(stfinancial): Look into LinkedTransferQueue
            // TODO(stfinancial): Do we want this to be blocking?
            timestampQueue = new LinkedBlockingQueue<>();
            this.workQueue = workQueue;
        }

//        void addElement(long timestamp) {
//            // TODO(stfinancial): With BlockingQueue this should be atomic?
//            timestampQueue.add(timestamp);
//        }

        @Override
        public void run() {
            while (true) {
                // TODO(stfinancial): Integrate QueueStrategy.
                // TODO(stfinancial): Embed this all in try catch for Interrupted Exception?
                // TODO(stfinancial): Need to think about whether this is safe. Can the workQueue/timestampQueue ever grow too large?
                if (timestampQueue.size() < 6) {
                    try {
                        System.out.println("About to take work item.");
                        WorkItem item = workQueue.take();
                        System.out.println("Obtained work item.");
                        timestampQueue.add(System.currentTimeMillis());
                        item.notify();
//                        // TODO(stfinancial): Does this actually need synchronized?
//                        synchronized (this) {
//                            // Getting the item, adding the timestamp, and notifying the work item must be atomic
//                            // TODO(stfinancial): Is this true?
//                            WorkItem item = workQueue.take();
//                            timestampQueue.add(System.currentTimeMillis());
//                            item.notify();
//                        }
//                        // TODO(stfinancial): ***** MUST DO *****: Take, notify, and adding timestamp must be an atomic operation.
//                        workQueue.take().notify();
                    } catch (InterruptedException e) {
                        System.out.println("RequestManager interrupted 1.");
                        continue;
                    }
                }
                if (timestampQueue.size() == 6) {
                    // Continue if the queue is not empty and the oldest item is older than 1 second.
                    while (!timestampQueue.isEmpty() && System.currentTimeMillis() - timestampQueue.peek() > 1000) {
                        timestampQueue.poll();
                    }
                }
                if (!timestampQueue.isEmpty()) {
                    try {
                        System.out.println("Sleeping...");
                        Thread.sleep(timestampQueue.peek() + 1000 - System.currentTimeMillis());
                    } catch (InterruptedException e) {
                        System.out.println("RequestManager interrupted 2.");
                        continue;
                    }
                }
            }
        }
    }

    // TODO(stfinancial): Need to review the thread safety of all the stuff here.
    private class WorkItem implements Callable<MarketResponse>, Comparable<WorkItem> {
        private final MarketRequest req;
        private final Market market;

        WorkItem(MarketRequest req, Market market) {
            this.req = req;
            // TODO(stfinancial): Should this be passed to the WorkItem or just accessed directly from the enclosing class?
            this.market = market;
        }

        @Override
        public MarketResponse call() throws Exception {
            // TODO(stfinancial): Should it add itself to the queue here?
//            queue.add(this);

            System.out.println("Waiting...");
            // Wait until we are told to submit by the RequestManager
            wait();
            // TODO(stfinancial): Do we need to catch InterruptedException here?
            System.out.println("Processing work item.");

            // TODO(stfinancial): Need to make sure that all of the stuff we're using in market is thread safe and does not modify instance variables.
            return market.processMarketRequest(req);

            // After we've been cleared to submit, we should give our timestamp to the request manager
            // TODO(stfinancial): Make this more accurate by having the market do this at the moment of the request (probably doesn't matter).
            // TODO(stfinancial): Is this thread safe?
//            manager.addElement(System.currentTimeMillis());
//            return new MarketResponse(NullNode.getInstance(), req, System.currentTimeMillis(), RequestStatus.success());
        }

        @Override
        public int compareTo(WorkItem o) {
            return req.compareTo(o.req);
        }
    }
}
