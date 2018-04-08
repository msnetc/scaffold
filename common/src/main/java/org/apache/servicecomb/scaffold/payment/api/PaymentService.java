package org.apache.servicecomb.scaffold.payment.api;

public interface PaymentService {
  PaymentResponseDTO pay(PaymentRequestDTO payment);
}
