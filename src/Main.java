import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Main {

    private static final int bufferSize = 10;
    private static int allProducts = 0;
    private static int allConsume = 0;
    private static ArrayList list = new ArrayList(bufferSize);
    private static Semaphore n = new Semaphore(0);
    private static Semaphore mutex = new Semaphore(1);
    private static Semaphore e = new Semaphore(bufferSize);

    static class Producer implements Runnable {
        @Override
        public void run() {
            try {
                while (n.availablePermits() != bufferSize ) {
                    Thread.sleep(3000);
                    e.acquire();
                    mutex.acquire();
                    if (allProducts >= bufferSize) {
                        mutex.release();
                        n.release(1);
                        break;
                    }
                    System.out.println("Producing");
                    System.out.println("Produced item: " + allProducts);
                    list.add(allProducts++);
                    System.out.println("Number of products: "+(allProducts - allConsume)+"\n");
                    mutex.release();
                    n.release(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Consumer implements Runnable {
        String id;
        public Consumer(String id) {
            this.id = id;
        }
        @Override
        public void run() {
            try {
                while (e.availablePermits() != 0) {
                    Thread.sleep(8000);
                    if (allConsume >= bufferSize){
                        break;
                    }
                    n.acquire(1);
                    mutex.acquire();
                    allConsume++;
                    System.out.println("Consuming" );
                    if(allConsume < bufferSize || !list.isEmpty()){
                        System.out.println("Consumer \"" + id + "\" consume: " + list.remove(0));
                    }
                    else {
                        System.out.println("Consumer \"" + id + "\" : There is no Product");
                        break;
                    }
                    System.out.println("Number of products: "+(allProducts - allConsume)+"\n");
                    mutex.release();
                    e.release();
                }
                System.out.println("Consumer \"" + id + "\" : There is no Product");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String [] args) {
        ExecutorService cachedPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cachedPool.execute(new Producer());
            cachedPool.execute(new Consumer(""+i+""));
        }
        cachedPool.shutdown();
    }
}
