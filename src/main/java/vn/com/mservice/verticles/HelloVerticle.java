package vn.com.mservice.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class HelloVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
        vertx.eventBus().consumer("vertx.test.hello", msg->{
            msg.reply("Thu xem sao nha");
        });
        vertx.eventBus().consumer("vertx.test.name", msg->{
            String name = (String) msg.body();
            msg.reply(String.format("Chao ban %s", name));
        });
    }
}
