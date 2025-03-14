package school.hei.haapi.service.utils;

import static school.hei.haapi.service.utils.DataFormatterUtils.instantToCommonDate;

import java.util.List;
import school.hei.haapi.model.dto.FeeDto;
import school.hei.haapi.model.dto.PaymentDto;
import school.hei.haapi.model.dto.UserDto;

public class PaidFeeReceiptDataProvider {
  private final UserDto user;
  private final FeeDto fee;
  private final PaymentDto payment;
  private final List<PaymentDto> paidPaymentsBefore;

  public PaidFeeReceiptDataProvider(PaymentDto paymentDto, List<PaymentDto> payments) {
    this.user = paymentDto.getUser();
    this.fee = paymentDto.getFee();
    this.payment = paymentDto;
    this.paidPaymentsBefore = paymentsSinceActual(payments, paymentDto);
  }

  public String getEntirePaymentAuthorName() {
    return user.getLastName() + " " + user.getFirstName();
  }

  public int getFeeTotalAmount() {
    return fee.getTotalAmount();
  }

  public int getTotalPaymentAmount() {
    return payment.getAmount();
  }

  public String getFeeComment() {
    return fee.getComment();
  }

  public int getRemainingAmount() {
    int actualTotalPaymentAmount = defineTotalPaymentSinceActual(paidPaymentsBefore);
    return fee.getTotalAmount() - actualTotalPaymentAmount;
  }

  public String getPaymentDate() {
    return instantToCommonDate(payment.getCreationDatetime());
  }

  public school.hei.haapi.endpoint.rest.model.Payment.TypeEnum getPaymentType() {
    return payment.getPaymentType();
  }

  private List<PaymentDto> paymentsSinceActual(List<PaymentDto> payments, PaymentDto payment) {
    int indexOfPayment = payments.indexOf(payment);
    return payments.subList(0, indexOfPayment + 1);
  }

  private int defineTotalPaymentSinceActual(List<PaymentDto> payments) {
    return payments.stream().map(PaymentDto::getAmount).reduce(0, Integer::sum);
  }
}
