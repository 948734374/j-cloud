package jcloudtest.test.utils;


import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * HttpClient工具类
 *
 * @author SHANHY
 * @return
 * @create 2015年12月18日
 */
public class HttpClientPoolUtil {

    private static final int timeOut = 10000000;

    private static ConcurrentHashMap<String, CloseableHttpClient> httpClients;
    private static ConcurrentHashMap<String, PoolingHttpClientConnectionManager> clientConnectionManagers;
    private static ScheduledExecutorService service;

    static {
        httpClients = new ConcurrentHashMap<>();
        clientConnectionManagers = new ConcurrentHashMap<>();
        closeExpiredConnectionsPeriodTask();
    }


    private final static Object syncLock = new Object();

    private static void config(HttpRequestBase httpRequestBase) {
        // 设置Header等
//        httpRequestBase.setHeader("User-Agent", "Mozilla/5.0");
        httpRequestBase
                .setHeader("Accept",
                        "*/*");
        httpRequestBase.setHeader("Accept-Language",
                "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");// "en-US,en;q=0.5");
        httpRequestBase.setHeader("Accept-Charset",
                "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");
        httpRequestBase.setHeader("Content-Type",
                "application/json");

        // 配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeOut)
                .setConnectTimeout(timeOut).setSocketTimeout(timeOut).build();
        httpRequestBase.setConfig(requestConfig);
    }

    /**
     * 获取HttpClient对象
     *
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static Map<String, CloseableHttpClient> getHttpClient(String url, String threadName) {
        String hostname = url.split("/")[2];
        int port = 80;
        if (hostname.contains(":")) {
            String[] arr = hostname.split(":");
            hostname = arr[0];
            port = Integer.parseInt(arr[1]);
        }
        synchronized (syncLock) {
            if (httpClients.get(threadName) == null) {
                CloseableHttpClient httpClient = createHttpClient(200, 40, 100, hostname, port, threadName);
                httpClients.put(threadName, httpClient);
            }
        }


        return httpClients;
    }

    /**
     * 创建HttpClient对象
     *
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static CloseableHttpClient createHttpClient(int maxTotal,
                                                       int maxPerRoute, int maxRoute, String hostname, int port, String threadName) {

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // 将最大连接数增加
        cm.setMaxTotal(maxTotal);
        // 将每个路由基础的连接增加
        cm.setDefaultMaxPerRoute(maxPerRoute);
        HttpHost httpHost = new HttpHost(hostname, port);
        // 将目标主机的最大连接数增加
        cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

        // 请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception,
                                        int executionCount, HttpContext context) {
                if (executionCount < 5) {// 如果已经重试了5次，就放弃
                    return true;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                    return false;
                }
                if (exception instanceof SSLException) {// SSL握手异常
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext
                        .adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setRetryHandler(httpRequestRetryHandler).build();

//        synchronized (httpClients) {
            clientConnectionManagers.put(threadName, cm);
//        }

        return httpClient;
    }

    private static void setPostParams(HttpPost httpost,
                                      Map<String, Object> params) {

        JSONObject jsonObject = new JSONObject();
        // 通过map集成entrySet方法获取entity
        Set<Map.Entry<String, Object>> entrySet = params.entrySet();
        // 循环遍历，获取迭代器
        for (Map.Entry<String, Object> mapEntry : entrySet) {
            try {
                jsonObject.put(mapEntry.getKey(), mapEntry.getValue());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        // 为httpPost设置封装好的请求参数
        try {
            httpost.setEntity(new StringEntity(jsonObject.toString()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * GET请求URL获取内容
     *
     * @param url
     * @return
     * @throws IOException
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static String post(String url, Map<String, Object> params, String threadName) throws IOException {
        HttpPost httppost = new HttpPost(url);
        config(httppost);
        setPostParams(httppost, params);
        CloseableHttpResponse response = null;
        HttpEntity entity;
        String result;
        try {
            response = getHttpClient(url, threadName).get(threadName).execute(httppost,
                    HttpClientContext.create());
            entity = response.getEntity();
            result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);

        } catch (Exception e) {

            result = "";
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void close(String threadName) {
        try {
            httpClients.get(threadName).close();
            clientConnectionManagers.get(threadName).close();
            httpClients.remove(threadName);
            clientConnectionManagers.remove(threadName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private static void closeExpiredConnectionsPeriodTask() {

        service = Executors.newSingleThreadScheduledExecutor();

        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                for (Map.Entry<String, PoolingHttpClientConnectionManager> entry : clientConnectionManagers.entrySet()) {

                    try {
                        System.out.println(entry.getKey() + "开始清理过期连接" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        entry.getValue().closeExpiredConnections();
                        entry.getValue().closeIdleConnections(30, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }, 5, 60, TimeUnit.SECONDS);

    }


}

