package vn.com.momo.api;

import lombok.extern.log4j.Log4j2;
import spark.Service;
import vn.com.momo.app.AppConfig;
import vn.com.momo.service.ServiceCode;
import vn.com.momo.service.ServiceMatcher;
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
            get("/service", (request, response) -> {
               String paramName = request.queryParams("name");
                String paramId = request.queryParams("id");
                String filePath = AppConfig.getInstance().getFileConfig().getProperty("fileInputPath", "/target/resources/");
                ServiceCode serviceCode = new ServiceCode(filePath + paramName);

                ServiceParsing serviceParsing = new ServiceParsing(filePath + paramName, serviceCode.getServiceCode(), Integer.parseInt(paramId));
                return "OK";
            });
        });
        path(reportUrl, () -> {
            get("/mapping", (request, response) -> {
                ServiceMatcher matching = new ServiceMatcher("exim.bank");
                matching.getTransIDList();
                return "OK";
            });
        });
    }
}
