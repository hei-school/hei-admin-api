package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.FeeMapper;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.service.FeeService;

@RestController
@AllArgsConstructor
public class FeeController {

  private final FeeService feeService;
  private final FeeMapper feeMapper;

  @GetMapping("/students/{studentId}/fees/{feeId}")
  public Fee getStudentFeeById(@PathVariable String studentId, @PathVariable String feeId) {
    return feeMapper.toRestFee(feeService.getByUserIdAndId(studentId, feeId));
  }
}
