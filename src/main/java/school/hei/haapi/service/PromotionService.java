package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.UpdatePromotionSGroup;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.dao.PromotionDao;

@Service
@AllArgsConstructor
public class PromotionService {
  private final PromotionRepository promotionRepository;
  private final PromotionDao promotionDao;
  private final GroupService groupService;

  public List<Promotion> getPromotions(
      String name, String ref, String groupRef, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(
            page.getValue() - 1,
            pageSize.getValue(),
            Sort.by(DESC, "creationDatetime").and(Sort.by(DESC, "ref")));
    return promotionDao.findByCriteria(name, ref, groupRef, pageable);
  }

  public Promotion getPromotionById(String id) {
    return promotionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Promotion with #id " + id + " not found"));
  }

  public Promotion crupdatePromotion(Promotion domain) {
    Optional<Promotion> optionalPromotion = promotionRepository.findById(domain.getId());
    if (optionalPromotion.isPresent()) {
      optionalPromotion.get().setRef(domain.getRef());
      optionalPromotion.get().setName(domain.getName());
      return promotionRepository.save(optionalPromotion.get());
    }
    return promotionRepository.save(domain);
  }

  public Promotion updateGroups(String promotionId, UpdatePromotionSGroup updatePromotionSGroup) {
    Promotion promotion = getPromotionById(promotionId);
    for (String groupId : updatePromotionSGroup.getGroupIds()) {
      switch (updatePromotionSGroup.getType()) {
        case ADD -> groupService.updateGroups(promotion, groupId);
        case REMOVE -> groupService.updateGroups(null, groupId);
      }
    }

    return promotionRepository.save(promotion);
  }
}
