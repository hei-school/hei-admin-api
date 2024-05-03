package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdatePromotion;
import school.hei.haapi.endpoint.rest.model.Promotion;

@Component
@AllArgsConstructor
public class PromotionMapper {

  private final GroupMapper groupMapper;

  public Promotion toRest(school.hei.haapi.model.Promotion domain) {
    return new Promotion()
        .id(domain.getId())
        .name(domain.getName())
        .creationDatetime(domain.getCreationDatetime())
        .ref(domain.getRef())
        .groups(
            domain.getGroups() == null
                ? List.of()
                : domain.getGroups().stream().map(groupMapper::toRestGroupIdentifier).toList());
  }

  public school.hei.haapi.model.Promotion toDomain(CrupdatePromotion rest) {
    return school.hei.haapi.model.Promotion.builder()
        .id(rest.getId())
        .name(rest.getName())
        .ref(rest.getRef())
        .build();
  }
}
