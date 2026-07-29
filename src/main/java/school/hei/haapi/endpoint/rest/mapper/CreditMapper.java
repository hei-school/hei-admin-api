package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Credit;

@Component
@AllArgsConstructor
public class CreditMapper {
  private final UserMapper userMapper;

  public Credit toRest(school.hei.haapi.model.Credit credit) {
    var identifier = userMapper.toIdentifier(credit.getStudent());
    return new Credit().student(identifier).amount(credit.getAmount());
  }
}
