package org.apache.servicecomb.scaffold.payment;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_Payment")
public class PaymentEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String transactionId;

  private long userId;

  private String bankName;

  private String cardNumber;

  private double amount;

  private Date time;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public String getBankName() {
    return bankName;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName;
  }

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public PaymentEntity() {
  }

  public PaymentEntity(String transactionId, long userId, String bankName, String cardNumber, double amount,
      Date time) {
    this.transactionId = transactionId;
    this.userId = userId;
    this.bankName = bankName;
    this.cardNumber = cardNumber;
    this.amount = amount;
    this.time = time;
  }
}
