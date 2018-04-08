package org.apache.servicecomb.scaffold.payment;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.scaffold.payment.api.PaymentRequestDTO;
import org.apache.servicecomb.scaffold.payment.api.PaymentResponseDTO;
import org.apache.servicecomb.scaffold.payment.api.PaymentService;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "payment")
@RequestMapping(path = "/")
public class PaymentServiceImpl implements PaymentService {
  private final PaymentRepository paymentRepository;

  @Autowired
  public PaymentServiceImpl(PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }

  @Override
  @PostMapping(path = "pay")
  @Transactional
  public PaymentResponseDTO pay(@RequestBody PaymentRequestDTO payment) {
    if (validatePayment(payment)) {
      if (checkBalance(payment)) {
        if (recordPayment(payment)) {
          if (cutWithBank(payment)) {
            return new PaymentResponseDTO(true);
          }
          throw new InvocationException(BAD_REQUEST, "cut with bank failed");
        }
        throw new InvocationException(BAD_REQUEST, "record payment failed");
      }
      throw new InvocationException(BAD_REQUEST, "check balance failed");
    }
    throw new InvocationException(BAD_REQUEST, "incorrect payment");
  }

  private boolean validatePayment(PaymentRequestDTO payment) {
    if (payment.getUserId() > 0 && payment.getAmount() > 0 && StringUtils.isNotEmpty(payment.getTransactionId())
        && StringUtils.isNotEmpty(payment.getBankName()) && StringUtils.isNotEmpty(payment.getCardNumber())) {
      //TransactionId需要不重复，未被使用过
      PaymentEntity pay = paymentRepository.findByTransactionId(payment.getTransactionId());
      return pay == null;
    }
    return false;
  }

  //检查用户的余额，这里我们假设每一个用户银行账户都有两百万存款
  //相比0.x.x(v0)版本，我们在新的1.x.x(v1)版本中大幅放宽的额度审查，认为用户银行账户都有两百万存款
  private boolean checkBalance(PaymentRequestDTO payment) {
    //我们先要查一下已经用了多少
    List<PaymentEntity> pays = paymentRepository.findByUserId(payment.getUserId());
    double used = 0;
    for (PaymentEntity pay : pays) {
      used += pay.getAmount();
    }
    //预估一下账户余额够不够
    return payment.getAmount() <= (2000000 - used);
  }

  //本地记账保留扣款凭据
  private boolean recordPayment(PaymentRequestDTO payment) {
    paymentRepository
        .save(new PaymentEntity(payment.getTransactionId(), payment.getUserId(), payment.getBankName(),
            payment.getCardNumber(), payment.getAmount(), new Date()));
    return true;
  }

  //请求银行划账
  private boolean cutWithBank(PaymentRequestDTO payment) {
    return true;
  }
}
