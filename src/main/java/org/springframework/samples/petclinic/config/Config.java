package org.springframework.samples.petclinic.config;

import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Configuration
@Component
@EnableR2dbcRepositories(basePackages = "org.springframework.samples.petclinic.r2dbc")
public class Config {

    @Value("${database}")
    private String database;

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    @LiquibaseDataSource
    public HikariDataSource springDataSource(DataSourceProperties properties) {
        return newHikariDataSource(properties);
    }

    private HikariDataSource newHikariDataSource(DataSourceProperties properties) {
        var dataSource = new HikariDataSource();
        dataSource.setDriverClassName(properties.getDriverClassName());
        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        return dataSource;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(HikariDataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.r2dbc.init-database", name = "true")
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
        populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("db/" + database + "/initDB.sql")));
        populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("db/" + database + "/populateDB.sql")));
        initializer.setDatabasePopulator(populator);

        return initializer;
    }

}
