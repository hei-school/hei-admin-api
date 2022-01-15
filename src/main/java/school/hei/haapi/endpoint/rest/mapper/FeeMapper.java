package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Fee;

@Component
public class FeeMapper {
  public Fee toRestFee(school.hei.haapi.model.Fee fee) {
    return new Fee()
        .id(fee.getId())
        .studentId(fee.getUserId())
        .status(fee.getStatus())
        .type(fee.getType())
        .totalAmount(fee.getTotalAmount())
        .remainingAmount(fee.getRemainingAmount())
        .comment(fee.getComment())
        .creationDatetime(fee.getCreationDatetime())
        .dueDatetime(fee.getDueDatetime());
  }
}
