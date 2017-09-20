package vn.com.momo.api;

import lombok.extern.log4j.Log4j2;
import vn.com.momo.app.AppConfig;
import vn.com.momo.service.ServiceCode;
import vn.com.momo.service.ServiceParsing;
import vn.com.momo.constant.AppConstant;

import static spark.Spark.*;

/**
 * Created by giangtrinh on 8/23/17.
 */

@Log4j2
public class ServiceController {
    public ServiceController() throws Exception {
        String reportUrl = String.join(AppConstant.FORWARD_SLASH);
        path(reportUrl, () -> {
            get("/bank", (request, response) -> {
               String paramName = request.queryParams("name");

                //ServiceParsing vcb = new ServiceParsing("/Users/giangtrinh/Downloads/doisoat/vcb.xlsx");

                String filePath = AppConfig.getInstance().getFileConfig().getProperty("fileInputPath", "/target/resources/");
                ServiceCode serviceCode = new ServiceCode(filePath + paramName);

                ServiceParsing serviceParsing = new ServiceParsing(filePath + paramName, serviceCode.getServiceCode());
                return "OK";
            });
        });
    }
}
