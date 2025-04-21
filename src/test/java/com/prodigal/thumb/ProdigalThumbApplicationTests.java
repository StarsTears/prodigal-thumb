package com.prodigal.thumb;

import com.prodigal.thumb.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProdigalThumbApplicationTests {
    @Resource
    private UserService userService;

    @Test
    void contextLoads() {

    }
    @Test
    void testPasswordEncrypt(){
        String encryptPassword = userService.getEncryptPassword("123456");
        System.out.println(encryptPassword);
    }

}
