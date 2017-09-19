package vn.com.momo.app;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import vn.com.momo.constant.AppConstant;
import vn.com.momo.properties.PropertiesReader;

import java.io.FileInputStream;
import java.util.Properties;


/**
 * Created by navcs on 2/8/17.
 */
@Log4j2
public class AppConfig {

    @Getter
    Properties server;
    @Getter
    Properties hikariCp;
    @Getter
    Properties producer;
    @Getter
    Properties topicProducer;

    //private Configurations configs;

    private static AppConfig instance = new AppConfig();

    private AppConfig() {
        try {
            server = getPropertiesPath("server.properties");

            producer = new PropertiesReader("producer.properties").getProperties();
            topicProducer = new PropertiesReader("topicproducer.properties").getProperties();

        } catch (Exception e) {
            log.error(AppUtils.getFullStackTrace(e));
        }
    }

    public static AppConfig getInstance() {
        return instance;
    }

    public Properties getPropertiesPath(String fileName) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(AppConstant.PATH_RESOURCES + fileName));
        return properties;
    }
}
