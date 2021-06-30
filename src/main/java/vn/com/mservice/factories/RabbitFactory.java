package vn.com.mservice.factories;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.rabbitmq.RabbitMQOptions;
import vn.com.mservice.verticles.SMSReceiverVerticle;

public class RabbitFactory {
    private static RabbitFactory instance = null;
    private static JsonObject loadedConfig = new JsonObject();
    private static Logger LOGGER = LoggingFactory.getInstance(RabbitFactory.class.getName());
    public static RabbitFactory getInstance(){
        if (instance == null){
            instance = new RabbitFactory();
        }
        return instance;
    }

    public static RabbitMQOptions getOptions(){
        //loadedConfig.mergeIn(config);
        loadedConfig.mergeIn(Vertx.currentContext().config());
        RabbitMQOptions rabbitMQOptions = new RabbitMQOptions();
        JsonObject rabbitObj = loadedConfig.getJsonObject("rabbitmq");

        rabbitMQOptions.setHost(rabbitObj.getString("host"));
        rabbitMQOptions.setUser(rabbitObj.getString("username"));
        rabbitMQOptions.setPassword(rabbitObj.getString("password"));
        rabbitMQOptions.setConnectionTimeout(rabbitObj.getInteger("connection_timeout")); // in milliseconds
        rabbitMQOptions.setRequestedHeartbeat(60); // in seconds
        rabbitMQOptions.setHandshakeTimeout(6000); // in milliseconds
        rabbitMQOptions.setRequestedChannelMax(5);
        rabbitMQOptions.setReconnectAttempts(5);
        rabbitMQOptions.setNetworkRecoveryInterval(500); // in milliseconds
        rabbitMQOptions.setAutomaticRecoveryEnabled(true);

        return rabbitMQOptions;
    }
}
