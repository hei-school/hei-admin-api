package school.hei.haapi.endpoint.rest.controller;

import com.lowagie.text.DocumentException;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.service.StudentFileService;

@RestController
@AllArgsConstructor
public class StudentFIleController {
  private final StudentFileService fileService;

  @GetMapping(
      value = "/students/{id}/scholarship_certificate/raw",
      produces = {MediaType.APPLICATION_PDF_VALUE})
  public byte[] getStudentScholarshipCertificatePdf(@PathVariable(name = "id") String studentId)
      throws IOException, DocumentException {
    return fileService.generatePdf(studentId);
  }
}
