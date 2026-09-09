package school.hei.haapi.endpoint.rest.mapper;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdatePromotion;
import school.hei.haapi.endpoint.rest.model.Promotion;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.CycleLevel;

@Slf4j
@Component
@AllArgsConstructor
public class PromotionMapper {

  private final GroupMapper groupMapper;

  public Promotion toRest(school.hei.haapi.model.Promotion domain) {
    return new Promotion(currentLevelsOf(domain))
        .id(domain.getId())
        .name(domain.getName())
        .creationDatetime(domain.getCreationDatetime())
        .ref(domain.getRef())
        .groups(
            domain.getGroups() == null
                ? List.of()
                : domain.getGroups().stream().map(groupMapper::toRestGroupIdentifier).toList());
  }

  private List<StudentLevel> currentLevelsOf(school.hei.haapi.model.Promotion domain) {
    if (domain.getStartDatetime() == null) {
      return List.of();
    }
    return domain.findLevelAt(Instant.now()).map(List::of).orElse(List.of());
  }

  public school.hei.haapi.model.Promotion toDomain(CrupdatePromotion rest) {
    return school.hei.haapi.model.Promotion.builder()
        .id(rest.getId())
        .name(rest.getName())
        .ref(rest.getRef())
        .cycleLevel(
            rest.getCycleLevel() == null ? null : CycleLevel.valueOf(rest.getCycleLevel().name()))
        .build();
  }
}
