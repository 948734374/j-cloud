package jcloud.jcloudsecurity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class demo {

    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "恭喜你登录成功";
    }

    @RequestMapping("/showLogin")
    public String login() {
//        return "/templates/login.html";
        return "login.html";
    }
}
