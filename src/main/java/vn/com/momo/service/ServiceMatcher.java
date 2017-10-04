package vn.com.momo.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oracle.jdbc.OracleTypes;
import vn.com.momo.app.AppUtils;
import vn.com.momo.hikari.DataBaseCP;

import java.sql.*;

/**
 * Created by giangtrinh on 10/3/17.
 */

@Log4j2
public class ServiceMatcher {
    @Getter @Setter String serviceCode;
    Connection conn = DataBaseCP.getInstance().getConnection();

    public ServiceMatcher(String serviceCode){
        this.serviceCode = serviceCode;
    }

    public long getTransIDList(){
        long mainTransId = 0;
        try {
            CallableStatement cs = conn.prepareCall("{call PRO_DOISOAT_NGANHANG(?,?,?,?,?,?,?,?,?,?,?)}");
            cs.setString(1, "all");
            cs.setString(2, null);
            cs.setString(3, "all");
            cs.setString(4, serviceCode);
            cs.setString(5, "all");
            cs.setString(6, "14-09-2017 21:37:12");
            cs.setString(7, "15-09-2017 16:05:47");
            cs.setString(8, "all");
            cs.setInt(9, 1000000);
            cs.setInt(10, 0);
            cs.registerOutParameter(11, OracleTypes.CURSOR);
            cs.execute();

            ResultSet resultSet = (ResultSet) cs.getObject(11);
            while (resultSet.next()) {

                long tIDMomo = resultSet.getLong(2);
                String refIDMomo = resultSet.getString(2);
                long momoTransIDApp = resultSet.getLong(3);

                if(momoTransIDApp > 0){
                    int idTransPartner = getPartnerDataByTID(momoTransIDApp);
                    if (idTransPartner != 0){
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

    private int getPartnerDataByTID(long refIDMomo) throws SQLException {
        int idTransPartner = 0;
        String sql = "SELECT * FROM trans_partners WHERE PARTNER_ID = '" + this.serviceCode + "' AND REF_TID = '" + refIDMomo + "'";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            idTransPartner = rs.getInt(1);
        }
        return idTransPartner;
    }

    private void updateTIDPartner(int idPartner, long tIDMomo) {
        try {
            Statement statement = conn.createStatement();

            String sql = "UPDATE trans_partners SET TID = " + tIDMomo + " WHERE ID = " + idPartner;
            log.info(sql);
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }
}
