package vn.com.momo.properties;



import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import vn.com.momo.constant.AppConstant;

public class PropertiesReader {
    private Properties properties;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public PropertiesReader(String resources) {

        try (InputStream props = new FileInputStream(AppConstant.PATH_RESOURCES + resources)) {
            Properties properties = new Properties();
            properties.load(props);
            setProperties(properties);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
