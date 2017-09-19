package vn.com.momo.gson;

import com.google.gson.JsonParser;

/**
 * Created by anhvunguyen on 24/01/2017.
 */
public class JsonParserInstance {
    private static JsonParser jsonParser = new JsonParser();

    public JsonParserInstance() {
    }

    public static JsonParser getInstance() {
        return jsonParser;
    }
}
