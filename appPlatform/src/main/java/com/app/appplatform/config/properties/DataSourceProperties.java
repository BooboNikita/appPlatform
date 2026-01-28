package com.app.appplatform.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据源配置属性类
 */
@Component
public class DataSourceProperties {

    /**
     * 主数据源配置
     */
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public static class PrimaryDataSourceProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;

        // Getters and Setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getDriverClassName() { return driverClassName; }
        public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    }

    /**
     * 副数据源配置
     */
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public static class SecondaryDataSourceProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;

        // Getters and Setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getDriverClassName() { return driverClassName; }
        public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    }
}
