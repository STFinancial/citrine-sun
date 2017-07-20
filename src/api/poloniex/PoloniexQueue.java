package api.poloniex;

import api.Market;
import api.QueueStrategy;
import api.queue.RequestQueue;
import api.queue.WorkItem;

final class PoloniexQueue extends RequestQueue {
    protected PoloniexQueue(Market market, QueueStrategy strategy, int numWorkers) {
        super(market, strategy, numWorkers);
    }

    @Override
    public void run() {
        // TODO(stfinancial): We are assuming strategy is STRICT for now.
        while (true) {
//                System.out.println("In PoloniexQueue loop.");
            // TODO(stfinancial): Embed this all in try catch for Interrupted Exception?
            // TODO(stfinancial): Need to think about whether this is safe. Can the workQueue/timestampQueue ever grow too large?
            if (timestampQueue.size() < 6) {
                try {
//                        System.out.println("About to take work item.");
                    WorkItem item = workQueue.take();
//                        System.out.println("Obtained work item.");
                    timestampQueue.add(System.currentTimeMillis());
                    // TODO(stfinancial): How big should this block be?
//                        System.out.println("Notifying work item.");
                    synchronized (item) {
                        item.setRateBlocked(false);
                        item.notify();
                    }
                } catch (InterruptedException e) {
                    System.out.println("PoloniexQueue interrupted 1.");
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
                long sleepTime = timestampQueue.peek() + 1000 - System.currentTimeMillis();
                System.out.println("Sleep time: " + sleepTime);
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        System.out.println("PoloniexQueue interrupted 2.");
                        continue;
                    }
                }
            }
        }
    }
}