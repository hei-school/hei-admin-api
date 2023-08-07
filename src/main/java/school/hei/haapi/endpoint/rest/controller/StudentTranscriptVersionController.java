package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.StudentTranscriptVersionMapper;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.StudentTranscriptVersionService;

import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

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
    school.hei.haapi.model.StudentTranscriptVersion version = studentTranscriptVersionService.getByIdAndStudentIdAndTranscriptId(versionId, transcriptId, studentId);
    if(Objects.isNull(version))
      throw new NotFoundException("Version Not Found");
    else
      return studentTranscriptVersionMapper.toRest(version);
  }

  @GetMapping(value = "/students/{studentId}/transcripts/{transcriptId}/versions")
  public List<StudentTranscriptVersion> getTranscriptsVersions(
          @PathVariable String studentId,
          @PathVariable String transcriptId,
          @RequestParam PageFromOne page,
          @RequestParam("page_size") BoundedPageSize pageSize) {
    return studentTranscriptVersionService.getAllByStudentIdAndTranscriptId(studentId, transcriptId,page, pageSize).stream()
            .map(studentTranscriptVersionMapper::toRest)
            .collect(toUnmodifiableList());
  }
}
