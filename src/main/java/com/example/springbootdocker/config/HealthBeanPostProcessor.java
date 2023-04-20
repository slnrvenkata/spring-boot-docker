package com.example.springbootdocker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/*
 * Spring Boot automatically creates a {@link DataSourceHealthIndicator} if
 * actuators are on the classpath. Unfortunately the SQL it uses to test that
 * the DataSource is available is (@code SELECT 1} which is not valid SQL for
 * OracleDB.
 * <p>
 * Spring Bean Post Processors are invoked after each bean is created and any
 * auto-wired methods (typically setters) have been called.
 * <p>
 * This post-processor waits for a {@code DataSourceHealthIndicator} to be
 * passed to it and overrides the query with a new one.
 *
 * @author Paul Chapman
 */
@Component
public class HealthBeanPostProcessor implements BeanPostProcessor {

    /*
     * Default query to use: <tt>{@value}</tt>.
     */
    public static final String DEFAULT_QUERY = "SELECT 1 FROM DUAL";
    private final String validationQuery;
    private Logger logger = LoggerFactory.getLogger(HealthBeanPostProcessor.class);
    public static final String PROP_dbHealthCheckRetryMaxAttempts = "${retry.healthcheck.db.maxattempts:1}";
	
    @Value(PROP_dbHealthCheckRetryMaxAttempts)
    private Integer maxRetriesProperty; 
    
    @Autowired
    private DataSource dataSource;
    
    /*
     * Create an instance that uses the {@link #DEFAULT_QUERY}.
     */
    public HealthBeanPostProcessor() {
        this.validationQuery = DEFAULT_QUERY;
    }

    /*
     * Create an instance that uses the query provided.
     *
     * @param validationQuery The query to use. It must be a SELECT returning a
     *                        single value.
     */
    public HealthBeanPostProcessor(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    /*
     * Does the work. If {@code bean} is an instance of
     * {@link DataSourceHealthIndicator} it sets a new validation query.
     * <p>
     * <b>From parent class:</b>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        String simpleName = bean.getClass().getSimpleName().toLowerCase();
        // Debugging only - show all health related beans as they come past
        if (beanName.toLowerCase().contains("health") || simpleName.contains("health") || simpleName.contains("oracledb"))
            logger.warn("Processing {}  <-  {} ", beanName, bean.getClass());

        synchronized (this) {
            // 1. Replace Spring Boot's health indicator with ours
            // 2. Override the default query for the DataSourceHealthIndicator to one that
            // works with OracleDB.
            if (bean instanceof DataSourceHealthIndicator) {
                logger.info("Found a {}", bean.getClass());
                DataSourceHealthIndicator dataSourceHealthIndicator = (DataSourceHealthIndicator) bean;

                if (!(bean instanceof RetryingDataSourceHealthIndicator)) {
                    logger.info("Switching health ind: {}  with ours", bean.getClass());
                    bean = new RetryingDataSourceHealthIndicator(dataSource, validationQuery,maxRetriesProperty);
                }

                dataSourceHealthIndicator.setQuery(validationQuery);

                // Log it
                logger.info("Set validation-query for DataSourceHealthIndicator: {}", //
                        dataSourceHealthIndicator.getQuery());
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
