package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/students/{studentId}/transcripts")
    public List<Transcript> getTranscriptByStudentId(
            @PathVariable String studentId,
            @RequestParam PageFromOne page,
            @RequestParam("page_size") BoundedPageSize pageSize){
        return transcriptService.getTranscriptsByStudentId(studentId, page, pageSize)
                .stream().map(transcriptMapper::toRestTranscript)
                .collect(toUnmodifiableList());
    }
}
