package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.TranscriptMapper;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.service.TranscriptService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class TranscriptController {
    private TranscriptService transcriptService;
    private TranscriptMapper transcriptMapper;

    @GetMapping(value = "/students/{student_id}/transcripts")
    public List<Transcript> getTranscripts(@PathVariable String student_id) {
        return transcriptService.getAllByStudentId(student_id)
                .stream()
                .map(transcriptMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping(value = "/students/{student_id}/transcripts/{transcript_id}")
    public Transcript getTranscriptById(@PathVariable String student_id, @PathVariable String transcript_id) {
        return transcriptMapper.toRest(transcriptService.getByStudentIdAndId(student_id, transcript_id));
    }
}
