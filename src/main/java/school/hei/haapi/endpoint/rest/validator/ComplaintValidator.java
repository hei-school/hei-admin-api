package school.hei.haapi.endpoint.rest.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateComplaint;
import school.hei.haapi.endpoint.rest.model.CrupdateComplaintRouting;
import school.hei.haapi.endpoint.rest.model.UpdateComplaintAssignees;
import school.hei.haapi.endpoint.rest.model.UpdateComplaintStatus;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class ComplaintValidator {
  public void validateCreate(CrupdateComplaint complaint) {
    if (complaint.getType() == null) {
      throw new BadRequestException("Type is mandatory");
    }
    if (complaint.getTitle() == null || complaint.getTitle().isBlank()) {
      throw new BadRequestException("Title is mandatory");
    }
    if (complaint.getDescription() == null || complaint.getDescription().isBlank()) {
      throw new BadRequestException("Description is mandatory");
    }
  }

  public void validateAssignees(UpdateComplaintAssignees dto) {
    if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
      throw new BadRequestException("At least one user id is mandatory");
    }
  }

  public void validateStatusUpdate(UpdateComplaintStatus dto) {
    if (dto.getStatus() == null) {
      throw new BadRequestException("Status is mandatory");
    }
  }

  public void validateRouting(CrupdateComplaintRouting dto) {
    if (dto.getType() == null) {
      throw new BadRequestException("Type is mandatory");
    }
    if (dto.getManagerId() == null || dto.getManagerId().isBlank()) {
      throw new BadRequestException("Manager id is mandatory");
    }
  }
}
