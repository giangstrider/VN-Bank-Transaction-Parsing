package vn.com.momo.app;

import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Created by anhvunguyen on 24/01/2017.
 */

@Log4j2
public class AppUtils {

	public static DateTimeFormatter FORMAT_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static DateTimeFormatter FORMAT_DATE_UNDERSCORE = DateTimeFormatter.ofPattern("yyyy_MM_dd");
	
	
	
    public static String getFullStackTrace(Exception ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);

        try {
            printWriter.close();
            stringWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringWriter.toString();
    }

    public static String createKey(Object... objects) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < objects.length - 1; i++) {
            stringBuilder.append(objects[i]);
            stringBuilder.append("_");
        }

        stringBuilder.append(objects[objects.length - 1]);

        return stringBuilder.toString();
    }

    public static String getStringFromJsonObject(JsonObject joData, String... keys) {
        try {
            return getStringFromJsonObject(getLastJsonObject(joData, keys), keys[keys.length - 1]);
        } catch (Exception e) {
            log.error("Exception = {}; jsonObject = {}; key = {};", e, joData, keys);
        }

        return "";
    }

    public static String getStringFromJsonObject(JsonObject joData, String key) throws Exception {
        if (isJsonPrimitiveString(joData, key)) {
            return joData.get(key).getAsString();
        }
        return "";
    }
    
    public static boolean isJsonPrimitiveNumber(JsonObject joData, String key) throws Exception {
        if (joData.has(key)) {
            if (joData.get(key).isJsonPrimitive()) {
                return joData.get(key).getAsJsonPrimitive().isNumber();
            }
        }
        return false;
    }
    
    public static int getIntFromJsonObject(JsonObject joData, String... keys) {
        try {
            return getIntFromJsonObject(getLastJsonObject(joData, keys), keys[keys.length - 1]);
        } catch (Exception e) {
            log.error("Exception = {}; jsonObject = {}; key = {};", e, joData, keys);
        }

        return 0;
    }
    public static int getIntFromJsonObject(JsonObject joData, String key) throws Exception {
        if (isJsonPrimitiveNumber(joData, key)) {
            return joData.get(key).getAsInt();
        }

        return 0;
    }

    public static boolean isJsonPrimitiveString(JsonObject joData, String key) throws Exception {
        if (joData.has(key)) {
            if (joData.get(key).isJsonPrimitive()) {
                return joData.get(key).getAsJsonPrimitive().isString();
            }
        }
        return false;
    }

    public static JsonObject getLastJsonObject(JsonObject joData, String... keys) throws Exception {
        JsonObject joTemp = joData;

        for (int i = 0; i < keys.length; i++) {
            if (isJsonObject(joTemp, keys[i])) {
                joTemp = joTemp.get(keys[i]).getAsJsonObject();
            } else {
                break;
            }
        }

        return joTemp;
    }

    public static boolean isJsonObject(JsonObject joData, String key) throws Exception {
        if (joData.has(key)) {
            return joData.get(key).isJsonObject();
        }

        return false;
    }

    public static LocalDateTime convertLocalDateTime(long time) throws Exception {
        return Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    public static boolean getBooleanFromJsonObject(JsonObject joData, String... keys) {
        try {
            return getBooleanFromJsonObject(getLastJsonObject(joData, keys), keys[keys.length - 1]);
        } catch (Exception e) {
            log.error("Exception = {}; jsonObject = {}; key = {};", e, joData, keys);
        }

        return false;
    }
    public static boolean getBooleanFromJsonObject(JsonObject joData, String key) throws Exception {
        if (isJsonPrimitiveBoolean(joData, key)) {
            return joData.get(key).getAsBoolean();
        }
        return false;
    }
    public static boolean isJsonPrimitiveBoolean(JsonObject joData, String key) throws Exception {
        if (joData.has(key)) {
            if (joData.get(key).isJsonPrimitive()) {
                return joData.get(key).getAsJsonPrimitive().isBoolean();
            }
        }
        return false;
    }
    
    

}