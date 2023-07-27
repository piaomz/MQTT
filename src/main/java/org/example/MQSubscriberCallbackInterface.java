package org.example;


import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public interface MQSubscriberCallbackInterface {
    public void messageHandler(String topic,JSONObject msg) throws ParseException;
}
