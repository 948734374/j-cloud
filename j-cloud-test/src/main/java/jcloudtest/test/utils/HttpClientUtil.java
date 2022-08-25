package jcloudtest.test.utils;


import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Component
public class HttpClientUtil {


    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public static List<String> list;

    public List<String> batchPost(String url, List<HashMap<String, Object>> params) throws InterruptedException, ExecutionException {
        list = Arrays.asList(new String[10]);
        CountDownLatch countDownLatch = new CountDownLatch(params.size());
        for (int i = 0; i < params.size(); i++) {

            Map<String, Object> param = params.get(i);
            param.put("index", i);
            threadPoolTaskExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String result = HttpClientPoolUtil.post(url, param);
                        list.set((Integer) param.get("index"), result);

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        countDownLatch.await();

        return list;
    }


    public List<String> batchPost1(String url, List<HashMap<String, Object>> params) throws InterruptedException, ExecutionException, IOException {
        ExecutorService executorService = new ThreadPoolExecutor(1, 20, 20, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));
        list = Arrays.asList(new String[params.size()]);

        ExecutorService service = TtlExecutors.getTtlExecutorService(executorService);

        InheritableThreadLocal<Integer> threadLocal = new TransmittableThreadLocal<>();
        threadLocal.set(0);

        CountDownLatch countDownLatch = new CountDownLatch(params.size());
        for (int i = 0; i < params.size(); i++) {

            Map<String, Object> param = params.get(i);



            Future<HashMap<String, String>> future = service.submit(new Callable<HashMap<String, String>>() {
                @Override
                public HashMap<String, String> call() throws Exception {

                    HashMap<String, String> resultMap = new HashMap<>();
                    String result;
                    try {
                        result = HttpClientPoolUtil.post(url, param);

                        synchronized (list){
                            list.set(threadLocal.get(),result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } finally {
                        countDownLatch.countDown();
                    }

                    threadLocal.remove();
                    return resultMap;
                }
            });


            System.out.println("子线程结束" + i);
//            System.out.println("子线程结束" + i + future.get().toString());
            threadLocal.set(threadLocal.get() + 1);
//
//            System.out.println("下标：" + future.get().get("1"));
//            System.out.println("值：" + future.get().get("2"));
//            list.set(Integer.parseInt(future.get().get("1")), future.get().get("2"));
        }
        countDownLatch.await();
        executorService.shutdown();
        HttpClientPoolUtil.close();
        return list;
    }

}