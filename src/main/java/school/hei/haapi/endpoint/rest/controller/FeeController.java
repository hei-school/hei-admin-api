package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.FeeMapper;
import school.hei.haapi.endpoint.rest.mapper.FeeTypeMapper;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.FeeType;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.UpdateFeeValidator;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.FeeTypeService;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class FeeController {
  private final UserService userService;
  private final FeeService feeService;
  private final FeeMapper feeMapper;
  private final UpdateFeeValidator updateFeeValidator;
  private final FeeTypeService feeTypeService;
  private final FeeTypeMapper feeTypeMapper;

  @GetMapping("/students/{studentId}/fees/{feeId}")
  public Fee getFeeByStudentId(@PathVariable String studentId, @PathVariable String feeId) {
    return feeMapper.toRestFee(feeService.getByStudentIdAndFeeId(studentId, feeId));
  }

  @PostMapping("/students/{studentId}/fees")
  public List<Fee> createFees(
      @PathVariable String studentId, @RequestBody List<CreateFee> toCreate) {
    return feeService
        .saveAll(feeMapper.toDomainFee(userService.getById(studentId), toCreate))
        .stream()
        .map(feeMapper::toRestFee)
        .collect(toUnmodifiableList());
  }

  @PutMapping("/students/{studentId}/fees")
  public List<Fee> updateStudentFees(@PathVariable String studentId, @RequestBody List<Fee> fees) {
    updateFeeValidator.accept(fees, studentId);
    User student = userService.getById(studentId);
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

  @GetMapping("/fees/types")
  public List<FeeType> getPredefinedFeeTypes(){
    return feeTypeService.getFeeTypes().stream().map(feeTypeMapper::toRest).collect(toUnmodifiableList());
  }

  @PutMapping("/fees/types")
  public FeeType createOrUpdatePredefinedFeetype(@RequestBody FeeType feeType){
       return feeTypeMapper.toRest(feeTypeService.createOrUpdateFeeTypes(feeTypeMapper.toDomain(feeType)));
  }

}
