package vn.com.momo.service;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oracle.jdbc.OracleTypes;
import org.apache.commons.collections4.CollectionUtils;
import vn.com.momo.app.AppConfig;
import vn.com.momo.app.AppUtils;
import vn.com.momo.gson.JsonParserInstance;
import vn.com.momo.hikari.DataBaseCP;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by giangtrinh on 10/3/17.
 */

@Log4j2
public class ServiceMatcher {
    @Getter @Setter String serviceCode;
    Connection conn = DataBaseCP.getInstance().getConnection();
    JsonObject serviceConfig;
    String requestDate;
    String fromDate;
    String toDate;
    HashMap PartnerRefDiction;

    public ServiceMatcher(String serviceCode, String fromDate, String toDate){
        this.serviceCode = serviceCode;
        this.fromDate = fromDate;
        this.toDate = toDate;

        JsonObject configJson = null;
        try {
            configJson = JsonParserInstance.getInstance().parse(
                    new FileReader(
                            AppConfig.getInstance().getFileConfig().getProperty("patternConfigPath", "/target/resources/pattern_config.json")
                    )
            ).getAsJsonObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        serviceConfig = configJson.getAsJsonObject(serviceCode);
    }

    public ArrayList getDifferData(){
        ArrayList<HashMap> misData = excludedData();

        for(HashMap transaction : misData){
            if(!checkAnsweredData(transaction.get("REF_TID").toString())){
                insertTransPartnersDiff(transaction);
            }
            log.info("{Transaction: {TID: "+ transaction.get("REF_TID") +", AMOUNT: "+ transaction.get("AMOUNT") +", TRANS_TYPE: "+ transaction.get("TRANS_TYPE") +"}}");
        }
//        log.info(misData.size());
        return misData;
    }

    private ArrayList excludedData(){
        ArrayList misData = getMisData();
        log.info("data: " + misData.size());
        ArrayList serviceData = getServiceData();
        log.info("service: " + serviceData.size());
        misData.removeAll(getServiceData());
        log.info("dataafter: " + misData.size());
        serviceData.removeAll(getMisData());
        log.info("serviceafter: " + serviceData.size());
        misData.addAll(serviceData);
        log.info("LAST: " + misData.size());
        return misData;
    }

    private ArrayList getMisData() {
        ArrayList misData = new ArrayList();

        try {
            CallableStatement cs = conn.prepareCall("{call PRO_DOISOAT_NGANHANG_DAILY(?,?,?,?)}");
            cs.setString(1, serviceCode);
            cs.setString(2, "15-09-2017 00:00:00");
            cs.setString(3, "15-09-2017 23:59:59");
            cs.registerOutParameter(4, OracleTypes.CURSOR);
            cs.execute();

            ResultSet resultSet = (ResultSet) cs.getObject(4);
            String fieldRefID = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("MisRefID"), "name");

            while (resultSet.next()){
                int statusTrans = resultSet.getInt("STATE");
                if(statusTrans != 6){
                    HashMap transactionData = new HashMap();
                    transactionData.put("REF_TID", resultSet.getString(fieldRefID));
                    transactionData.put("AMOUNT", resultSet.getDouble("AMOUNT"));
                    transactionData.put("TRANS_TYPE", resultSet.getString("TRANSTYPE"));
                    misData.add(transactionData);
                }
            }

        } catch (Exception e){
            e.getStackTrace();
        }

        return misData;
    }

    private ArrayList getServiceData() {
        ArrayList serviceData = new ArrayList();
        try{
            String onlyDateParsing = fromDate.substring(0, 10);
            requestDate = onlyDateParsing;
            String sql = "SELECT * FROM TRANS_PARTNERS WHERE PARTNER_ID = '" + serviceCode +"' AND TRANS_DATE = TO_DATE('" + onlyDateParsing + "','dd-MM-yyyy')";
            //String sql = "SELECT COUNT(*) FROM TRANS_PARTNERS WHERE PARTNER_ID = '" + serviceCode +"' AND TRANS_DATE = TO_DATE('" + onlyDateParsing + "','dd-MM-yyyy')";
            log.info(sql);
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            int count = 0;
            while (resultSet.next()) {
                double amount = resultSet.getDouble("CREDIT_AMOUNT");
                if(amount == 0){
                    amount = resultSet.getDouble("DEBIT_AMOUNT");
                }

                if(amount == 0){
                    log.info(resultSet.getString("REF_TID") + " - " + amount + " - " + resultSet.getString("TRANS_TYPE"));
                }

                HashMap transactionData = new HashMap();
                transactionData.put("REF_TID", resultSet.getString("REF_TID"));
                transactionData.put("AMOUNT", amount);
                transactionData.put("TRANS_TYPE", resultSet.getString("TRANS_TYPE"));
                serviceData.add(transactionData);

                count++;
            }

            log.info("===" + count);
        }catch (Exception e){
            e.getStackTrace();
        }

        return serviceData;
    }

    private boolean checkAnsweredData(String ref_tid){
        try{
            String sql = "SELECT * FROM TRANS_PARTNERS_DIFF WHERE REF_TID = " + ref_tid;
            log.info(sql);
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            int count = 0;
            while (resultSet.next()) {
                count++;
            }

            log.info("Count: " + count);
            if(count > 0){
                return true;
            }
        }catch (Exception e){
            e.getStackTrace();
        }

        return false;
    }

    private void insertTransPartnersDiff(HashMap transaction){
        String sql = "INSERT INTO TRANS_PARTNERS_DIFF(PARTNER_ID,REF_TID,TID,TRANS_DATE,AMOUNT,TRANS_TYPE, MOMO_ID) " +
                "VALUES('" + serviceCode + "', '" + transaction.get("REF_TID") + "', " + transaction.get("TID") + ", to_date('" + requestDate + "', 'dd/MM/yyyy'), " +
                "" + transaction.get("AMOUNT") + ", " + transaction.get("TRANS_TYPE") + ", '" + "momo_id" + "')";
        //log.info(sql);
        DataBaseCP.getInstance().insert(sql);
    }
}
