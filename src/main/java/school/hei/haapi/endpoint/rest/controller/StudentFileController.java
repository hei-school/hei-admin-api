package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.FileInfoMapper;
import school.hei.haapi.endpoint.rest.model.FileInfo;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.service.StudentFileService;

@RestController
@AllArgsConstructor
public class StudentFileController {
  private final StudentFileService fileService;
  private final FileInfoMapper fileInfoMapper;

  @GetMapping(
      value = "/students/{id}/scholarship_certificate/raw",
      produces = APPLICATION_PDF_VALUE)
  public byte[] getStudentScholarshipCertificate(@PathVariable(name = "id") String studentId) {
    return fileService.generatePdf(studentId, "scolarity");
  }

  @PostMapping(value = "/students/{student_id}/files/raw", consumes = MULTIPART_FORM_DATA_VALUE)
  public FileInfo uploadStudentFile(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(name = "filename") String fileName,
      @RequestParam(name = "file_type") FileType fileType,
      @RequestPart(name = "file") MultipartFile fileToUpload) {
    return fileInfoMapper.toRest(
        fileService.uploadStudentFile(fileName, fileType, studentId, fileToUpload));
  }

  @GetMapping(value = "/students/{student_id}/files")
  public List<FileInfo> getStudentFiles(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(name = "file_type", required = false) FileType fileType) {
    return fileService.getStudentFiles(studentId, fileType).stream()
        .map(fileInfoMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @GetMapping(value = "/students/{student_id}/files/{id}")
  public FileInfo getStudentFilesById(
      @PathVariable(name = "student_id") String studentId,
      @PathVariable(name = "id") String fileId) {
    return fileInfoMapper.toRest(fileService.getStudentFileById(studentId, fileId));
  }
}
