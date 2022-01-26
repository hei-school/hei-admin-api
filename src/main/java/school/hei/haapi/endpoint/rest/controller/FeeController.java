package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.FeeMapper;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.endpoint.rest.security.model.Role;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.service.FeeService;

@RestController
@AllArgsConstructor
public class FeeController {

  private final FeeService feeService;
  private final FeeMapper feeMapper;

  @GetMapping("/students/{studentId}/fees/{feeId}")
  public Fee getStudentFeeById(
      @AuthenticationPrincipal Principal principal,
      @PathVariable String studentId,
      @PathVariable String feeId) {
    if (Role.STUDENT.getRole().equals(principal.getRole())
        && !studentId.equals(principal.getUser().getId())) {
      throw new ForbiddenException();
    }
    return feeMapper.toRestFee(feeService.getByUserIdAndId(studentId, feeId));
  }
}
