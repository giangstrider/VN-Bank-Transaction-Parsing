package vn.com.momo.api;

import lombok.extern.log4j.Log4j2;
import vn.com.momo.bank.ServiceParsing;
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
            before("/*", (q, a) -> log.info("Received api call"));
            get("/bank", (request, response) -> {
                ServiceParsing vcb = new ServiceParsing("/Users/giangtrinh/Downloads/doisoat/vcb.xlsx");

                return "OK";
            });
        });
    }
}
