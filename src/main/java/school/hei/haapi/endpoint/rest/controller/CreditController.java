package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CreditMapper;
import school.hei.haapi.endpoint.rest.model.Credit;
import school.hei.haapi.endpoint.rest.model.CreditTransaction;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.TrackActivity;
import school.hei.haapi.service.CreditService;

@RestController
@AllArgsConstructor
@TrackActivity
public class CreditController {
  private final CreditService creditService;
  private final CreditMapper creditMapper;

  @GetMapping("/students/{student_id}/credit")
  public Credit getCreditByStudentId(@PathVariable("student_id") String studentId) {
    return creditMapper.toRest(creditService.getCreditByStudentId(studentId).get());
  }

  @GetMapping("/students/{student_id}/credit-transactions")
  public List<CreditTransaction> getCreditTransactionsByStudentId(
      @PathVariable("student_id") String studentId,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "10") BoundedPageSize pageSize) {
    return creditMapper.toCreditTransactionRest(
        creditService.getCreditTransactionsByStudentId(studentId, page, pageSize));
  }
}
