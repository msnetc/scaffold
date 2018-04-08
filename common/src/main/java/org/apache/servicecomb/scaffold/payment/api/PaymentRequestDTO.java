package org.apache.servicecomb.scaffold.payment.api;

public class PaymentRequestDTO {
  private String transactionId;

  private long userId;

  private String bankName;

  private String cardNumber;

  private double amount;

  public String getTransactionId() {
    return transactionId;
  }

  public long getUserId() {
    return userId;
  }

  public String getBankName() {
    return bankName;
  }

  public String getCardNumber() {
    return cardNumber;
  }

  public double getAmount() {
    return amount;
  }

  public PaymentRequestDTO() {
  }

  public PaymentRequestDTO(String transactionId, long userId, String bankName, String cardNumber, double amount) {
    this.transactionId = transactionId;
    this.userId = userId;
    this.bankName = bankName;
    this.cardNumber = cardNumber;
    this.amount = amount;
  }
}
