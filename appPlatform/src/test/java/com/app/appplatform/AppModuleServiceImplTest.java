package com.app.appplatform;

import com.app.appplatform.dto.AppModuleDto;
import com.app.appplatform.entity.AppModule;
import com.app.appplatform.mapper.AppModuleMapper;
import com.app.appplatform.service.AppModuleService;
import com.app.appplatform.service.impl.AppModuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppModuleServiceImplTest {

    @Mock
    private AppModuleMapper appModuleMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private AppModuleServiceImpl moduleService;

    private HttpHeaders headers;

    @Autowired
    private AppModuleService appModuleService;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
//        when(cacheManager.getCache(anyString())).thenReturn(cache);
    }

    @Test
    void getActiveModules_WithHuaweiDevice_ShouldShowAllModules() {
        // Arrange
        String username = "testuser";
        String deviceInfo = "{deviceId: 362DD777-F566-4906-82B1-24827726702B, brand: iPhone}";
        headers.add("deviceInfo", deviceInfo);

        List<AppModule> mockModules = new ArrayList<>();
        mockModules.add(createMockModule(1L, "Module 1", true));
        mockModules.add(createMockModule(2L, "Module 2", false));

        when(appModuleMapper.findAllActive()).thenReturn(mockModules);

        // Act
        List<AppModuleDto> result = moduleService.getActiveModules(username, headers);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(appModuleMapper).findAllActive();
        verify(appModuleMapper, never()).findAllActiveAndHideForTest();
    }

    @Test
    void getActiveModules_WithTestUser_ShouldHideTestModules() {
        // Arrange
        String username = "apptest";
        String deviceInfo = "{deviceId: TKQ1.221114.001, brand: Redmi}";
        headers.add("deviceInfo", deviceInfo);

        List<AppModule> mockModules = new ArrayList<>();
        mockModules.add(createMockModule(1L, "Module 1", false));
        mockModules.add(createMockModule(2L, "Test Module", false));

        when(appModuleMapper.findAllActiveAndHideForTest()).thenReturn(mockModules);

        // Act
        List<AppModuleDto> result = moduleService.getActiveModules(username, headers);

        // Assert
        assertNotNull(result);
        assertEquals(mockModules.size(), result.size());
        assertEquals("Module 1", result.get(0).getTitle());
        verify(appModuleMapper).findAllActiveAndHideForTest();
        verify(appModuleMapper, never()).findAllActive();
    }

    @Test
    void getActiveModules_WithInvalidDeviceInfo_ShouldNotFail() {
        // Arrange
        String username = "testuser";
        String invalidDeviceInfo = "invalid-json";
        headers.add("deviceInfo", invalidDeviceInfo);

        List<AppModule> mockModules = new ArrayList<>();
        mockModules.add(createMockModule(1L, "Module 1", false));
        when(appModuleMapper.findAllActive()).thenReturn(mockModules);

        // Act
        List<AppModuleDto> result = moduleService.getActiveModules(username, headers);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appModuleMapper).findAllActive();
    }

    @Test
    void getActiveModules_WithNullDeviceInfo_ShouldNotFail() {
        // Arrange
        String username = "testuser";
        // deviceInfo is null
        List<AppModule> mockModules = new ArrayList<>();
        mockModules.add(createMockModule(1L, "Module 1", false));
        when(appModuleMapper.findAllActive()).thenReturn(mockModules);

        // Act
        List<AppModuleDto> result = moduleService.getActiveModules(username, headers);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appModuleMapper).findAllActive();
    }

//    @Test
//    void testCachingBehavior() {
//        // 第一次调用应该会执行方法
//        String username = "apptest";
//        String deviceInfo = "{brand: huawei}";
//        headers.add("deviceInfo", deviceInfo);
//
//        List<AppModuleDto> firstCall = appModuleService.getActiveModules(username, headers);
//
//        // 验证缓存中是否有数据
//        Cache cache = cacheManager.getCache("appModules");
//        if (cache != null) {
//            System.out.println(cache.get("appModules"));
//        }
////        assertNotNull(cache.get("appModules"));
//
//        // 第二次调用应该从缓存获取
//        String deviceInfo2 = "{brand: xiaomi}";
//        headers.clear();
//        headers.add("deviceInfo", deviceInfo2);
//        List<AppModuleDto> secondCall = appModuleService.getActiveModules(username, headers);
//
//        System.out.println(firstCall);
//        System.out.println(secondCall);
//
//        // 验证两次返回的是同一个对象（引用相等）
////        assertSame(firstCall, secondCall);
//    }

    private AppModule createMockModule(Long id, String name, boolean hideForTest) {
        AppModule module = new AppModule();
        module.setId(id);
        module.setTitle(name);
        module.setHideForTest(hideForTest);
        module.setIsActive(true);
        return module;
    }
}
