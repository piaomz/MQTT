import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.MQSubscriber;
import org.example.MQSubscriberCallbackInterface;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MQSubscriberTest {
    public static void main(String[] args) throws MqttException, ParseException {
        //Test Server
        String broker = "tcp://broker.emqx.io:1883";
        String username = "emqx";
        String password = "public";
        String clientid = "mqttx_95566b52";
        String clientid2 = "mqttx_9144c890";
        //The subsribed topic
        String subscribeTopic = "piaomz/subscribeTest";
        //For actively request device info
        String getDeviceInfoTopic = "piaomz/getDeviceInfoTest";
        int qos = 0;

        MQSubscriber subscriber = new MQSubscriber(broker,username,password,clientid,subscribeTopic,qos);
        subscriber.setCallback(new MQSubscriberCallbackInterface() {
            @Override
            public void messageHandler(String topic,JSONObject msg) {
                // Callback Function when received a message
                System.out.println("subscriber receive from topic: "+topic);
                System.out.println("msg: "+ msg);
            }
        });
        subscriber.startListening();

        //Another client for testing when receive message, return one
        MQSubscriber subscriber2 = new MQSubscriber(broker,username,password,clientid2,getDeviceInfoTopic,qos);
        subscriber2.setCallback(new MQSubscriberCallbackInterface() {
            @Override
            public void messageHandler(String topic,JSONObject msg) throws ParseException {
                //Use this msg can distinguish what msg is (what request is for), default is for request device info, return(publish) to the subsribe topic.
                System.out.println("subscriber2 receive from topic: "+topic);
                System.out.println("msg: "+ msg);
                //return message
                JSONParser parser = new JSONParser();
                JSONObject msgreturn  = (JSONObject) parser.parse("{\"msg\":\"this is device info\"}");
                subscriber2.sendMsg(subscribeTopic,msgreturn);
            }
        });
        subscriber2.startListening();

        //Testing for request the device info 主动请求
        JSONParser parser = new JSONParser();
        JSONObject msg  = (JSONObject) parser.parse("{\"msg\":\"request device info\"}");
        //actively request device info, need a new topic, can specific msg
        JSONObject result = subscriber.getDeviceInfo(getDeviceInfoTopic,msg);
        System.out.println(result);
    }
}
