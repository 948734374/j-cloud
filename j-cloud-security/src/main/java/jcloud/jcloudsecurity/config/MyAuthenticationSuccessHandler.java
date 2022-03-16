package jcloud.jcloudsecurity.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private String url;

    public MyAuthenticationSuccessHandler(String url) {
        this.url = url;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //获取IP地址
        System.out.println(request.getRemoteAddr());
        //获取认证用户信息
        User user = (User) authentication.getPrincipal();
        System.out.println("=====" + user.getAuthorities());
        System.out.println(url);
        //重定向
        response.sendRedirect(url);
    }
}