package school.hei.haapi.endpoint.rest.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateMpbs;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CreateMpbsValidator {
  public void accept(String studentId, String feeId, CreateMpbs payload) {
    if (payload.getStudentId() == null
        || payload.getStudentId().isEmpty()
        || payload.getStudentId().isBlank()) {
      throw new BadRequestException("Student id is mandatory");
    }
    if (payload.getFeeId() == null
        || payload.getFeeId().isEmpty()
        || payload.getFeeId().isBlank()) {
      throw new BadRequestException("Fee id is mandatory");
    }
    if (!studentId.equals(payload.getStudentId())) {
      throw new BadRequestException(
          "Student Path = " + studentId + " doesn't match to student in payload");
    }
    if (!feeId.equals(payload.getFeeId())) {
      throw new BadRequestException("Fee Path = " + feeId + " doesn't match to fee in payload");
    }
    if (payload.getPspId() == null
        || payload.getPspId().isBlank()
        || payload.getPspId().isEmpty()) {
      throw new BadRequestException("Psp id is mandatory");
    }
    if (payload.getPspType() == null) {
      throw new BadRequestException("Psp type is mandatory");
    }
  }
}
