package vn.com.mservice.verticles;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;


public class MainVerticle extends AbstractVerticle {
    final JsonObject loadedConfig = new JsonObject();

    @Override
    public void start(Promise<Void> startPromise) {
        loadConfig().onSuccess(this::deployOtherVerticles);
    }

    Future<JsonObject> loadConfig() {
        ConfigStoreOptions fileStore = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path", "./config.json"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(fileStore);

        ConfigRetriever configRetriever = ConfigRetriever.create(vertx, options);
        return configRetriever.getConfig();
    }

    Future<Void> deployOtherVerticles(JsonObject config){
        final DeploymentOptions unblocking_opts = new DeploymentOptions().setWorker(false).setConfig(config);
        final DeploymentOptions blocking_opts = new DeploymentOptions().setWorker(true).setInstances(8).setConfig(config);
        Future<String> httpVerticle = Future.future(promise -> vertx.deployVerticle(new HTTPVerticle().getClass().getName(), unblocking_opts, promise));
        Future<String> helloVerticle = Future.future(promise -> vertx.deployVerticle(new HelloVerticle().getClass().getName(), unblocking_opts, promise));
        Future<String> smsReceiverVerticle = Future.future(promise -> vertx.deployVerticle(new SMSReceiverVerticle().getClass().getName(), unblocking_opts, promise));

        return CompositeFuture.all(httpVerticle, helloVerticle, smsReceiverVerticle).mapEmpty();
    }

}
