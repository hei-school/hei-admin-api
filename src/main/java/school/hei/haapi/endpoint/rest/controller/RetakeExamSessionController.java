package school.hei.haapi.endpoint.rest.controller;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamSessionMapper;
import school.hei.haapi.endpoint.rest.model.RetakeExamSession;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.RetakeExamSessionService;

@RestController
@RequiredArgsConstructor
public class RetakeExamSessionController {
  private final RetakeExamSessionService retakeExamSessionService;
  private final RetakeExamSessionMapper retakeExamSessionMapper;

  @GetMapping("/retake_exam_sessions")
  public List<RetakeExamSession> getRetakeExamSessions(
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize,
      @RequestParam(value = "from", required = false) Instant from,
      @RequestParam(value = "to", required = false) Instant to) {
    return retakeExamSessionMapper.toRestList(
        retakeExamSessionService.getRetakeExamSessions(title, page, pageSize, from, to));
  }

  @PutMapping("/retake_exam_sessions")
  public RetakeExamSession createOrUpdateRetakeExamSessions(
      @RequestBody RetakeExamSession retakeExamSession) {
    return retakeExamSessionMapper.toRest(
        retakeExamSessionService.save(retakeExamSessionMapper.toDomain(retakeExamSession)));
  }
}
