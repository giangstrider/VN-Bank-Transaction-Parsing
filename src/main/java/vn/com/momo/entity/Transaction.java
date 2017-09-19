package vn.com.momo.entity;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {
    @Getter @Setter private Date date;
    @Getter @Setter private int momoId;
    @Getter @Setter private int transacionId;
    @Getter @Setter private double amount;
    @Getter @Setter private String type;
}
