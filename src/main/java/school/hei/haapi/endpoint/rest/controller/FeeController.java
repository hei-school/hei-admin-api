package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.FeeMapper;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.FeeService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class FeeController {

  private final FeeService feeService;
  private final FeeMapper feeMapper;

  @GetMapping("/students/{studentId}/fees/{feeId}")
  public Fee getFeeByStudentId(
      @PathVariable String studentId,
      @PathVariable String feeId) {
    return feeMapper.toRestFee(feeService.getByStudentIdAndFeeId(studentId, feeId));
  }

  @PostMapping("/students/{studentId}/fees")
  public List<Fee> createFees(
      @PathVariable String studentId, @RequestBody List<CreateFee> toCreate) {
    return feeService.saveAll(
            feeMapper.toDomainFee(studentId, toCreate)).stream()
        .map(feeMapper::toRestFee)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/students/{studentId}/fees")
  public List<Fee> getFeesByStudentId(
      @PathVariable String studentId,
      @RequestParam PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(required = false) Fee.StatusEnum status) {
    return feeService.getFeesByStudentId(studentId, page, pageSize, status).stream()
        .map(feeMapper::toRestFee)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/fees")
  public List<Fee> getFees(
      @RequestParam PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(required = false) Fee.StatusEnum status) {
    return feeService.getFees(page, pageSize, status).stream()
        .map(feeMapper::toRestFee)
        .collect(toUnmodifiableList());
  }
}
