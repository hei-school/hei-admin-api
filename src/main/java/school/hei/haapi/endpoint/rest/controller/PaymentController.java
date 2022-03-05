package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.PaymentMapper;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.endpoint.rest.model.Payment;
import school.hei.haapi.service.PaymentService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentMapper paymentMapper;

  @PostMapping("/students/{studentId}/fees/{feeId}/payments")
  public List<Payment> createPayments(
      @PathVariable String studentId,
      @PathVariable String feeId,
      @RequestBody List<CreatePayment> toCreate) {
    return paymentService
        .saveAll(studentId, feeId, toCreate.stream()
            .map(paymentMapper::toDomainPayment)
            .collect(toUnmodifiableList()))
        .stream()
        .map(paymentMapper::toRestPayment)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/students/{studentId}/fees/{feeId}/payments")
  public List<Payment> getFeePaymentsByStudentId(
      @PathVariable String studentId, @PathVariable String feeId) {
    return paymentService.getByStudentIdAndFeeId(studentId, feeId).stream()
        .map(paymentMapper::toRestPayment)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/students/{studentId}/fees/payments")
  public List<Payment> getPaymentsByStudentId(@PathVariable String studentId) {
    return paymentService.getByStudentId(studentId).stream()
        .map(paymentMapper::toRestPayment)
        .collect(toUnmodifiableList());
  }
}
