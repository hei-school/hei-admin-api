package school.hei.haapi.endpoint.rest.controller;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DocumentMapper;
import school.hei.haapi.endpoint.rest.model.Document;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.service.StudentFileService;

@RestController
@AllArgsConstructor
public class StudentFileController {
  private final StudentFileService fileService;
  private final DocumentMapper documentMapper;

  @GetMapping(
      value = "/students/{id}/scholarship_certificate/raw",
      produces = APPLICATION_PDF_VALUE)
  public byte[] getStudentScholarshipCertificate(@PathVariable(name = "id") String studentId) {
    return fileService.generatePdf(studentId, "scolarity");
  }

  @PostMapping(value = "/students/{student_id}/files/raw")
  public Document uploadStudentFile(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(name = "file_name") String fileName,
      @RequestParam(name = "file_type") FileType fileType,
      @RequestBody byte[] toUpload) {
    return documentMapper.toRest(
        fileService.uploadStudentFile(fileName, fileType, studentId, toUpload));
  }

  @GetMapping(value = "/students/{student_id}/files")
  public List<Document> getStudentFiles(@PathVariable(name = "student_id") String studentId) {
    return fileService.getStudentFiles(studentId).stream()
        .map(documentMapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping(value = "/students/{student_id}/files/{id}")
  public Document getStudentFilesById(
      @PathVariable(name = "student_id") String studentId,
      @PathVariable(name = "id") String fileId) {
    return documentMapper.toRest(fileService.getStudentFileById(studentId, fileId));
  }
}
