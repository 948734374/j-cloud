package jcloudtest.test.utils;


import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;
import jcloudtest.test.entity.HttpResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Component
public class HttpClientUtil {


    private static ConcurrentHashMap<String, List<String>> resultMaps;
    private static ConcurrentHashMap<String, List<HttpResult>> resultMaps1;

    static {
        resultMaps = new ConcurrentHashMap<>();
        resultMaps1 = new ConcurrentHashMap<>();
    }


    public List<String> batchPost(String url, List<HashMap<String, Object>> params) throws InterruptedException, ExecutionException, IOException {
        ExecutorService executorService = new ThreadPoolExecutor(1, 20, 20, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));


        ExecutorService service = TtlExecutors.getTtlExecutorService(executorService);

        InheritableThreadLocal<Integer> index = new TransmittableThreadLocal<>();
        InheritableThreadLocal<String> parentThreadName = new TransmittableThreadLocal<>();
        index.set(0);
        String threadName = Thread.currentThread().getName();
        parentThreadName.set(threadName);

        resultMaps.put(threadName, Collections.synchronizedList(Arrays.asList(new String[params.size()])));

        CountDownLatch countDownLatch = new CountDownLatch(params.size());
        for (int i = 0; i < params.size(); i++) {

            Map<String, Object> param = params.get(i);


            service.submit(new Runnable() {
                @Override
                public void run() {

                    String result;
                    try {
                        result = HttpClientPoolUtil.post(url, param, parentThreadName.get());

//                        synchronized (resultMaps) {
                        resultMaps.get(parentThreadName.get()).set(index.get(), result);
//                        }
                    } catch (Exception e) {
                        System.out.println("请求出现异常");
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } finally {
                        countDownLatch.countDown();
                    }

                    index.remove();
                    parentThreadName.remove();

                }
            });

            index.set(index.get() + 1);

        }
        countDownLatch.await();
//        executorService.shutdown();
        service.shutdown();
        HttpClientPoolUtil.close(parentThreadName.get());

        List<String> httpResult;
//        synchronized (resultMaps) {
        httpResult = resultMaps.get(parentThreadName.get());
        resultMaps.remove(parentThreadName.get());
//        }
        parentThreadName.remove();
        index.remove();
        return httpResult;
    }


    public List<HttpResult> batchPost1(List<String> urls, List<String> params) throws InterruptedException, ExecutionException, IOException {
        ExecutorService executorService = new ThreadPoolExecutor(1, 20, 20, TimeUnit.SECONDS, new LinkedBlockingDeque<>(8));



        String threadName = Thread.currentThread().getName();

        resultMaps1.put(threadName, Collections.synchronizedList(Arrays.asList(new HttpResult[params.size()])));

        CountDownLatch countDownLatch = new CountDownLatch(params.size());
        for (int i = 0; i < params.size(); i++) {

            String param = params.get(i);

            String url = urls.get(i);

            int indexOf = i;

            executorService.submit(new Runnable() {
                @Override
                public void run() {

                    HttpResult result;
                    try {
                        result = UrlUtils.doPostWithCode(url, param);

                        resultMaps1.get(threadName).set(indexOf, result);
                    } catch (Exception e) {
                        System.out.println("请求出现异常");
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } finally {
                        countDownLatch.countDown();
                    }


                }
            });

        }
        countDownLatch.await();
        executorService.shutdown();

        List<HttpResult> httpResult;
        httpResult = resultMaps1.get(threadName);
        resultMaps1.remove(threadName);
        return httpResult;
    }
}