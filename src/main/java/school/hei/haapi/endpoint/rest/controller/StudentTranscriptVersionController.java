package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.StudentTranscriptVersionMapper;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.StudentTranscriptVersionService;

import java.util.Objects;

@RestController
@AllArgsConstructor
public class StudentTranscriptVersionController {

  private final StudentTranscriptVersionService studentTranscriptVersionService;
  private final StudentTranscriptVersionMapper studentTranscriptVersionMapper;

  @GetMapping(value = "/students/{studentId}/transcripts/{transcriptId}/versions/{versionId}")
  public StudentTranscriptVersion getStudentTranscriptVersion(@PathVariable String studentId,
                                             @PathVariable String transcriptId,
                                             @PathVariable String versionId   ) {
    school.hei.haapi.model.StudentTranscriptVersion version = studentTranscriptVersionService.getByIdAndStudentIdAndTranscriptId(versionId, transcriptId, studentId);
    if(Objects.isNull(version))
      throw new NotFoundException("Version Not Found");
    else
      return studentTranscriptVersionMapper.toRest(version);
  }
}
