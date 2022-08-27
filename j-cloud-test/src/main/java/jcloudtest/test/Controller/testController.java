package jcloudtest.test.Controller;

import com.alibaba.ttl.TransmittableThreadLocal;
import jcloudtest.test.Service.testService;
import jcloudtest.test.entity.SysUser;
import jcloudtest.test.entity.TestSleep;
import jcloudtest.test.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/aaa")
public class testController {

    @Autowired
    private testService testService;

    private static int anInt = 0;

    public static final Object obj = new Object();

    @Autowired
    HttpClientUtil httpClientUtil;


    private static ThreadLocal tl = new TransmittableThreadLocal<>(); // 这里采用TTL的实现

    @GetMapping("/test")
    @ResponseBody
    public List<SysUser> test() {
        return testService.find();
    }

    @GetMapping("/testBatch")
    @ResponseBody
    public void testBatch() {

        List<HashMap<String, Object>> list = new ArrayList<>();
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("id", 1);
//        map.put("name", "2");
//        map.put("password", "3");
//
//        list.add(map);
//        HashMap<String, Object> map1 = new HashMap<>();
//        map1.put("id", 2);
//        map1.put("name", "2");
//        map1.put("password", "3");
//        list.add(map1);
//        HashMap<String, Object> map2 = new HashMap<>();
//        map2.put("id", 3);
//        map2.put("name", "2");
//        map2.put("password", "3");
//        list.add(map2);
//        HashMap<String, Object> map3 = new HashMap<>();
//        map3.put("id", 4);
//        map3.put("name", "2");
//        map3.put("password", "3");
//        list.add(map3);
//        HashMap<String, Object> map4 = new HashMap<>();
//        map4.put("id", 5);
//        map4.put("name", "2");
//        map4.put("password", "3");
//        list.add(map4);

        for (int i = 0; i < 100; i++) {
            HashMap<String, Object> tem = new HashMap<>();

            tem.put("id", i);
            tem.put("name", "");
            tem.put("password", "");

            list.add(tem);
        }

        List<String> strings = null;
        try {
            strings = httpClientUtil.batchPost("http://localhost:11111/aaa/testSleep", list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("testBatch结果："+strings.toString());

    }


    @GetMapping("/testBatch1")
    @ResponseBody
    public void testBatch1() {

        List<HashMap<String, Object>> list = new ArrayList<>();


        for (int i = 0; i < 100; i++) {
            HashMap<String, Object> tem = new HashMap<>();

            tem.put("index", "index"+i);
            list.add(tem);
        }

        List<String> strings = null;
        try {
            strings = httpClientUtil.batchPost("http://localhost:11111/aaa/testSleep1", list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("testBatch1结果："+strings.toString());

    }

    @PostMapping("/testSleep")
    @ResponseBody
    public SysUser testSleep(@RequestBody SysUser sysUser) throws InterruptedException {

        long a = new Random().nextInt(20000);
        System.out.println(Thread.currentThread().getName() + "开始沉睡");

        Thread.sleep(a);
        System.out.println(Thread.currentThread().getName() + "结束沉睡，时间" + a);

        return sysUser;
    }

    @PostMapping("/testSleep1")
    @ResponseBody
    public TestSleep testSleep1(@RequestBody TestSleep testSleep) throws InterruptedException {

        long a = new Random().nextInt(20000);
        System.out.println(Thread.currentThread().getName() + "开始沉睡");

        Thread.sleep(a);
        System.out.println(Thread.currentThread().getName() + "结束沉睡，时间" + a);

        return testSleep;
    }
}
