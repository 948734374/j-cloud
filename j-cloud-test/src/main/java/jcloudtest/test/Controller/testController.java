package jcloudtest.test.Controller;

import jcloudtest.test.Service.testService;
import jcloudtest.test.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/aaa")
public class testController {

    @Autowired
    private testService testService;

    @GetMapping("/test")
    @ResponseBody
    public List<SysUser> test() {
        return testService.find();
    }
}
