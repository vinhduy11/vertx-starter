package vn.com.mservice.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import netscape.javascript.JSObject;
import vn.com.mservice.factories.PostgresDBFactory;

public class DBVerticle extends AbstractVerticle {
    private static PostgresDBFactory postgresDBFactory;
    @Override
    public void start(Promise<Void> startPromise) {
        startDBConnection().compose(this::startEventBusConsumer);
    }

    Future<Void> startDBConnection(){
        postgresDBFactory = PostgresDBFactory.getInstance(vertx);
        return Future.succeededFuture();
    }

    Future<Void> startEventBusConsumer(Void unused){
        vertx.eventBus().consumer("vertx.test.hello", this::selectQuery);
        return Future.succeededFuture();
    }

    void selectQuery(Message msg){
        if (msg != null) {
            String name = (String) msg.body();

            msg.reply(String.format("Chao ban %s", name));
        }

    }
}
