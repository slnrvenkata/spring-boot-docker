package com.example.springbootdocker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.SQLTransientConnectionException;
import java.util.concurrent.atomic.AtomicLong;

public class RetryingDataSourceHealthIndicator extends DataSourceHealthIndicator {
    public static final int DEFAULT_MAX_RETRIES = 1;
    static private Logger logger = LoggerFactory.getLogger(RetryingDataSourceHealthIndicator.class);
    
    private int maxRetries;
    
    private AtomicLong totalRetries = new AtomicLong(0L);

    public RetryingDataSourceHealthIndicator(DataSource dataSource, String query, Integer maxRetriesProperty) {
        super(dataSource, query);
        this.maxRetries= maxRetriesProperty;
        logger.warn("Creating RetryingDataSourceHealthIndicator with maxRetries:{}:",maxRetriesProperty);
    }

    public void setMaxRetries(int newValue) {
        maxRetries = newValue;
    }

    @Override
    protected void doHealthCheck(Builder builder) throws Exception {

        builder.withDetail("sql", getQuery() == null ? " No query specified" : getQuery());
        try {
        	for (int i = 0; i < maxRetries; i++) {
                try {
                    super.doHealthCheck(builder);
                    builder.withDetail("retries", i);
                    builder.withDetail("totalRetries", totalRetries.get());
                    return;
                } catch (DataAccessException e) {
                    Throwable rootCause = e.getRootCause();
                    if (rootCause instanceof SQLTransientConnectionException) {
                        // Log possible Timeout
                        logger.warn("TransientConnectionException: {}", rootCause.getLocalizedMessage());
                        totalRetries.incrementAndGet();
                    } else {
                        throw e; // Rethrow
                    }
                }
            }
        } catch(Exception e) {
        	logger.error("Giving up after {} {}", maxRetries, " retries");
            builder.withDetail("failed", "After { " + maxRetries + "} attempts");
            builder.withDetail("totalRetries", totalRetries.get());
            String errorMsg = "OracleDB Health Check Failed";
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
    }
}
