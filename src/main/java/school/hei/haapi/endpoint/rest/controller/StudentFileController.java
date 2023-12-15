package school.hei.haapi.endpoint.rest.controller;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.service.StudentFileService;

@RestController
@AllArgsConstructor
public class StudentFileController {
  private final StudentFileService fileService;

  @GetMapping(
      value = "/students/{id}/scholarship_certificate/raw",
      produces = APPLICATION_PDF_VALUE)
  public byte[] getStudentScholarshipCertificate(@PathVariable(name = "id") String studentId) {
    return fileService.generatePdf(studentId, "scolarity");
  }
}
