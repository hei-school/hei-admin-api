package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.InterestHistory;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.service.InterestHistoryService;
import school.hei.haapi.service.UserService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class IntrestHistoryController {

  private final InterestHistoryService interestHistoryService;

  @GetMapping(value = "/interest/{fee_id}/fee")
  public List<InterestHistory> getInterest(@PathVariable String fee_id) {
    return interestHistoryService.getAllByFeeId(fee_id);
  }
  @GetMapping(value = "/interest/{fee_id}/fee/amount")
  public int getInterestAmount(@PathVariable String fee_id) {
    return interestHistoryService.getInterestAmount(fee_id);
  }

}
