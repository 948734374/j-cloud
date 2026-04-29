package jcloud.jcloudsecurity.config;

import jcloud.jcloudsecurity.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private MyAccessDeniedHandler myAccessDeniedHandler;
    @Autowired
    private UserDetailServiceImpl userDetailService;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

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
                .usernameParameter("user").passwordParameter("pass")
                //自定义登录页面
                .loginPage("/login.html")
                //必须和表单提交的接口一样，执行自定义登录逻辑
                .loginProcessingUrl("/login")
                //必须是post请求
//                .successForwardUrl("/toMain")
//              .failureForwardUrl("/toError");
                //自定义登录成功处理器
                //与successForwardUrl不共存，下同
                .successHandler(new MyAuthenticationSuccessHandler("/main.html"))
//                .successHandler(new MyAuthenticationSuccessHandler("http://www.baidu.com"))
                //自定义登录失败处理器
                .failureHandler(new MyAuthenticationFailureHandler("/error.html"));
        //授权
        //也可以加http方法
        http.authorizeRequests()
                //放行/login.html,不需要认证
                .antMatchers("/login.html").permitAll()
                //放行/error.html，不需要认证
                .antMatchers("/error.html").permitAll()
                //基于权限判断
//                .antMatchers("/main.html").hasAuthority("admin")
//                .antMatchers(HttpMethod.POST, "/main1.html").hasAnyAuthority("ROLE_1", "ROLE_2").antMatchers("/main1.html").hasRole("abc")
//                ip限定
//                .antMatchers("/main1.html").hasIpAddress("127.0.0.1")
//                .antMatchers("/main2.html").denyAll()
//                所有请求必须认证
//                .anyRequest().authenticated();
                .anyRequest().access("@myserviceImpl.hasPermission(request,authentication)");
        //异常处理器
        http.exceptionHandling().accessDeniedHandler(myAccessDeniedHandler);
        //关闭csrf防护
        http.csrf().disable();

        //浏览器关闭或者服务器重启不需要重新登录
        http.rememberMe()
                //失效时间
                .tokenValiditySeconds(10)
                //自定义登陆逻辑
                .userDetailsService(userDetailService)
                //持久层对象
                .tokenRepository(persistentTokenRepository);

        http.logout()
                .logoutSuccessUrl("/login.html");
    }

    /**
     * 放行静态资源,css,js,images     *      * @param web     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**").antMatchers("/**/*.png");
    }


    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();

//        jdbcTokenRepository.setCreateTableOnStartup(true);
        jdbcTokenRepository.setDataSource(dataSource);

        return jdbcTokenRepository;
    }

}
