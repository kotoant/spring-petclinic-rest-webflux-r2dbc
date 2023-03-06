package org.springframework.samples.petclinic.r2dbc;

import io.r2dbc.spi.Connection;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

@Component
public class DSLAccess {
    private final DatabaseClient databaseClient;

    private final Settings settings = new Settings().withRenderQuotedNames(RenderQuotedNames.NEVER);
    private final Map<String, SQLDialect> profileToDialect = Map.of(
        "h2", SQLDialect.H2,
        "postgresql", SQLDialect.POSTGRES,
        "mysql", SQLDialect.MYSQL
    );
    @Value("${database}")
    private String database;

    public DSLAccess(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    private DSLContext dsl(Connection connection) {
        return DSL.using(connection, profileToDialect.get(database), settings);
    }

    public <T> Mono<T> withDSLContext(Function<DSLContext, Mono<T>> action) {
        return databaseClient.inConnection(connection -> action.apply(dsl(connection)));
    }

    public <T> Flux<T> withDSLContextMany(Function<DSLContext, Flux<T>> action) {
        return databaseClient.inConnectionMany(connection -> action.apply(dsl(connection)));
    }
}
