package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.TranscriptMapper;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.service.TranscriptService;

@RestController
@AllArgsConstructor
public class TranscriptController {
  private final TranscriptService service;
  private final TranscriptMapper mapper;

  @PutMapping("/students/{id}/transcripts")
  public List<Transcript> crupdateTranscripts(@PathVariable("id") String studentId,
                                              @RequestBody List<Transcript> transcripts) {
    var toSave = transcripts.stream()
        .map(mapper::toDomain)
        .collect(Collectors.toUnmodifiableList());
    return service.crupdateTranscripts(toSave).stream()
        .map(mapper::toRest)
        .collect(Collectors.toList());
  }
}
