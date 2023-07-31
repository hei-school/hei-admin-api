package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.TranscriptMapper;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.TranscriptService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class TranscriptController {

  private final TranscriptService transcriptService;
  private final TranscriptMapper transcriptMapper;

  @GetMapping(value = "/students/{studentId}/transcripts/{transcriptId}")
  public Transcript getStudentTranscriptById(@PathVariable String studentId,
                                             @PathVariable String transcriptId) {
    return transcriptMapper.toRest(transcriptService.getByIdAndStudentId(transcriptId, studentId));
  }

  @GetMapping(value = "/students/{studentId}/transcripts")
  public List<Transcript> getStudentTranscripts(@PathVariable String studentId,
                                         @RequestParam PageFromOne page,
                                         @RequestParam("page_size") BoundedPageSize pageSize) {
    return transcriptService.getAllTranscriptsByStudentId(studentId, page, pageSize).stream()
        .map(transcriptMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @PutMapping(value = "/students/{studentId}/transcripts")
  public List<Transcript> crudStudentTranscripts(@PathVariable String studentId,
                                                 @RequestBody List<Transcript> toWrite) {
    var saved = transcriptService.saveAll(toWrite.stream()
        .map(transcript -> transcriptMapper.toDomain(transcript, studentId))
        .collect(toUnmodifiableList()));
    return saved.stream()
        .map(transcriptMapper::toRest)
        .collect(toUnmodifiableList());
  }
}
