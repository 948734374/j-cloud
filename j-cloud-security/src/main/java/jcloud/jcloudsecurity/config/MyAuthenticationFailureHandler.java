package jcloud.jcloudsecurity.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.WriteAbortedException;


public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final String url;

    public MyAuthenticationFailureHandler(String url) {
        this.url = url;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION,exception);
        //重定向
        response.sendRedirect(url);
    }
}