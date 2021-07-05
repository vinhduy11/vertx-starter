package vn.com.mservice.factories;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

public class PostgresDBFactory {
    private static PostgresDBFactory instance = null;
    private static JsonObject loadedConfig = new JsonObject();
    private static Logger LOGGER = LoggingFactory.getInstance(PostgresDBFactory.class.getName());
    private static SQLClient primaryClient = null;
    private static SQLClient standbyClient = null;
    public PostgresDBFactory(Vertx vertx){
        loadedConfig.mergeIn(Vertx.currentContext().config());
        createHikariCPPrimaryPool(vertx);
        createHikariCPStandbyPool(vertx);
    }
    public static PostgresDBFactory getInstance(Vertx vertx){
        if (instance == null)
            instance = new PostgresDBFactory(vertx);

        return instance;
    }

//    public void createC3P0Pool(){
//        JsonObject configObj = loadedConfig.getJsonObject("oracle_pri");
//        JsonObject config = new JsonObject()
//            .put("url", configObj.getString("jdbcUrl"))
//            .put("driver_class", "oracle.jdbc.OracleDriver")
//            .put("user", configObj.getString("username"))
//            .put("password", configObj.getString("password"))
//            .put("initial_pool_size", 10)
//            .put("min_pool_size", 2)
//            .put("max_pool_size", configObj.getString("maximumPoolSize"));
//
//
//        primaryClient = JDBCClient.createShared(Vertx.vertx(), config);
//    }

    public void createHikariCPPrimaryPool(Vertx vertx){
        JsonObject configObj = loadedConfig.getJsonObject("postgres_pri");
        JsonObject config = new JsonObject()
            .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
            .put("driverClassName", "org.postgresql.ds.PGSimpleDataSource")
            .put("jdbcUrl", configObj.getString("jdbcUrl"))
            .put("username", configObj.getString("username"))
            .put("password", configObj.getString("password"))
            .put("maximumPoolSize", configObj.getInteger("maximumPoolSize"))
            .put("maxLifetime", 1700000)
            .put("cachePrepStmts", true)
            .put("prepStmtCacheSize", 250)
            .put("prepStmtCacheSqlLimit", 2048)
            .put("useServerPrepStmts", true)
            .put("useLocalSessionState", true)
            .put("rewriteBatchedStatements", true)
            .put("cacheResultSetMetadata", true)
            .put("cacheServerConfiguration", true)
            .put("elideSetAutoCommits", true)
            .put("maintainTimeStats", false);

        standbyClient = JDBCClient.createShared(vertx, config);

    }

    public void createHikariCPStandbyPool(Vertx vertx){
        JsonObject configObj = loadedConfig.getJsonObject("postgres_pri");
        JsonObject config = new JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
                .put("driverClassName", "org.postgresql.Driver")
                .put("jdbcUrl", configObj.getString("jdbcUrl"))
                .put("username", configObj.getString("username"))
                .put("password", configObj.getString("password"))
                .put("maximumPoolSize", configObj.getInteger("maximumPoolSize"))
                .put("maxLifetime", 1700000)
                .put("cachePrepStmts", true)
                .put("prepStmtCacheSize", 250)
                .put("prepStmtCacheSqlLimit", 2048)
                .put("useServerPrepStmts", true)
                .put("useLocalSessionState", true)
                .put("rewriteBatchedStatements", true)
                .put("cacheResultSetMetadata", true)
                .put("cacheServerConfiguration", true)
                .put("elideSetAutoCommits", true)
                .put("maintainTimeStats", false);

        standbyClient = JDBCClient.createShared(vertx, config);
    }

    public Future<ResultSet> selectQueryWithParams(String sql, JsonArray params) {
        return Future.future(promise ->
        standbyClient.getConnection(res -> {
            if (res.succeeded()) {
                LOGGER.info("Get Connection Success");
                SQLConnection connection = res.result();
                connection.queryWithParams(sql, params, res2 -> {
                    if (res2.succeeded()) {
                        LOGGER.info("Query Done");
                        ResultSet rs = res2.result();
                        // Do something with results
                        promise.complete(rs);
                    } else {
                        LOGGER.error(res2.cause().getMessage());
                    }
                });
            } else {
                LOGGER.error(res.cause().getMessage());
            }
        }));
    }

    public Future<ResultSet> selectQuery(String sql) {
        return Future.future(promise ->
                standbyClient.getConnection(res -> {
                    if (res.succeeded()) {
                        LOGGER.info("Get Connection Success");
                        SQLConnection connection = res.result();
                        connection.query(sql, res2 -> {
                            if (res2.succeeded()) {
                                LOGGER.info("Query Done");
                                ResultSet rs = res2.result();
                                // Do something with results
                                promise.complete(rs);
                            } else {
                                LOGGER.error(res2.cause().getMessage());
                            }
                        });
                    } else {
                        LOGGER.error(res.cause().getMessage());
                    }
                }));
    }

}
