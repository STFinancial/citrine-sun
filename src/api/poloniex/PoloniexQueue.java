package api.poloniex;

import api.QueueStrategy;
import api.WorkItem;
import api.request.MarketRequest;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by Timothy on 11/27/16.
 */
class PoloniexQueue implements Runnable {
    // Number of requests per second as limited by the API.
    private static final int REQUESTS_PER_SECOND = 6;

//    private int requestsInLastSecond = 0;

    PriorityBlockingQueue<WorkItem> requestQueue;
    private QueueStrategy strategy;
    private long timeout;
    private ExecutorService workers;

    Queue<WorkItem> previousRequests;

    PoloniexQueue(QueueStrategy strategy, long timeout) {
        this.strategy = strategy;
        this.timeout = timeout;
        requestQueue = new PriorityBlockingQueue<>();
        previousRequests = new LinkedList<>();
        workers = Executors.newFixedThreadPool(12); // We will say 2 seconds for now, later use the timeout value to find a good number.
//        workers = Executors.newFixedThreadPool((int) (REQUESTS_PER_SECOND / (timeout / 1000)));
    }

//    Future<? extends MarketResponse> process(MarketRequest request) {
//        workers.submit()
//    }

    void addWorkItem(WorkItem item) {
        item.setTimestamp(System.currentTimeMillis());
        requestQueue.add(item);
    }

    @Override
    public void run() {
        while (true) {
            long time = System.currentTimeMillis();
            WorkItem oldestRequest = previousRequests.peek();
            // Get rid of requests older than 1 second.
            while (oldestRequest != null && oldestRequest.getTimestamp() + 1000 < time) {
                previousRequests.poll();
                oldestRequest = previousRequests.peek();
            }

            WorkItem newItem;
            while (previousRequests.size() < 6) {
                try {
                    newItem = requestQueue.take();
                } catch (InterruptedException e) {
                    // TODO(stfinancial): How slow is this try catch?
                    break;
                }
                newItem.setWork(workers.submit(newItem)); // How do I return the future here...
                previousRequests.add(newItem);
            }
        }
    }

//    void setStrategy(QueueStrategy strategy) {
//        this.strategy = strategy;
//    }
//
//    QueueStrategy getStrategy() {
//        return strategy;
//    }
//
//    void setTimeout(long timeout) {
//        // Change thread pool size?
//        this.timeout = timeout;
//    }
//
//    long getTimeout() {
//        return timeout;
//    }


}
