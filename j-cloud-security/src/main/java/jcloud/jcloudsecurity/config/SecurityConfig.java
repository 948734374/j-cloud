package jcloud.jcloudsecurity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private MyAccessDeniedHandler myAccessDeniedHandler;

    /**
     * 指定密码加密的方法     *     * @return
     */
    @Bean
    public BCryptPasswordEncoder getPasswordEncode() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //表单提交
        http.formLogin()
                //自定义用户名和密码参数
                .usernameParameter("user")
                .passwordParameter("pass")
                //自定义登录页面
                .loginPage("/login.html")
                //必须和表单提交的接口一样，执行自定义登录逻辑
                .loginProcessingUrl("/login")
                //必须是post请求
//              .successForwardUrl("/toMain")
//              .failureForwardUrl("/toError");
                //自定义登录成功处理器
                //与successForwardUrl不共存，下同
                .successHandler(new MyAuthenticationSuccessHandler("/main.html"))
//                .successHandler(new MyAuthenticationSuccessHandler("http://www.baidu.com"))
                //自定义登录失败处理器
                .failureHandler(new MyAuthenticationFailureHandler("/error.html"));
        //授权
        http.authorizeRequests()
                //放行/login.html,不需要认证
                .antMatchers("/login.html").permitAll()
                //放行/error.html，不需要认证
                .antMatchers("/error.html").permitAll()
                //基于权限判断
                .antMatchers("/main1.html").hasAuthority("admin")
                .antMatchers("/main1.html").hasAnyAuthority("admin","admin1")
                //所有请求必须认证
                .anyRequest().authenticated();
        //异常处理器
        http.exceptionHandling().accessDeniedHandler(myAccessDeniedHandler);
        //关闭csrf防护
        http.csrf().disable();
    }

    /**
     * 放行静态资源,css,js,images     *      * @param web     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**")
                .antMatchers("/**/*.png");
    }
}
