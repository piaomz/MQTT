package org.example;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.util.HashMap;

public class MQSubscriber {
    private final int timeoutSec = 3;
    private String brokerURL;
    private String username;
    private String password;
    private String clientid;
    private String subscribeTopic;
    private int qos;
    private MqttClient client;
    private MQSubscriberCallbackInterface callback;
    private JSONObject returnMsg;
    private MemoryPersistence memoryPersistence;
    private static Logger logger = Logger.getLogger(MQSubscriber.class);
    public MQSubscriber(String brokerURL, String username,String password, String clientid, String subscribeTopic,int qos) throws MqttException {
        this.brokerURL = brokerURL;
        this.username = username;
        this.password = password;
        this.clientid = clientid;
        this.subscribeTopic = subscribeTopic;
        this.qos = qos;
    }
    public void setCallback(MQSubscriberCallbackInterface callback){
        this.callback = callback;


    }
    public void startListening() throws MqttException {
        client = new MqttClient(this.brokerURL, this.clientid, new MemoryPersistence());
        // connect options
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(this.username);
        options.setPassword(this.password.toCharArray());
        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(60);

        MQSubscriber that = this;
        client.setCallback(new MqttCallback() {
            public void connectionLost(Throwable cause) {
                logger.error("client: "+clientid+" connectionLost: " + cause.getMessage());
                try {
                    throw cause;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                //System.out.println("\nclient: "+clientid+" connectionLost: " + cause.getMessage());
            }
            public void messageArrived(String topic, MqttMessage message) throws ParseException {
                String msg =new String(message.getPayload());
                //System.out.println("\ntopic: " + topic);
                //System.out.println("Qos: " + message.getQos());
                //System.out.println("message content: " + msg);
                logger.info("Message Arrired: topic: " + topic+", Qos: "+ message.getQos()+", message content: " + msg);
                JSONParser parser = new JSONParser();
                that.returnMsg = (JSONObject) parser.parse(msg);

                that.callback.messageHandler(topic,that.returnMsg);
            }
            public void deliveryComplete(IMqttDeliveryToken token) {
                //System.out.println("\ndeliveryComplete---------" + token.isComplete());
            }
        });

        client.connect(options);
        client.subscribe(this.subscribeTopic, qos);
    }
    public JSONObject getDeviceInfo(String getDeviceInfoTopic,JSONObject requestMsg){
        try {
            //clear msg stored;
            this.returnMsg = null;
            // publish message
            this.sendMsg(getDeviceInfoTopic,requestMsg);

            //Wait for return msg
            double countdown = 0;
            while(countdown<timeoutSec){
                countdown+=0.1;
                Thread.sleep(100);
                if(returnMsg!=null){
                    return returnMsg;
                }
            }
            return null;
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendMsg(String topic,JSONObject msg) {
        String content = msg.toJSONString();
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        // publish message
        try {
            client.publish(topic,message);
            logger.info("Published: topic:"+topic+", message:"+message);

        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

    }

}
