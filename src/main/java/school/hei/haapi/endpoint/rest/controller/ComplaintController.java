package school.hei.haapi.endpoint.rest.controller;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.ComplaintMapper;
import school.hei.haapi.endpoint.rest.mapper.ComplaintRoutingMapper;
import school.hei.haapi.endpoint.rest.model.Complaint;
import school.hei.haapi.endpoint.rest.model.ComplaintAttachment;
import school.hei.haapi.endpoint.rest.model.ComplaintRouting;
import school.hei.haapi.endpoint.rest.model.ComplaintStatus;
import school.hei.haapi.endpoint.rest.model.ComplaintStatusHistory;
import school.hei.haapi.endpoint.rest.model.ComplaintType;
import school.hei.haapi.endpoint.rest.model.CrupdateComplaint;
import school.hei.haapi.endpoint.rest.model.CrupdateComplaintRouting;
import school.hei.haapi.endpoint.rest.model.UpdateComplaintAssignees;
import school.hei.haapi.endpoint.rest.model.UpdateComplaintStatus;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.validator.ComplaintValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.service.ComplaintRoutingService;
import school.hei.haapi.service.ComplaintService;
import school.hei.haapi.service.ComplaintStatusHistoryService;
import school.hei.haapi.service.UserService;

@RestController
@RequiredArgsConstructor
public class ComplaintController {
  private final ComplaintService complaintService;
  private final ComplaintRoutingService complaintRoutingService;
  private final ComplaintStatusHistoryService complaintStatusHistoryService;
  private final ComplaintMapper complaintMapper;
  private final ComplaintRoutingMapper complaintRoutingMapper;
  private final ComplaintValidator validator;
  private final UserService userService;
  private final PaginationFromPageAndPageSize paginationFromPageAndPageSize;

  @GetMapping("/complaints")
  public List<Complaint> getComplaints(
      @RequestParam(required = false) PageFromOne page,
      @RequestParam(name = "page_size", required = false) BoundedPageSize pageSize,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(name = "complaint_type", required = false, defaultValue = "")
          List<ComplaintType> types,
      @RequestParam(name = "complaint_status", required = false, defaultValue = "")
          List<ComplaintStatus> statuses,
      @RequestParam(name = "manager_id", required = false) String managerId,
      @RequestParam(name = "assignee_id", required = false) String assigneeId) {
    var principal = AuthProvider.getPrincipal();
    // a teacher only ever sees complaints assigned to them, whatever filter they pass
    var effectiveAssigneeId =
        principal.getUser().getRole() == User.Role.TEACHER ? principal.getUserId() : assigneeId;
    return complaintMapper.toRest(
        complaintService.getComplaints(
            complaintMapper.toDomainTypes(types),
            complaintMapper.toDomainStatuses(statuses),
            managerId,
            effectiveAssigneeId,
            from,
            to,
            paginationFromPageAndPageSize.apply(page, pageSize)));
  }

  @PostMapping("/complaints")
  public Complaint createComplaint(@RequestBody CrupdateComplaint dto) {
    validator.validateCreate(dto);
    var student = AuthProvider.getPrincipal().getUser();
    return complaintMapper.toRest(
        complaintService.create(
            complaintMapper.toDomainType(dto.getType()),
            dto.getTitle(),
            dto.getDescription(),
            student));
  }

  @GetMapping("/complaints/{complaint_id}")
  public Complaint getComplaintById(@PathVariable(name = "complaint_id") String complaintId) {
    return complaintMapper.toRest(complaintService.getById(complaintId));
  }

  @PutMapping("/complaints/{complaint_id}/assignees")
  public Complaint addComplaintAssignees(
      @PathVariable(name = "complaint_id") String complaintId,
      @RequestBody UpdateComplaintAssignees dto) {
    validator.validateAssignees(dto);
    var actor = AuthProvider.getPrincipal().getUser();
    return complaintMapper.toRest(
        complaintService.addAssignees(complaintId, dto.getUserIds(), actor));
  }

  @PutMapping("/complaints/{complaint_id}/status")
  public Complaint updateComplaintStatus(
      @PathVariable(name = "complaint_id") String complaintId,
      @RequestBody UpdateComplaintStatus dto) {
    validator.validateStatusUpdate(dto);
    var actor = AuthProvider.getPrincipal().getUser();
    return complaintMapper.toRest(
        complaintService.changeStatus(
            complaintId, complaintMapper.toDomainStatus(dto.getStatus()), dto.getComment(), actor));
  }

  @GetMapping("/complaints/{complaint_id}/history")
  public List<ComplaintStatusHistory> getComplaintStatusHistory(
      @PathVariable(name = "complaint_id") String complaintId,
      @RequestParam(required = false) PageFromOne page,
      @RequestParam(name = "page_size", required = false) BoundedPageSize pageSize,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    return complaintMapper.toRestHistory(
        complaintStatusHistoryService.getHistory(
            complaintId, from, to, paginationFromPageAndPageSize.apply(page, pageSize)));
  }

  @GetMapping("/students/{student_id}/complaints")
  public List<Complaint> getStudentComplaints(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(required = false) PageFromOne page,
      @RequestParam(name = "page_size", required = false) BoundedPageSize pageSize) {
    return complaintMapper.toRest(
        complaintService.getStudentComplaints(
            studentId, paginationFromPageAndPageSize.apply(page, pageSize)));
  }

  @GetMapping("/complaint-routings")
  public List<ComplaintRouting> getComplaintRoutings(
      @RequestParam(required = false) PageFromOne page,
      @RequestParam(name = "page_size", required = false) BoundedPageSize pageSize,
      @RequestParam(name = "complaint_type", required = false, defaultValue = "")
          List<ComplaintType> types) {
    return complaintRoutingMapper.toRest(
        complaintRoutingService.getComplaintRoutings(
            complaintMapper.toDomainTypes(types),
            paginationFromPageAndPageSize.apply(page, pageSize)));
  }

  @PostMapping("/complaint-routings")
  public ComplaintRouting createComplaintRouting(@RequestBody CrupdateComplaintRouting dto) {
    validator.validateRouting(dto);
    var manager = userService.getById(dto.getManagerId());
    return complaintRoutingMapper.toRest(
        complaintRoutingService.create(complaintMapper.toDomainType(dto.getType()), manager));
  }

  @GetMapping("/complaint-routings/last")
  public ComplaintRouting getLastComplaintRouting(@RequestParam ComplaintType type) {
    return complaintRoutingMapper.toRest(
        complaintRoutingService.getLast(complaintMapper.toDomainType(type)));
  }

  @PostMapping(
      value = "/complaints/{complaint_id}/attachments/raw",
      consumes = MULTIPART_FORM_DATA_VALUE)
  public ComplaintAttachment uploadComplaintAttachment(
      @PathVariable(name = "complaint_id") String complaintId,
      @RequestParam(name = "filename") String fileName,
      @RequestPart(name = "file_to_upload") MultipartFile fileToUpload) {
    var actor = AuthProvider.getPrincipal().getUser();
    return complaintMapper.toRest(
        complaintService.uploadAttachment(complaintId, fileName, fileToUpload, actor));
  }

  @GetMapping("/complaints/{complaint_id}/attachments")
  public List<ComplaintAttachment> getComplaintAttachments(
      @PathVariable(name = "complaint_id") String complaintId) {
    return complaintMapper.toRestAttachments(complaintService.getAttachments(complaintId));
  }
}
