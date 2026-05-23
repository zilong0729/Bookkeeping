package com.bookkeeping.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * 数据库连接池优化配置 - 支持高并发
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        // 基础配置
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        // 连接池大小配置（根据服务器配置调整）
        dataSource.setInitialSize(10);
        dataSource.setMinIdle(10);
        dataSource.setMaxActive(200);  // 增加最大连接数
        dataSource.setMaxWait(60000);

        // 连接获取配置
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setMaxEvictableIdleTimeMillis(900000);

        // 连接有效性检测
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);  // 性能优化：获取连接时不检测
        dataSource.setTestOnReturn(false);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setValidationQueryTimeout(1);

        // 连接泄漏检测
        dataSource.setRemoveAbandoned(true);
        dataSource.setRemoveAbandonedTimeout(180);
        dataSource.setLogAbandoned(true);

        // PreparedStatement缓存
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(50);

        // 连接属性
        dataSource.setConnectionProperties("druid.stat.mergeSql=true;druid.stat.slowSqlMillis=3000");

        // 异步初始化
        dataSource.setAsyncInit(true);

        try {
            dataSource.init();
            log.info("Druid连接池初始化成功: maxActive={}", dataSource.getMaxActive());
        } catch (SQLException e) {
            log.error("Druid连接池初始化失败", e);
            throw new RuntimeException("数据库连接池初始化失败", e);
        }

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionManager.setDefaultTimeout(30);
        return transactionManager;
    }
}
