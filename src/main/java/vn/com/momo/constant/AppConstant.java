package vn.com.momo.constant;

import java.time.format.DateTimeFormatter;

public class AppConstant {

    public static DateTimeFormatter FORMAT_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static String PATH_RESOURCES = "";
    
    public static String PORT = "port";
    public static String MIN_THREADS = "minThreads";
    public static String MAX_THREADS = "maxThreads";
    public static String TIME_OUT_MILLIS = "timeOutMillis";
    
    public static final String IN = "in";
    public static final String OUT = "out";
    public static final int ACTION_LOGIN_ID = 30;
    public static final int ACTION_REGISTER_ID = 52;
    
    public static final String ACTION_ID = "action_id";
    public static final String ACTION_NAME = "action_name";
    
    public static final String USER = "user";
    
    public static final String OUT_RESULT = "result";
    
    public static final String MOMO_MSG = "momoMsg";
    public static final String TRAN_HIS_MSG = "tranHisMsg";
    public static final String GIFT_ID = "giftId";
    public static final String USE_VOUCHER = "useVoucher";
    
//    public static final String AMOUNT = "amount";
    public static final String SERVICE_ID = "serviceId";
    public static final String TRAN_ID = "tranId";
    public static final String TRAN_TYPE = "tranType";
    public static final String TIME = "time";
    public static final String PREFIX_KEYSPACE = "prefixKeyspace";
	public static final String FORWARD_SLASH  = "/";
	public static final String UNDERSCORE  = "_";
	public static final String BACK_SLASH  = "\\";
	public static final int KEY_EXPIRE_TWO_HOUR = 3600 * 2;

    public static final String LOCATION = "location";

}
