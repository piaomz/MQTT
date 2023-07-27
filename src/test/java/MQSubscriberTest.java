import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.MQSubscriber;
import org.example.MQSubscriberCallbackInterface;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MQSubscriberTest {
    public static void main(String[] args) throws MqttException, ParseException {
        String broker = "tcp://broker.emqx.io:1883";
        String username = "emqx";
        String password = "public";
        String clientid = "mqttx_95566b52";
        String clientid2 = "mqttx_9144c890";
        String subscribeTopic = "mqtt/subscribeTestPiaomz";
        String getDeviceInfoTopic = "mqtt/getDeviceInfoTestPiaomz";
        int qos = 0;


        MQSubscriber subscriber = new MQSubscriber(broker,username,password,clientid,subscribeTopic,qos);
        subscriber.setCallback(new MQSubscriberCallbackInterface() {
            @Override
            public void messageHandler(String topic,JSONObject msg) {
                System.out.println("subscriber receive from topic: "+topic);
                System.out.println("msg: "+ msg.toJSONString());
            }
        });
        subscriber.startListening();

        MQSubscriber subscriber2 = new MQSubscriber(broker,username,password,clientid2,getDeviceInfoTopic,qos);
        subscriber2.setCallback(new MQSubscriberCallbackInterface() {
            @Override
            public void messageHandler(String topic,JSONObject msg) throws ParseException {
                System.out.println("subscriber2 receive from topic: "+topic);
                System.out.println("msg: "+ msg.toJSONString());
                JSONParser parser = new JSONParser();
                JSONObject msgreturn  = (JSONObject) parser.parse("{\"msg\":\"this is device info\"}");
                subscriber2.sendMsg(subscribeTopic,msgreturn);


            }
        });
        subscriber2.startListening();
        JSONParser parser = new JSONParser();
        JSONObject msg  = (JSONObject) parser.parse("{\"msg\":\"request device info\"}");
        System.out.println(subscriber.getDeviceInfo(getDeviceInfoTopic,msg));
    }
}
