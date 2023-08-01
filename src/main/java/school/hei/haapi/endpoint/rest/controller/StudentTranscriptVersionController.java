package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.StudentTranscriptVersionMapper;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.StudentTranscriptVersionService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class StudentTranscriptVersionController {

  private final StudentTranscriptVersionService studentTranscriptVersionService;
  private final StudentTranscriptVersionMapper studentTranscriptVersionMapper;

  @GetMapping(value = "/students/{studentId}/transcripts/{transcriptId}/versions/{versionId}")
  public StudentTranscriptVersion getStudentTranscriptVersion(@PathVariable String studentId,
                                             @PathVariable String transcriptId,
                                             @PathVariable String versionId   ) {
    return studentTranscriptVersionMapper.toRest(studentTranscriptVersionService.getByIdAndStudentIdAndTranscriptId(versionId, transcriptId, studentId));
  }
}
