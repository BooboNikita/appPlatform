package com.app.appplatform;

import com.app.appplatform.dto.AppModuleDto;
import com.app.appplatform.service.AppModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AppPlatformApplicationTests {

    private HttpHeaders headers;

    @Autowired
    private AppModuleService appModuleService;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
//        when(cacheManager.getCache(anyString())).thenReturn(cache);
    }

    @Test
    void contextLoads() {

    }

    @Test
    public void testBcryptPasswordEncoder() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("password123");
        System.out.println(encodedPassword);
    }

//    @Test
//    public void testCheckHelperModule() {
//        String deviceInfo = "{deviceId: 362DD777-F566-4906-82B1-24827726702B, brand: iPhone}";
//        headers.add("deviceInfo", deviceInfo);
//
//        AppModuleDto appModuleDto = appModuleService.checkHelperModuleExists("apptest", headers);
//        System.out.println(appModuleDto);
//
//        appModuleDto = appModuleService.checkHelperModuleExists("apptest1", headers);
//        System.out.println(appModuleDto);
//    }
}
