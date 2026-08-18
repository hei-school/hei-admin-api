package school.hei.haapi.endpoint.rest.controller;

import static school.hei.haapi.model.PaymentStatus.INVALIDATE;
import static school.hei.haapi.model.PaymentStatus.VALIDATE;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.PaymentMapper;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.endpoint.rest.model.CreditPayment;
import school.hei.haapi.endpoint.rest.model.Payment;
import school.hei.haapi.endpoint.rest.model.PaymentStatus;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.PaymentService;

@RestController
@AllArgsConstructor
public class PaymentController {
  private final PaymentService paymentService;
  private final PaymentMapper paymentMapper;

  @PostMapping("/students/{studentId}/fees/{feeId}/payments")
  public List<Payment> createPayments(
      @PathVariable String feeId,
      @RequestBody List<CreatePayment> toCreate,
      @PathVariable(name = "studentId") String studentId) {
    return paymentService.saveAll(paymentMapper.toDomainPayment(feeId, toCreate)).stream()
        .map(paymentMapper::toRestPayment)
        .toList();
  }

  @PatchMapping("/students/payments/validate")
  public List<CreditPayment> validatePayments(@RequestBody List<String> paymentIds) {
    var payments = paymentService.getByIds(paymentIds);
    payments.forEach(payment -> payment.setStatus(VALIDATE));
    return paymentMapper.toRestCreditPayment(paymentService.saveAll(payments));
  }

  @PatchMapping("/students/payments/reject")
  public List<CreditPayment> rejectPayments(@RequestBody List<String> paymentIds) {
    var payments = paymentService.getByIds(paymentIds);
    payments.forEach(payment -> payment.setStatus(INVALIDATE));
    return paymentMapper.toRestCreditPayment(paymentService.saveAll(payments));
  }

  @DeleteMapping("/students/{studentId}/fees/{feeId}/payments/{paymentId}")
  public Payment deleteStudentFeePaymentById(
      @PathVariable(name = "studentId") String studentId,
      @PathVariable(name = "feeId") String feeId,
      @PathVariable("paymentId") String paymentId) {
    return paymentMapper.toRestPayment(paymentService.deleteFeePaymentById(paymentId));
  }

  @GetMapping("/students/{studentId}/fees/{feeId}/payments")
  public List<Payment> getPaymentsByStudentId(
      @PathVariable String studentId,
      @PathVariable String feeId,
      @RequestParam PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize) {
    return paymentService.getByStudentIdAndFeeId(studentId, feeId, page, pageSize).stream()
        .map(paymentMapper::toRestPayment)
        .toList();
  }

  @GetMapping("/students/credit-payments")
  public List<CreditPayment> getCreditPaymentsByStatus(
      @RequestParam(value = "status", required = false) PaymentStatus status,
      @RequestParam(value = "page", required = false) PageFromOne page,
      @RequestParam(value = "page_size", required = false) BoundedPageSize pageSize) {
    var domainStatus =
        status == null ? null : school.hei.haapi.model.PaymentStatus.valueOf(status.toString());
    return paymentMapper.toRestCreditPayment(
        paymentService.getCreditPaymentsByStatus(domainStatus, page, pageSize));
  }
}
