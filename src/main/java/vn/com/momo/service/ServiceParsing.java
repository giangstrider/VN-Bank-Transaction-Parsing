package vn.com.momo.service;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
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
    private String momoId;
    //private Integer transactionId;
    private String transactionId;
    private Double debitAmount;
    private Double creditAmount;
    private String typeTransaction;

    public ServiceParsing(String fileName, String serviceCode) {

        try {
            JsonObject configJson = JsonParserInstance.getInstance().parse(
                    new FileReader(
                            AppConfig.getInstance().getFileConfig().getProperty("patternConfigPath", "/target/resources/pattern_config.json")
                    )
            ).getAsJsonObject();

            JsonObject serviceConfig = configJson.getAsJsonObject(serviceCode);
            //VCB handle IBVCB TOPUP, pattern description differ with Cashin-Cashout

            FileInputStream excelFile = new FileInputStream(new File(fileName));

            Workbook workbook = new HSSFWorkbook(excelFile);
            //Workbook workbook = new HSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            while (iterator.hasNext()) {

                Row currentRow = iterator.next();
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

                try {
                    Transaction transaction = new Transaction();
                    transaction.setServiceName(serviceCode);

                    //Check regex for date and momoId
                    date = getStringValueByPattern(datePattern, positionDate, currentRow).replaceAll("\\s.*", "");
                    momoId = getStringValueByPattern(momoPattern, positionMomo, currentRow).replaceAll("[^0-9]", "");
                    transactionId = getStringValueByPattern(transactionPattern, positionTransaction, currentRow);

                    log.info("Date: " + date);
                    log.info("momoId: " + momoId);
                    log.info("transactionId: " + transactionId);

                    transaction.setDate(date);
                    transaction.setMomoId(Integer.parseInt(momoId));
                    transaction.setTransactionId(transactionId);

                    if(!date.equals("") && !momoId.equals("")){
                        debitAmount = getDoubleValueByPattern(debitAmountPattern, debitAmountPosition, currentRow);
                        creditAmount = getDoubleValueByPattern(creditAmountPattern, creditAmountPosition, currentRow);
                        transaction.setDebitAmount(debitAmount);
                        transaction.setCreditAmount(creditAmount);

                        String typeMatcher = getStringValueByPattern(typePattern, typePosition, currentRow);
                        log.info("debit: " + debitAmount);
                        log.info("credit: " + creditAmount);

                        if(!typeMatcher.equals("")){
                            typeTransaction = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("Type").getAsJsonObject("matcher"), typeMatcher);
                            transaction.setType(typeTransaction);
                            log.info("typeTransaction: " + typeTransaction);
                        }
                    }

                    saveServiceParsed(transaction);
                }catch(IllegalStateException e){
                    log.info("IllegalStateException - Not a correct row!");
                    continue;
                }catch(NumberFormatException e) {
                    log.info("NumberFormatException - Not a correct row!");
                    continue;
                }
                catch(NullPointerException e){
                    log.info("Null pointer");
                    continue;
                }

                log.info("=================================");


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void saveServiceParsed(Transaction transaction) throws Exception {
        String sql = "INSERT INTO TRANS_PARTNERS(PARTNER_ID,REF_TID,TID,TRANS_DATE,CREDIT_AMOUNT,DEBIT_AMOUNT,TRANS_TYPE) " +
                "VALUES('" + transaction.getServiceName() + "', '" + transaction.getTransactionId() + "', 1111, to_date('" + transaction.getDate() + "', 'dd/MM/yyyy'), " +
                "" + transaction.getCreditAmount() + ", " + transaction.getDebitAmount() + ", '" + transaction.getType() + "')";
        log.info(sql);
        DataBaseCP.getInstance().insert(sql);

    }

    protected List<Object> addFields(Transaction transaction) {
        List<Object> valuesInsertQuerySql = new ArrayList<>();
        valuesInsertQuerySql.add("PartnerId");
        valuesInsertQuerySql.add("RefTID");
        valuesInsertQuerySql.add("TDU");
        valuesInsertQuerySql.add(transaction.getDate());

        return valuesInsertQuerySql;
    }

    private String getStringValueByPattern(String pattern, Integer position, Row currentRow){
        String value = null;
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
            log.info("Parse date error");
        }
        return date;
    }
}