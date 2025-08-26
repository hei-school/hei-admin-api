package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.GradeChangeHistoryMapper;
import school.hei.haapi.endpoint.rest.model.GradeHistory;
import school.hei.haapi.service.GradeService;

@RestController
@AllArgsConstructor
public class GradeChangeHistoryController {
  private GradeService gradeService;
  private GradeChangeHistoryMapper gradeChangeHistoryMapper;

  @GetMapping("/grades/{grade_id}/history")
  public List<GradeHistory> getGradeHistory(@PathVariable("grade_id") String gradeId) {
    return gradeService.getById(gradeId).getGradeChangeHistories().stream()
        .map(gradeChangeHistoryMapper::toRest)
        .toList();
  }
}
