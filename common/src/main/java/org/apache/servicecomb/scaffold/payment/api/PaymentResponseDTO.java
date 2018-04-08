package org.apache.servicecomb.scaffold.payment.api;

public class PaymentResponseDTO {
  private boolean success;

  public boolean isSuccess() {
    return success;
  }

  public PaymentResponseDTO() {
  }

  public PaymentResponseDTO(boolean success) {
    this.success = success;
  }
}
