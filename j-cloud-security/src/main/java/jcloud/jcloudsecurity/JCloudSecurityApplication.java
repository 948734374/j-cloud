package jcloud.jcloudsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableGlobalMethodSecurity(securedEnabled = true)
public class JCloudSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(JCloudSecurityApplication.class, args);
    }

}
