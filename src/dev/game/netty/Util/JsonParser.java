package dev.game.netty.Util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParser {
    private static JSONParser jParser = new JSONParser();

    public static JSONObject createJson(String data){
        JSONObject result = new JSONObject();
        try{
            result = (JSONObject)jParser.parse(data);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return result;
    }
}
