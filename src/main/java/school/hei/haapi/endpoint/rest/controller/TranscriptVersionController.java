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
public class TranscriptVersionController {
    private final TranscriptVersionService service;
    private final TranscriptVersionMapper mapper;

   @GetMapping("/students/{student_id}/transcripts/{transcript_id}/versions")
    public List<StudentTranscriptVersion> getVersions(
            @RequestParam(value = "student_id") String studentId,
            @RequestParam(value = "transcript_id") String transcriptId,
            @RequestParam(defaultValue = "1") PageFromOne page,
            @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize){
       return service.getAllVersions(studentId,transcriptId,page,pageSize).stream()
               .map(mapper::toRest)
               .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping("/students/{student_id}/transcripts/{transcript_id}/versions/{version_id}")
    public StudentTranscriptVersion getTranscriptVersion(
            @RequestParam(value = "student_id") String studentId,
            @RequestParam(value = "transcript_id") String transcriptId,
            @RequestParam(value = "version_id",defaultValue = "latest") String vId){
       return mapper.toRest(service.getTranscriptVersion(studentId,transcriptId,vId));
    }
    @GetMapping("students/{student_id}/transcripts/{transcript_id}/versions/{version_id}/raw")
    public void getPDF(
            @RequestParam(value = "student_id") String studentId,
            @RequestParam(value = "transcript_id") String transcriptId,
            @RequestParam(value = "version_id",defaultValue = "latest") String vId){}

}
