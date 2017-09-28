package vn.com.momo.service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.com.momo.app.AppConfig;
import vn.com.momo.app.AppUtils;
import vn.com.momo.entity.Transaction;
import vn.com.momo.gson.JsonParserInstance;
import vn.com.momo.hikari.DataBaseCP;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.*;
import java.util.Iterator;

/**
 * Created by giangtrinh on 9/18/17.
 */

@Log4j2
public class ServiceParsing {
    private String date;
    private String momoId = null;
    private String transactionId = null;
    private Double debitAmount = 0.0;
    private Double creditAmount = 0.0;
    private String typeTransaction = null;

    public ServiceParsing(String fileName, String serviceCode, int paramId) {

        try {
            JsonObject configJson = JsonParserInstance.getInstance().parse(
                    new FileReader(
                            AppConfig.getInstance().getFileConfig().getProperty("patternConfigPath", "/target/resources/pattern_config.json")
                    )
            ).getAsJsonObject();
            JsonObject serviceConfig = configJson.getAsJsonObject(serviceCode);

            FileInputStream excelFile = new FileInputStream(new File(fileName));
            String ableTypeParse = AppUtils.getStringFromJsonObject(serviceConfig, "FileType");
            Workbook workbook = null;
            if(ableTypeParse.equals("HSSF")){
                workbook = new HSSFWorkbook(excelFile);
            }else{
                workbook = new XSSFWorkbook(excelFile);
            }

            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            String datePattern = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("Date"), "pattern");
            Integer positionDate = AppUtils.getIntFromJsonObject(serviceConfig.getAsJsonObject("Date"), "position");

            String momoPattern = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("MomoId"), "pattern");
            Integer positionMomo = AppUtils.getIntFromJsonObject(serviceConfig.getAsJsonObject("MomoId"), "position");

            String transactionPattern = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("RefId"), "pattern");
            Integer positionTransaction = AppUtils.getIntFromJsonObject(serviceConfig.getAsJsonObject("RefId"), "position");

            String debitAmountPattern = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("DebitAmount"), "pattern");
            Integer debitAmountPosition = AppUtils.getIntFromJsonObject(serviceConfig.getAsJsonObject("DebitAmount"), "position");

            String creditAmountPattern = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("CreditAmount"), "pattern");
            Integer creditAmountPosition = AppUtils.getIntFromJsonObject(serviceConfig.getAsJsonObject("CreditAmount"), "position");

            String typePattern = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("Type"), "pattern");
            Integer typePosition = AppUtils.getIntFromJsonObject(serviceConfig.getAsJsonObject("Type"), "position");

            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                try {
                    date = getStringValueByPattern(datePattern, positionDate, currentRow).replaceAll("\\s.*", "");
                    Pattern patternDate = Pattern.compile("^(0[1-9]|[1-2][0-9]|3[0-1])\\/(0[1-9]|1[0-2])\\/[0-9]{4}$");
                    Matcher matcherDate = patternDate.matcher(date);
                    if(matcherDate.find()){
                        Transaction transaction = new Transaction();
                        transaction.setServiceName(serviceCode);
                        transaction.setDate(date);

                        if(positionMomo > 0){
                            momoId = getStringValueByPattern(momoPattern, positionMomo, currentRow).replaceAll("[^0-9]", "");
                            transaction.setMomoId(momoId);
                        }

                        transactionId = getStringValueByPattern(transactionPattern, positionTransaction, currentRow);
                        if(transactionId.equals("")){
                            log.info(currentRow.getCell(positionTransaction));
                        }
                        transaction.setTransactionId(transactionId);

                        debitAmount = getDoubleValueByPattern(debitAmountPattern, debitAmountPosition, currentRow);
                        transaction.setDebitAmount(debitAmount);

                        creditAmount = getDoubleValueByPattern(creditAmountPattern, creditAmountPosition, currentRow);
                        transaction.setCreditAmount(creditAmount);

                        String typeMatcher = getStringValueByPattern(typePattern, typePosition, currentRow);
                        if(!typeMatcher.equals("")){
                            typeTransaction = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("Type").getAsJsonObject("matcher"), typeMatcher);
                            transaction.setType(typeTransaction);
                        }

                        log.info("{Transaction: {Date: "+ transaction.getDate() +", momoId: "+ transaction.getMomoId() +", transactionId: "+ transaction.getTransactionId() +", " +
                                "debit: "+ transaction.getDebitAmount() +", credit: "+ transaction.getCreditAmount() +", type: "+ transaction.getType() +"}}");
                        saveServiceParsed(transaction);
                    }else{
                        log.info("NOT TRANSACTION: " + currentRow.getCell(positionTransaction));
                    }


                }catch(IllegalStateException e){
                    e.printStackTrace();
                    log.info(currentRow.getCell(positionTransaction));
                    continue;
                }catch(NumberFormatException e) {
                    e.printStackTrace();
                    log.info(currentRow.getCell(positionTransaction));
                    continue;
                }
                catch(NullPointerException e){
                    e.printStackTrace();
                    log.info(currentRow.getCell(positionTransaction));
                    //continue;
                }
            }
        } catch (FileNotFoundException e) {
            try {
                sendNotiStatus(paramId, 2);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (IOException e) {
            try {
                sendNotiStatus(paramId, 2);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (Exception e) {
            try {
                sendNotiStatus(paramId, 2);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }

        try {
            sendNotiStatus(paramId, 1);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("Done Import");

    }

    private void sendNotiStatus(int id, int status) throws Exception{
        String url = AppConfig.getInstance().getFileConfig().getProperty("apiDashboardPath", "http://172.16.8.10/accounts/receive_status");
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.addHeader("Content-Type","application/json");
        vn.com.momo.entity.Message message = new vn.com.momo.entity.Message();
        message.setId(id);
        message.setStatus(status);
        String param = new Gson().toJson(message);
        post.setEntity(new StringEntity(param));
        HttpResponse response = client.execute(post);
        String result = response.toString();
        log.info("Result: " + result);
    }

    private void saveServiceParsed(Transaction transaction) throws Exception {
        String sql = "INSERT INTO TRANS_PARTNERS(PARTNER_ID,REF_TID,MOMO_ID,TRANS_DATE,CREDIT_AMOUNT,DEBIT_AMOUNT,TRANS_TYPE) " +
                "VALUES('" + transaction.getServiceName() + "', '" + transaction.getTransactionId() + "', " + transaction.getMomoId() + ", to_date('" + transaction.getDate() + "', 'dd/MM/yyyy'), " +
                "" + transaction.getCreditAmount() + ", " + transaction.getDebitAmount() + ", '" + transaction.getType() + "')";
        //log.info(sql);
        DataBaseCP.getInstance().insert(sql);
    }

    private String getStringValueByPattern(String pattern, Integer position, Row currentRow){
        String value = "";
        if(!pattern.equals("")){
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(currentRow.getCell(position).getStringCellValue());

            if(m.find()){
                value = m.group(0).replaceAll("[-+.^:,_]","");
            }
        }else{
            value = currentRow.getCell(position).getStringCellValue();
        }

        return value;
    }

    private Double getDoubleValueByPattern(String pattern, Integer position, Row currentRow){
        Double value = 0.0;
        DataFormatter formatter = new DataFormatter();
        String valueString = formatter.formatCellValue(currentRow.getCell(position)).replace(".00", "").replaceAll("[,.]", "");
        if(!valueString.equals("")){
            value = Double.parseDouble(valueString);
        }

        return value;
    }

    private Date getDateValueByPattern(String pattern, Integer position, Row currentRow){
        String dateString = currentRow.getCell(position).getStringCellValue();
        Date date = null;
        try {
            date = new SimpleDateFormat(pattern).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            log.info("Parse date error");
        }
        return date;
    }
}