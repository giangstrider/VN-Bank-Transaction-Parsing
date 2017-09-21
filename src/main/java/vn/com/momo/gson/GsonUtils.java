package vn.com.momo.gson;

import com.google.gson.Gson;
import lombok.Getter;

public class GsonUtils {
    private static GsonUtils gsonUtils = new GsonUtils();

    @Getter
    private Gson gson;

    public GsonUtils() {
        this.gson = new Gson();
    }

    public static Gson gson() {
        return gsonUtils.getGson();
    }
}
