package vn.com.momo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import vn.com.momo.app.AppUtils;
import vn.com.momo.gson.JsonParserInstance;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by giangtrinh on 9/20/17.
 */

@Log4j2
public class ServiceCode {
    private String serviceCode;

    public ServiceCode(String fileName){
        try {
            JsonObject configJson = JsonParserInstance.getInstance().parse(
                    new FileReader(
                            "/Users/giangtrinh/app/report-api/target/resources/service_code.json"
                    )
            ).getAsJsonObject();

            JsonArray serviceCodeConfig = configJson.getAsJsonArray("service");
            Iterator<JsonElement> jsonArrayConfig = serviceCodeConfig.iterator();
            while(jsonArrayConfig.hasNext()){
                String pattern = AppUtils.getStringFromJsonObject(jsonArrayConfig.next().getAsJsonObject(), "pattern");
                log.info("Pattern: " + pattern);
                log.info("Filename: " + fileName);

                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(fileName);

                if(m.find()){
                    serviceCode = AppUtils.getStringFromJsonObject(jsonArrayConfig.next().getAsJsonObject(), "value");
                    log.info("serviceCode: " + serviceCode);
                    break;
                }
            }

        } catch (FileNotFoundException e) {
            log.info("File not found!");
        } catch(Exception e){

        }
    }

    public String getServiceCode(){
        return serviceCode;
    }
}
