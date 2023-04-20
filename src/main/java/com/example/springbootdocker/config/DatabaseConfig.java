package com.example.springbootdocker.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableRetry
public class DatabaseConfig {

    // private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    //@Autowired
    //private DataSource dataSource;
    // ------------------------
    @Autowired
    private Environment env;

    @Lazy
    @Autowired
    private LocalContainerEntityManagerFactoryBean entityManagerFactory;

    /*
     * DataSource definition for database connection. Settings are read from the
     * application.properties file (using the env object).
     */
    @Bean(name = "datasource")
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(env.getProperty("oracleDb.driverClassName"));
        config.setJdbcUrl(env.getProperty("oracleDb.url"));
        config.setUsername(env.getProperty("oracleDb.username"));
        config.setPassword(env.getProperty("oracleDb.password"));

        config.setSchema(env.getProperty("oracleDb.schema"));
        config.setConnectionTestQuery(env.getProperty("oracleDb.validationQuery"));
        config.setMaximumPoolSize(Integer.parseInt(env.getProperty("oracleDb.maxpoolsize")));
        config.setMinimumIdle(Integer.parseInt(env.getProperty("oracleDb.minidle")));
        return new HikariDataSource(config);
    }


    /*
     * Declare the JPA entity manager factory.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactory = new LocalContainerEntityManagerFactoryBean();

        localContainerEntityManagerFactory.setDataSource(dataSource());

        // Classpath scanning of @Component, @Service, etc annotated class
        localContainerEntityManagerFactory.setPackagesToScan(env.getProperty("hibernate.entitymanager.packagesToScan"));

        // Vendor adapter
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        localContainerEntityManagerFactory.setJpaVendorAdapter(vendorAdapter);

        // Hibernate properties
        Properties additionalProperties = new Properties();
        additionalProperties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        additionalProperties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
        additionalProperties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        localContainerEntityManagerFactory.setJpaProperties(additionalProperties);

        return localContainerEntityManagerFactory;
    }

    /*
     * Declare the transaction manager.
     */
    @Bean
    public JpaTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

    /*
     * PersistenceExceptionTranslationPostProcessor is a bean post processor
     * which adds an advisor to any bean annotated with Repository so that any
     * platform-specific exceptions are caught and then rethrown as one Spring's
     * unchecked data access exceptions (i.e. a subclass of
     * DataAccessException).
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public HealthIndicator dbHealthIndicator() {

        DataSourceHealthIndicator indicator = new DataSourceHealthIndicator(dataSource());
        indicator.setQuery("SELECT 1 FROM DUAL");
        return indicator;
    }
} // class DatabaseConfig
