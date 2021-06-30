package vn.com.mservice.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vn.com.mservice.factories.LoggingFactory;
import vn.com.mservice.factories.OracleDBFactory;
import vn.com.mservice.factories.RabbitFactory;

import java.util.List;

public class HTTPVerticle extends AbstractVerticle {
    private static Logger LOGGER = LoggingFactory.getInstance(HTTPVerticle.class.getName());
    private HttpServer server;
    private JsonObject loadedConfig = new JsonObject();
    @Override
    public void start(Promise<Void> startPromise) {
        initRouter().compose(this::initHttpServer);
    }

    Future<Router> initRouter() {
        Router router = Router.router(vertx);
        router.get("/w").handler(this::doWelcome);
        router.get("/h").handler(this::doHello);
        router.get("/h/:name").handler(this::doHelloName);
        return Future.succeededFuture(router);
    }

    Future<Void> initHttpServer(Router router) {
        server = vertx.createHttpServer().requestHandler(router);
        // Now bind the server:
        loadedConfig.mergeIn(config());

        JsonObject httpObj = loadedConfig.getJsonObject("http");
        int httpPort = httpObj.getInteger("port");
        LOGGER.info(httpPort);
        return Future.future(promise -> server.listen(httpPort));
    }

    void doWelcome(RoutingContext ctx) {
        ctx.response()
                .putHeader("content-type", "text/plain")
                .end("Start thanh cong roi ne mung qua");
    }

    void doHello(RoutingContext ctx) {
        vertx.eventBus().request("vertx.test.hello", "Test", ar -> {
            if (ar.succeeded()) {
                ctx.response().putHeader("content-type", "text/plain").end("Received reply: " + ar.result().body());
            }
        });
    }

    void doHelloName(RoutingContext ctx) {
        String name = ctx.pathParam("name");
        vertx.eventBus().request("vertx.test.name", name, ar->{
            ctx.response().putHeader("content-type", "text/plain").end("Received reply: " + ar.result().body());
        });
    }

}
