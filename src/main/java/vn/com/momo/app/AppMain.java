package vn.com.momo.app;

import lombok.extern.log4j.Log4j2;
import vn.com.momo.api.ServiceController;
import vn.com.momo.constant.AppConstant;

import static spark.Spark.*;

/**
 * Created by navcs on 3/6/17.
 */
@Log4j2
public class AppMain {

	public static void main(String[] args) {
		try {
			setPathResources(args[0]);
			initServer();

			get("/hello", (req, res) -> {

				return "Hello Word";
			});

//			new CrontabController();
//			new CrontabController(new PromotionReportService());
			new ServiceController();

			exception(Exception.class, (e, req, res) -> {
				log.error(AppUtils.getFullStackTrace(e));
				res.status(400);
				res.body("The request cannot be fulfilled due to bad syntax.");
			});
			
			log.info("start end");

		} catch (Exception e) {
			log.error("{}; args.length = {}", AppUtils.getFullStackTrace(e), args.length);
		}
	}

	private static void setPathResources(String path) throws Exception {
		AppConstant.PATH_RESOURCES = path;
	}

	public static void initServer() throws Exception {
		port(Integer.parseInt(AppConfig.getInstance().getServer().getProperty(AppConstant.PORT)));
		threadPool(Integer.parseInt(AppConfig.getInstance().getServer().getProperty(AppConstant.MAX_THREADS)),
				Integer.parseInt(AppConfig.getInstance().getServer().getProperty(AppConstant.MIN_THREADS)),
				Integer.parseInt(AppConfig.getInstance().getServer().getProperty(AppConstant.TIME_OUT_MILLIS)));
		
	}

}
