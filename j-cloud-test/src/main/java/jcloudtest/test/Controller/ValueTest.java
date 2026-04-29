package jcloudtest.test.Controller;

import org.springframework.beans.factory.annotation.Value;

public class ValueTest {

    @Value("aa")
    public void setAa(String aa) {
        this.aa = aa;
    }

    private String aa;

}
