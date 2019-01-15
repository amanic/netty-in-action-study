package zkLockTest;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.CountDownLatch;

public class ASimpleTest {

    private Integer sum = 0;


    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    public void doAdd(){
        this.sum++;
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(10);
        final ASimpleTest aSimpleTest = new ASimpleTest();



        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

                    CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.94.169:2181", retryPolicy);

                    client.start();

                    final InterProcessMutex mutex = new InterProcessMutex(client, "/curator/lock");
                    for (int i = 0; i < 1000; i++) {
                        try {
                            mutex.acquire();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        aSimpleTest.doAdd();
                        try {
                            mutex.release();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    client.close();
                }
            }).start();
            countDownLatch.countDown();
        }
        countDownLatch.await();
        System.out.println(aSimpleTest.getSum());
    }
}
