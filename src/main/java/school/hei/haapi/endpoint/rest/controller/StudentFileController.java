package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.time.Instant;
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
import school.hei.haapi.endpoint.rest.mapper.WorkDocumentMapper;
import school.hei.haapi.endpoint.rest.model.FileInfo;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.endpoint.rest.model.WorkStudyStatus;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.StudentFileService;

@RestController
@AllArgsConstructor
public class StudentFileController {
  private final StudentFileService fileService;
  private final FileInfoMapper fileInfoMapper;
  private final WorkDocumentMapper workDocumentMapper;

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
      @RequestPart(name = "file_to_upload") MultipartFile fileToUpload) {
    return fileInfoMapper.toRest(
        fileService.uploadStudentFile(fileName, fileType, studentId, fileToUpload));
  }

  @PostMapping(
      value = "/students/{student_id}/work_files/raw",
      consumes = MULTIPART_FORM_DATA_VALUE)
  public FileInfo uploadStudentWorkFile(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(name = "filename", required = true) String filename,
      @RequestParam(name = "work_study_status", required = false) WorkStudyStatus workStatus,
      @RequestParam(name = "commitment_begin", required = true) Instant commitmentBegin,
      @RequestParam(name = "commitment_end", required = true) Instant commitmentEnd,
      @RequestParam(name = "creation_datetime", required = false) Instant creationDatetime,
      @RequestPart(name = "file_to_upload") MultipartFile fileToUpload) {
    return workDocumentMapper.toRest(
        fileService.uploadStudentWorkFile(
            studentId,
            filename,
            creationDatetime,
            commitmentBegin,
            commitmentEnd,
            workStatus,
            fileToUpload));
  }

  @GetMapping(value = "/students/{student_id}/work_files")
  public List<FileInfo> getStudentWorkDocuments(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return fileService.getStudentWorkFiles(studentId, page, pageSize).stream()
        .map(workDocumentMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @GetMapping(value = "/students/{student_id}/files")
  public List<FileInfo> getStudentFiles(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(name = "file_type", required = false) FileType fileType,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return fileService.getStudentFiles(studentId, fileType, page, pageSize).stream()
        .map(fileInfoMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @GetMapping(value = "/students/{student_id}/work_files/{id}")
  public FileInfo getStudentWorkDocumentsById(
      @PathVariable(name = "student_id") String studentId, @PathVariable(name = "id") String id) {
    return workDocumentMapper.toRest(fileService.getStudentWorkFileById(id));
  }

  @GetMapping(value = "/students/{student_id}/files/{id}")
  public FileInfo getStudentFilesById(
      @PathVariable(name = "student_id") String studentId,
      @PathVariable(name = "id") String fileId) {
    return fileInfoMapper.toRest(fileService.getStudentFileById(studentId, fileId));
  }
}
