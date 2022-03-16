package jcloud.jcloudsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=======执行自定义登录逻辑====");
        //校验用户名，实际环境中需要从数据库查询
        if (!username.equals("admin")) {
            throw new UsernameNotFoundException("用户不存在");
        }
        //比较密码，实际需要从数据库取出原密码校验，框架会自动读取登录页面的密码
        String password = bCryptPasswordEncoder.encode("123456");
        //返回UserDetails，实际开发中可拓展UserDetails
        return new User(username, password,
                //自定义权限
                AuthorityUtils.commaSeparatedStringToAuthorityList("permission1"));
    }
}
