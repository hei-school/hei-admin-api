package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.FeeMapper;
import school.hei.haapi.endpoint.rest.mapper.FeeTemplateMapper;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.UpdateFeeValidator;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.FeeTemplateService;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class FeeController {
  private final UserService userService;
  private final FeeService feeService;
  private final FeeMapper feeMapper;
  private final UpdateFeeValidator updateFeeValidator;
  private final FeeTemplateService feeTemplateService;
  private final FeeTemplateMapper feeTemplateMapper;

  @GetMapping("/fees/{fee_id}")
  public Fee getFeeById(@PathVariable(name = "fee_id") String id) {
    return feeMapper.toRestFee(feeService.getById(id));
  }

  @GetMapping("/students/{studentId}/fees/{feeId}")
  public Fee getFeeByStudentId(@PathVariable String studentId, @PathVariable String feeId) {
    return feeMapper.toRestFee(feeService.getByStudentIdAndFeeId(studentId, feeId));
  }

  @DeleteMapping("/students/{studentId}/fees/{feeId}")
  public Fee deleteStudentFeeById(
      @PathVariable(name = "studentId") String studentId,
      @PathVariable(name = "feeId") String feeId) {
    return feeMapper.toRestFee(feeService.deleteFeeByStudentIdAndFeeId(studentId, feeId));
  }

  @PostMapping("/students/{studentId}/fees")
  public List<Fee> createFees(
      @PathVariable String studentId, @RequestBody List<CreateFee> toCreate) {
    return feeService
        .saveAll(feeMapper.toDomainFee(userService.findById(studentId), toCreate))
        .stream()
        .map(feeMapper::toRestFee)
        .collect(toUnmodifiableList());
  }

  @PutMapping("/students/{studentId}/fees")
  public List<Fee> updateStudentFees(@PathVariable String studentId, @RequestBody List<Fee> fees) {
    updateFeeValidator.accept(fees, studentId);
    User student = userService.findById(studentId);
    List<school.hei.haapi.model.Fee> domainFeeList =
        fees.stream().map(fee -> feeMapper.toDomain(fee, student)).collect(toList());
    return feeService.updateAll(domainFeeList, studentId).stream()
        .map(feeMapper::toRestFee)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/students/{studentId}/fees")
  public List<Fee> getFeesByStudentId(
      @PathVariable String studentId,
      @RequestParam PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(required = false) FeeStatusEnum status) {
    return feeService.getFeesByStudentId(studentId, page, pageSize, status).stream()
        .map(feeMapper::toRestFee)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/fees")
  public List<Fee> getFees(
      @RequestParam PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(required = false) FeeStatusEnum status,
      @RequestParam(name = "isMpbs", required = false) boolean isMpbs,
      @RequestParam(name = "student_ref", required = false) String studentRef) {
    return feeService.getFees(page, pageSize, status, isMpbs, studentRef).stream()
        .map(feeMapper::toRestFee)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/fees/templates")
  public List<FeeTemplate> getFeeTemplates(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "amount", required = false) Integer amount,
      @RequestParam(value = "number_of_payments", required = false) Integer numberOfPayments,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "10") BoundedPageSize pageSize) {
    return feeTemplateService
        .getFeeTemplates(name, amount, numberOfPayments, page, pageSize)
        .stream()
        .map(feeTemplateMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/fees/templates/{id}")
  public FeeTemplate getFeeTemplateById(@PathVariable String id) {
    return feeTemplateMapper.toRest(feeTemplateService.getFeeTemplateById(id));
  }

  @PutMapping("/fees/templates/{id}")
  public FeeTemplate createOrUpdateFeeTemplate(
      @PathVariable String id, @RequestBody CrupdateFeeTemplate feeType) {
    return feeTemplateMapper.toRest(
        feeTemplateService.createOrUpdateFeeTemplate(feeTemplateMapper.toDomain(feeType)));
  }
}
