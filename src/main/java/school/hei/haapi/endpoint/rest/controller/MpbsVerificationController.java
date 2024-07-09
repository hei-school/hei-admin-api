package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.MpbsVerificationMapper;
import school.hei.haapi.endpoint.rest.model.MpbsVerification;
import school.hei.haapi.service.MpbsVerificationService;

@RestController
@AllArgsConstructor
public class MpbsVerificationController {
  private final MpbsVerificationService mpbsVerificationService;
  private final MpbsVerificationMapper mapper;

  @GetMapping(value = "/students/{student_id}/fees/{fee_id}/mpbs/verifications")
  public List<MpbsVerification> getMpbsVerifications(
      @PathVariable(name = "student_id") String studentId,
      @PathVariable(name = "fee_id") String feeId) {
    return mpbsVerificationService.findAllByStudentIdAndFeeId(studentId, feeId).stream()
        .map(mapper::toRest)
        .collect(toUnmodifiableList());
  }

  @GetMapping(value = "/verify-transactions")
  public List<MpbsVerification> verifyTransactions() {
    if (mpbsVerificationService.checkMobilePaymentThenSaveVerification() == null) {
      return List.of();
    }
    // TODO: this doesn't work but the service script is executed
    return mpbsVerificationService.checkMobilePaymentThenSaveVerification().stream()
        .map(mapper::toRest)
        .collect(toUnmodifiableList());
  }
}
