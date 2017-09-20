package vn.com.momo.bank;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.com.momo.app.AppUtils;
import vn.com.momo.entity.Transaction;
import vn.com.momo.gson.JsonParserInstance;
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

    public ServiceParsing(String fileName) {

        try {
            JsonObject configJson = JsonParserInstance.getInstance().parse(
                    new FileReader(
                            "/Users/giangtrinh/app/report-api/target/resources/pattern_config.json"
                    )
            ).getAsJsonObject();

            JsonObject serviceConfig = configJson.getAsJsonObject("VCBbank");
            //VCB handle IBVCB TOPUP, pattern description differ with Cashin-Cashout

            FileInputStream excelFile = new FileInputStream(new File(fileName));

            Workbook workbook = new XSSFWorkbook(excelFile);
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
                    //Check regex for date and momoId
                    date = getStringValueByPattern(datePattern, positionDate, currentRow);
                    momoId = getStringValueByPattern(momoPattern, positionMomo, currentRow).replaceAll("[^0-9]", "");
                    transactionId = getStringValueByPattern(transactionPattern, positionTransaction, currentRow);

                    log.info("DAte: " + date);
                    log.info("momoId: " + momoId);
                    log.info("transaxtionId: " + transactionId);
                    if(!date.equals("") && !momoId.equals("")){
                        debitAmount = getDoubleValueByPattern(debitAmountPattern, debitAmountPosition, currentRow);
                        creditAmount = getDoubleValueByPattern(creditAmountPattern, creditAmountPosition, currentRow);
                        String typeMatcher = getStringValueByPattern(typePattern, typePosition, currentRow);
                        log.info("debit: " + debitAmount);
                        log.info("credit: " + creditAmount);

                        if(!typeMatcher.equals("")){
                            String typeTransaction = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("Type").getAsJsonObject("matcher"), typeMatcher);
                            log.info("typeTransaction: " + typeTransaction);
                        }
                    }



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
}