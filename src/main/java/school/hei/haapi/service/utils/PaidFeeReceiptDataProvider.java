package school.hei.haapi.service.utils;

import static school.hei.haapi.service.utils.DataFormatterUtils.instantToCommonDate;

import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;

public class PaidFeeReceiptDataProvider {
  private final User student;
  private final Fee fee;
  private final Payment payment;

  public PaidFeeReceiptDataProvider(User student, Fee fee, Payment payment) {
    this.student = student;
    this.fee = fee;
    this.payment = payment;
  }

  public String getEntirePaymentAuthorName() {
    return student.getLastName() + " " + student.getFirstName();
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
    return fee.getTotalAmount() - payment.getAmount();
  }

  public String getPaymentDate() {
    return instantToCommonDate(payment.getCreationDatetime());
  }

  public school.hei.haapi.endpoint.rest.model.Payment.TypeEnum getPaymentType() {
    return payment.getType();
  }
}
