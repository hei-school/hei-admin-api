package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.ComplaintRouting;
import school.hei.haapi.model.ComplaintType;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.ComplaintRoutingRepository;

@Service
@AllArgsConstructor
public class ComplaintRoutingService {
  private final ComplaintRoutingRepository complaintRoutingRepository;

  public ComplaintRouting create(ComplaintType type, User manager) {
    return complaintRoutingRepository.save(
        ComplaintRouting.builder().type(type).manager(manager).build());
  }

  public ComplaintRouting getLast(ComplaintType type) {
    return complaintRoutingRepository
        .findFirstByTypeOrderByCreationDatetimeDesc(type)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "No manager is routed for complaint type #%s".formatted(type)));
  }

  public List<ComplaintRouting> getComplaintRoutings(List<ComplaintType> types, Pageable pageable) {
    if (types == null || types.isEmpty()) {
      return complaintRoutingRepository.findAllByOrderByCreationDatetimeDesc(pageable);
    }
    return complaintRoutingRepository.findByTypeInOrderByCreationDatetimeDesc(types, pageable);
  }
}
