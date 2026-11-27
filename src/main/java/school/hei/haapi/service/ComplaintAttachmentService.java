package school.hei.haapi.service;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.file.zip.FileTyper;
import school.hei.haapi.model.Complaint;
import school.hei.haapi.model.ComplaintAttachment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.FilenameValidator;
import school.hei.haapi.repository.ComplaintAttachmentRepository;
import school.hei.haapi.repository.ComplaintRepository;
import school.hei.haapi.service.aws.FileService;

@Service
@AllArgsConstructor
public class ComplaintAttachmentService {
  private static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of("image/jpeg", "image/png", "image/webp", "application/pdf");

  private final ComplaintAttachmentRepository complaintAttachmentRepository;
  private final ComplaintRepository complaintRepository;
  private final FileService fileService;
  private final FileTyper fileTyper;
  private final MultipartFileConverter multipartFileConverter;
  private final FilenameValidator filenameValidator;

  public List<ComplaintAttachment> getByComplaintId(String complaintId) {
    getComplaint(complaintId);
    return complaintAttachmentRepository.findAllByComplaint_IdOrderByCreatedAtDesc(complaintId);
  }

  @Transactional
  public ComplaintAttachment upload(
      String complaintId, String fileName, MultipartFile fileToUpload, User actor) {
    filenameValidator.accept(fileName);
    Complaint complaint = getComplaint(complaintId);

    File file = multipartFileConverter.apply(fileToUpload);
    MediaType detectedType = fileTyper.apply(file);
    String contentType = detectedType.getType() + "/" + detectedType.getSubtype();
    if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
      throw new BadRequestException(
          "Unsupported file type: %s. Only JPEG, PNG, WEBP images and PDF files are allowed."
              .formatted(contentType));
    }

    String storageKey =
        "COMPLAINT/%s/%s%s".formatted(complaintId, UUID.randomUUID(), extensionFor(contentType));
    fileService.uploadObjectToS3Bucket(storageKey, file);

    var attachment =
        ComplaintAttachment.builder()
            .complaint(complaint)
            .fileName(fileName)
            .filePath(storageKey)
            .contentType(contentType)
            .uploadedBy(actor)
            .build();
    return complaintAttachmentRepository.save(attachment);
  }

  private Complaint getComplaint(String complaintId) {
    return complaintRepository
        .findById(complaintId)
        .orElseThrow(
            () -> new NotFoundException("Complaint with id #%s not found".formatted(complaintId)));
  }

  private static String extensionFor(String contentType) {
    return switch (contentType) {
      case "image/jpeg" -> ".jpg";
      case "image/png" -> ".png";
      case "image/webp" -> ".webp";
      case "application/pdf" -> ".pdf";
      default -> throw new BadRequestException("Unsupported file type: " + contentType);
    };
  }
}
