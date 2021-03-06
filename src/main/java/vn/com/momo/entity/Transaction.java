package vn.com.momo.entity;
import lombok.Getter;
import lombok.Setter;

public class Transaction {
    @Getter @Setter private String date;
    @Getter @Setter private String momoId;
    @Getter @Setter private String transactionId;
    @Getter @Setter private double debitAmount;
    @Getter @Setter private double creditAmount;
    @Getter @Setter private String type;
    @Getter @Setter private String serviceName;
}
