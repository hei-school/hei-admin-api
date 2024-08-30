package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Letter;
import school.hei.haapi.endpoint.rest.model.PagedLettersResponse;

@Component
@AllArgsConstructor
public class LetterMapper {

  public Letter toRest(school.hei.haapi.model.Letter domain) {
    return new Letter()
        .id(domain.getId())
        .description(domain.getDescription())
        .creationDatetime(domain.getCreationDatetime())
        .approvalDatetime(domain.getApprovalDatetime())
        .status(domain.getStatus())
        .ref(domain.getRef())
        .studentRef(domain.getStudent().getRef())
        .filePath(domain.getFilePath());
  }

  public PagedLettersResponse toPagedRest(Page<school.hei.haapi.model.Letter> domain) {
    return new PagedLettersResponse()
        .pageNumber(domain.getNumber())
        .pageSize(domain.getSize())
        .data(domain.get().map(this::toRest).toList())
        .count((int) domain.getTotalElements())
        .hasPrevious(domain.hasPrevious());
  }
}
