//package api.queue;
//
//import api.Market;
//import api.request.MarketRequest;
//import api.request.MarketResponse;
//
//import java.util.concurrent.*;
//
///**
// * Created by Timothy on 7/9/17.
// */
//public class WorkQueue {
//    // TODO(stfinancial): ****** NOTE ****** This design is slightly wrong, we likely need to use the response timestamps, rather than the time we send them.
//    private PriorityBlockingQueue<WorkItem> items;
//    private ExecutorService queueWorkers;
//    private RequestManager requestManager;
//    private final Market market;
//
//    // TODO(stfinancial): Remove runnable once integrated with markets.
//    WorkQueue(Market market) {
//        queueWorkers = Executors.newFixedThreadPool(5);
//        items = new PriorityBlockingQueue<>();
//        this.market = market;
//        requestManager = new RequestManager(items);
//        // TODO(stfinancial): Do we need to call a join here at some point?
//        (new Thread(requestManager)).start();
//    }
//
//    // TODO(stfinancial): Should this be a callable?
//    Future<MarketResponse> submitWork(MarketRequest req) {
//        // TODO(stfinancial): If this is running on the same thread as the Market, is there deadlock issues?
//
//        // Construct the work item, which adds itself to the work queue.
//        WorkItem item = new WorkItem(req, market);
////        System.out.println("Constructed work item.");
//        // Submit the item to the workers, the item will run when the request manager has said it can.
//        Future<MarketResponse> response = queueWorkers.submit(item);
////        System.out.println("Submitted work item to workers.");
//        // The WorkItem is now fully constructed and waiting until it is notified, so we can add it to the work queue.
//        items.add(item);
//        // TODO(stfinancial): Consider using a lock to ensure that this is returned as soon as possible.
//        return response;
//    }
//
//    private class RequestManager implements Runnable {
//        // TODO(stfinancial): Look into turning this into a semaphore. Check the method acquire(int permits)... this will be perfect for kraken where the methods are worth a different amount.
//
//        // What do we need to maintain here?
//        // Need to maintain a queue of requests
//        BlockingQueue<Long> timestampQueue; // Contains a queue of timestamps so we know when we are allowed to submit another request to the executorservice
//        PriorityBlockingQueue<WorkItem> workQueue;
//
//        RequestManager(PriorityBlockingQueue<WorkItem> workQueue) {
//            // TODO(stfinancial): Look into LinkedTransferQueue
//            // TODO(stfinancial): Do we want this to be blocking?
//            timestampQueue = new LinkedBlockingQueue<>();
//            this.workQueue = workQueue;
//        }
//
//        @Override
//        public void run() {
//            while (true) {
////                System.out.println("In RequestManager loop.");
//                // TODO(stfinancial): Integrate QueueStrategy.
//                // TODO(stfinancial): Embed this all in try catch for Interrupted Exception?
//                // TODO(stfinancial): Need to think about whether this is safe. Can the workQueue/timestampQueue ever grow too large?
//                if (timestampQueue.size() < 6) {
//                    try {
////                        System.out.println("About to take work item.");
//                        WorkItem item = workQueue.take();
////                        System.out.println("Obtained work item.");
//                        timestampQueue.add(System.currentTimeMillis());
//                        // TODO(stfinancial): How big should this block be?
////                        System.out.println("Notifying work item.");
//                        synchronized (item) {
//                            item.rateBlocked = false;
//                            item.notify();
//                        }
//                    } catch (InterruptedException e) {
//                        System.out.println("RequestManager interrupted 1.");
//                        continue;
//                    }
//                }
//                if (timestampQueue.size() == 6) {
//                    // Continue if the queue is not empty and the oldest item is older than 1 second.
//                    while (!timestampQueue.isEmpty() && System.currentTimeMillis() - timestampQueue.peek() > 1000) {
//                        timestampQueue.poll();
//                    }
//                }
//                if (!timestampQueue.isEmpty()) {
//                    long sleepTime = timestampQueue.peek() + 1000 - System.currentTimeMillis();
//                    System.out.println("Sleep time: " + sleepTime);
//                    if (sleepTime > 0) {
//                        try {
//                            Thread.sleep(sleepTime);
//                        } catch (InterruptedException e) {
//                            System.out.println("RequestManager interrupted 2.");
//                            continue;
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    // TODO(stfinancial): Need to review the thread safety of all the stuff here.
//    private class WorkItem implements Callable<MarketResponse>, Comparable<WorkItem> {
//        private final MarketRequest req;
//        private final Market market;
//        private boolean rateBlocked = true;
//
//        WorkItem(MarketRequest req, Market market) {
//            this.req = req;
//            // TODO(stfinancial): Should this be passed to the WorkItem or just accessed directly from the enclosing class?
//            this.market = market;
//        }
//
//        @Override
//        public MarketResponse call() throws Exception {
//            // TODO(stfinancial): Potentially should place the timestamp in the queue here. Either before or after response is complete. (After in case of high latency... maybe)
//
//            System.out.println("Waiting...");
//            // Wait until we are told to submit by the RequestManager
//            synchronized (this) {
//                while (rateBlocked) {
//                    wait();
//                }
//            }
//            // TODO(stfinancial): Need to make sure that all of the stuff we're using in market is thread safe and does not modify instance variables.
//            return market.processMarketRequest(req);
//            // TODO(stfinancial): Maybe add completion timestamp here.
//        }
//
//        @Override
//        public int compareTo(WorkItem o) {
//            return req.compareTo(o.req);
//        }
//    }
//}
