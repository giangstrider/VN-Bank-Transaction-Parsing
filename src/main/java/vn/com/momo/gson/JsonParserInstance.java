package vn.com.momo.gson;

import com.google.gson.JsonParser;

public class JsonParserInstance {
    private static JsonParser jsonParser = new JsonParser();

    public JsonParserInstance() {
    }

    public static JsonParser getInstance() {
        return jsonParser;
    }
}
