package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.WorkInfoMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateWorkStudyStudent;
import school.hei.haapi.endpoint.rest.model.WorkStudyStudent;
import school.hei.haapi.model.WorkInfo;
import school.hei.haapi.service.WorkInfoService;

@RestController
@AllArgsConstructor
public class WorkInfoController {
  private final WorkInfoService workInfoService;
  private final WorkInfoMapper workInfoMapper;

  @GetMapping("/students/{id}/work_info")
  public WorkStudyStudent getStudentWorkInfo(@PathVariable(name = "id") String studentId) {
    List<WorkInfo> studentWorkInfo = workInfoService.getWorkInfo(studentId);

    return workInfoMapper.toRest(studentId, studentWorkInfo);
  }

  @PutMapping("/students/{id}/work_info")
  public List<WorkStudyStudent> createOrUpdateStudentWorkStudyInfo(
      @PathVariable(name = "id") String studentId,
      @RequestBody List<CrupdateWorkStudyStudent> studentWorkStudyInfos) {
    List<WorkInfo> studentWorkInfoToSave =
        workInfoMapper.toDomain(studentId, studentWorkStudyInfos);

    return List.of(
        workInfoMapper.toRest(studentId, workInfoService.saveAll(studentWorkInfoToSave)));
  }
}
