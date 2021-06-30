package vn.com.mservice.verticles;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import io.vertx.core.logging.Logger;
import io.vertx.ext.sql.ResultSet;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import vn.com.mservice.factories.LoggingFactory;
import vn.com.mservice.factories.OracleDBFactory;
import vn.com.mservice.factories.RabbitFactory;

import java.util.List;


public class SMSReceiverVerticle extends AbstractVerticle {

    private static Logger LOGGER = LoggingFactory.getInstance(SMSReceiverVerticle.class.getName());

    @Override
    public void start(Promise<Void> startPromise) {
        initRabbitConnect().onSuccess(this::initSMSReceiver);
    }

    Future<RabbitMQClient> initRabbitConnect() {

        RabbitMQOptions rabbitMQOptions = RabbitFactory.getInstance().getOptions();

        RabbitMQClient client = RabbitMQClient.create(vertx, rabbitMQOptions);
        client.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                //OGGER.info("RabbitMQ successfully connected!");
                JsonObject queueConfig = new JsonObject();
                queueConfig.put("x-message-ttl", 10_000L);

                client.queueDeclare("ops_vertx_test", true, false, true, queueConfig, queueResult -> {
                    if (queueResult.succeeded()) {
                        //LOGGER.info("Queue declared!");
                    } else {
                        //LOGGER.info("Queue failed to be declared!");
                        queueResult.cause().printStackTrace();
                    }
                });
            } else {
                System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });

        return Future.<RabbitMQClient>succeededFuture(client);
    }

    Future<Void> initSMSReceiver(RabbitMQClient rabbitMQClient){
        rabbitMQClient.start().onSuccess(v -> {
            // At this point the exchange, queue and binding will have been declared even if the client connects to a new server
            rabbitMQClient.basicConsumer("ops_vertx_test", rabbitMQConsumerAsyncResult -> {
                if (rabbitMQConsumerAsyncResult.succeeded()) {
                    //LOGGER.info("RabbitMQ consumer created !");
                    RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
                    mqConsumer.handler(this::handlerMsg);
                } else {
                    rabbitMQConsumerAsyncResult.cause().printStackTrace();
                }
            });
        }).onFailure(ex -> {
            //LOGGER.("It went wrong: " + ex.getMessage());
        });
        return Future.succeededFuture();
    }

    void handlerMsg(RabbitMQMessage message){
        OracleDBFactory oracleDBFactory = OracleDBFactory.getInstance();
        String sql = "SELECT * FROM VH_SMSC_TEMPLATES WHERE TEMPLATE_NAME = ?";
        JsonArray params = new JsonArray();
        params.add("SMS_OPC_FPTSHOP_2");
        Future<ResultSet> future = oracleDBFactory.selectQuery(sql, params);

        future.onSuccess(resultSet -> {
            List<JsonObject> rows = resultSet.getRows();

            for (JsonObject row : rows) {

                String id = row.getString("TEMPLATE_NAME");
                LOGGER.info(id);
            }
        });

        LOGGER.info("Got message: " + message.body().toString());
    }
}
