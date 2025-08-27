package school.hei.haapi.endpoint.rest.controller;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.GradeChangeHistoryMapper;
import school.hei.haapi.endpoint.rest.model.GradeHistory;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.dao.GradeHistoryDao;

@RestController
@AllArgsConstructor
public class GradeChangeHistoryController {
  private GradeChangeHistoryMapper gradeChangeHistoryMapper;
  private GradeHistoryDao gradeHistoryDao;

  @GetMapping("/grades/{grade_id}/history")
  public List<GradeHistory> getGradeHistory(
      @PathVariable("grade_id") String gradeId,
      @RequestParam(value = "page", required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", required = false, defaultValue = "15")
          BoundedPageSize pageSize,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(required = false) String comment) {
    return gradeHistoryDao.findByCriteria(page, pageSize, gradeId, from, to, comment).stream()
        .map(gradeChangeHistoryMapper::toRest)
        .toList();
  }
}
