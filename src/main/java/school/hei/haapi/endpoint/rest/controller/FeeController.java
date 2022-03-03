package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.FeeMapper;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.service.FeeService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class FeeController {

  private final FeeService feeService;
  private final FeeMapper feeMapper;

  @GetMapping("/students/{studentId}/fees/{feeId}")
  public Fee getFeeByStudentId(
      @PathVariable String studentId, @PathVariable String feeId) {
    return feeMapper.toRestFee(feeService.getByStudentIdAndFeeId(studentId, feeId));
  }

  @PostMapping("/students/{studentId}/fees")
  public List<Fee> createFees(
      @PathVariable String studentId, @RequestBody List<CreateFee> toCreate) {
    return feeService
        .saveAll(studentId, toCreate.stream()
            .map(feeMapper::toDomainFee)
            . collect(toUnmodifiableList()))
        .stream()
        .map(feeMapper::toRestFee)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/students/{studentId}/fees")
  public List<Fee> getFeesByStudentId(@PathVariable String studentId) {
    return feeService.getFeesByStudentId(studentId).stream()
        .map(feeMapper::toRestFee)
        .collect(toUnmodifiableList());
  }
}
