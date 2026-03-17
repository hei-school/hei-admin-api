package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.StatusCheckMapper;
import school.hei.haapi.endpoint.rest.model.StatusCheck;
import school.hei.haapi.endpoint.rest.model.StatusCheckResult;
import school.hei.haapi.service.StatusCheckService;

@RestController
@AllArgsConstructor
public class StatusCheckController {
  private final StatusCheckService service;
  private final StatusCheckMapper mapper;

  @GetMapping("/status-checks")
  public List<StatusCheck> getAllStatusChecks(
      @RequestParam(required = false) StatusCheckResult result) {
    return service.getAllByResult(result).stream().map(mapper::toRest).toList();
  }
}
