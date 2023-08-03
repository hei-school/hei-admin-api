package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.FileMapper;
import school.hei.haapi.endpoint.rest.mapper.TranscriptVersionMapper;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.TranscriptVersionService;
import school.hei.haapi.service.aws.S3Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class TranscriptVersionController {
    private final TranscriptVersionService service;
    private final TranscriptVersionMapper mapper;
    private final S3Service s3Service;

   @GetMapping("/students/{student_id}/transcripts/{transcript_id}/versions")
    public List<StudentTranscriptVersion> getVersions(
            @PathVariable(value = "student_id") String studentId,
            @PathVariable(value = "transcript_id") String transcriptId,
            @RequestParam(value = "page",defaultValue = "1") PageFromOne page,
            @RequestParam(value = "page_size",defaultValue = "15") BoundedPageSize pageSize){
       return service.getTranscriptsVersions(transcriptId,page,pageSize)
                .stream()
               .map(mapper::toRest)
               .collect(Collectors.toList());
    }

    @GetMapping("/students/{student_id}/transcripts/{transcript_id}/versions/{version_id}")
    public StudentTranscriptVersion getTranscriptVersion(
            @PathVariable(value = "student_id") String studentId,
            @PathVariable(value = "transcript_id") String transcriptId,
            @PathVariable(value = "version_id") String versionId){
       return mapper.toRest(service.getTranscriptVersion(transcriptId,versionId));
    }

    @GetMapping("students/{student_id}/transcripts/{transcript_id}/versions/{version_id}/raw")
    public ResponseEntity<ByteArrayResource> getTranscriptVersionAsPdf(
            HttpServletResponse response,
            @PathVariable(value = "student_id") String studentId,
            @PathVariable(value = "transcript_id") String transcriptId,
            @PathVariable(value = "version_id") String versionId) {
       byte[] file = service.getTranscriptVersionPdfByStudentIdAndTranscriptIdAndVersionId(transcriptId,versionId);
       String filename = service.getTranscriptVersion(transcriptId,versionId).getPdfLink();
       return FileMapper.customFileResponse(file,filename,"application/pdf");
    }
    @PostMapping("/students/{studentId}/transcripts/{transcriptId}/versions/latest/raw")
    public StudentTranscriptVersion addNewTranscriptVersionWithPdf(@PathVariable String studentId,
                                                            @PathVariable String transcriptId,
                                                            @RequestPart("pdf_file")MultipartFile pdfFile){

        String editorId = AuthProvider.getPrincipal().getUserId();

        return mapper.toRest(service.addNewTranscriptVersion(studentId,transcriptId, editorId, pdfFile));
    }
}
