package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.TranscriptVersionMapper;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.TranscriptVersionService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class TranscriptionVersionController {
    private final TranscriptVersionService service;
    private final TranscriptVersionMapper mapper;

   @GetMapping("/students/{sId}/transcripts/{tId}/versions")
    public List<StudentTranscriptVersion> getVersions(
            @RequestParam(value = "sId") String studentId,
            @RequestParam(value = "tId") String transcriptId,
            @RequestParam(defaultValue = "1") PageFromOne page,
            @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize){
       return service.getTranscriptsVersions(transcriptId,page,pageSize).stream()
               .map(mapper::toRest)
               .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping("/students/{sId}/transcripts/{tId}/versions/{vId}")
    public StudentTranscriptVersion getTranscriptVersion(
            @RequestParam(value = "sId") String studentId,
            @RequestParam(value = "tId") String transcriptId,
            @RequestParam(value = "vId",defaultValue = "latest") String vId){
       return mapper.toRestPdf(service.getTranscriptVersion(transcriptId,vId));
    }
}
