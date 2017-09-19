package vn.com.momo.bank;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
public class Eximbank {
    public Eximbank(String fileName) {

        try {
            JsonObject configJson = JsonParserInstance.getInstance().parse(
                    new FileReader(
                            "/home/strider/app/VN-Bank-Transaction-Parsing/target/resources/pattern_config.json"
                    )
            ).getAsJsonObject();

            JsonObject serviceConfig = configJson.getAsJsonObject("TPbank");

            FileInputStream excelFile = new FileInputStream(new File(fileName));
            Workbook workbook = new HSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            while (iterator.hasNext()) {

                Row currentRow = iterator.next();
                //log.info(configJson.getAsJsonObject("TPbank").getAsJsonObject("Date"));
                Integer positionDate = AppUtils.getIntFromJsonObject(serviceConfig.getAsJsonObject("Date"), "position");

                String momoSplit = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("MomoId"), "split");
                String momoPattern = AppUtils.getStringFromJsonObject(serviceConfig.getAsJsonObject("MomoId"), "pattern");
                Integer positionMomo = AppUtils.getIntFromJsonObject(serviceConfig.getAsJsonObject("MomoId"), "position");
                //log.info(momoPattern);
                try {
                    Pattern p = Pattern.compile(momoPattern);
                    Matcher m = p.matcher(currentRow.getCell(positionMomo).getStringCellValue());

                    if(m.find()){
                        log.info(m.group(0));
                    }
                }catch(IllegalStateException e){
                    log.info("Not a correct row!");
                    continue;
                }catch(NullPointerException e){
                    log.info("End of file!");
                    continue;
                }


                Transaction transaction = new Transaction();

                Iterator<Cell> cellIterator = currentRow.iterator();

                while (cellIterator.hasNext()) {

                    Cell currentCell = cellIterator.next();
                    if (currentCell.getCellTypeEnum() == CellType.STRING) {
                        //   System.out.print(currentCell.getStringCellValue() + "--");
                    } else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                        //    System.out.print(currentCell.getNumericCellValue() + "--");
                    }

                    //System.out.print(currentCell.getStringCellValue());

                }
                // System.out.println();

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}