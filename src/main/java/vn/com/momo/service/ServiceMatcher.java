package vn.com.momo.service;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oracle.jdbc.OracleTypes;
import vn.com.momo.app.AppConfig;
import vn.com.momo.app.AppUtils;
import vn.com.momo.gson.JsonParserInstance;
import vn.com.momo.hikari.DataBaseCP;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.*;

/**
 * Created by giangtrinh on 10/3/17.
 */

@Log4j2
public class ServiceMatcher {
    @Getter @Setter String serviceCode;
    Connection conn = DataBaseCP.getInstance().getConnection();
    JsonObject serviceConfig;

    public ServiceMatcher(String serviceCode){
        this.serviceCode = serviceCode;

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

    public long getTransIDList(){
        long mainTransId = 0;
        try {
            CallableStatement cs = conn.prepareCall("{call PRO_DOISOAT_NGANHANG_DAILY(?,?,?,?)}");
            cs.setString(1, serviceCode);
            cs.setString(2, "15-09-2017 00:00:00");
            cs.setString(3, "15-09-2017 23:59:59");
            cs.registerOutParameter(4, OracleTypes.CURSOR);
            cs.execute();

            ResultSet resultSet = (ResultSet) cs.getObject(4);
            String fieldRefID = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("MisRefID"), "name");
            String typeRefID = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("MisRefID"), "type");
            log.info("reach first");
            while (resultSet.next()) {
                long tIDMomo = resultSet.getLong(1);
                if(typeRefID.equals("long")){
                    long momoTransIDApp = resultSet.getLong(fieldRefID);
                    if(momoTransIDApp > 0){
                        int idTransPartner = getPartnerDataByTIDLong(momoTransIDApp);
                        updateTIDPartner(idTransPartner, tIDMomo);
                        log.info("Update TransPartnerID: " + idTransPartner);
                    }
                }else{
                    log.info("reach 1");
                    String refIdMomo = resultSet.getString(fieldRefID);
                    log.info("ref: " + refIdMomo);
                    if(refIdMomo != null){
                        int idTransPartner = getPartnerDataByTIDString(refIdMomo);
                        updateTIDPartner(idTransPartner, tIDMomo);
                        log.info("Update TransPartnerID: " + idTransPartner);
                    }
                }
            }
            conn.close();

        } catch (Exception e) {
            e.getStackTrace();
        }
        return mainTransId;
    }

    private int getPartnerDataByTIDLong(long refIDMomo) throws SQLException {
        int idTransPartner = 0;
        String sql = "SELECT * FROM trans_partners WHERE PARTNER_ID = '" + this.serviceCode + "' AND REF_TID = '" + refIDMomo + "'";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            idTransPartner = rs.getInt(1);
        }
        return idTransPartner;
    }

    private int getPartnerDataByTIDString(String refIDMomo) throws SQLException {
        int idTransPartner = 0;
        String sql = "SELECT * FROM trans_partners WHERE PARTNER_ID = '" + this.serviceCode + "' AND REF_TID = '" + refIDMomo + "'";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            idTransPartner = rs.getInt(1);
        }
        return idTransPartner;
    }

    private void updateTIDPartner(long idPartner, long tIDMomo) {
        if (idPartner != 0) {
            try {
                Statement statement = conn.createStatement();

                String sql = "UPDATE trans_partners SET TID = " + tIDMomo + " WHERE ID = " + idPartner;
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                e.getStackTrace();
            }
        }
    }
}
