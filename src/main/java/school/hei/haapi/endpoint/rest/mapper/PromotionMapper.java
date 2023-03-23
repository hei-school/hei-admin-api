package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Promotion;

@Component
public class PromotionMapper {

    public Promotion toRest(school.hei.haapi.model.Promotion domain) {
        Promotion restPromotion = new Promotion();
        restPromotion.setId(domain.getId());
        restPromotion.setPromotionBegin(domain.getPromotionBegin());
        restPromotion.setPromotionEnd(domain.getPromotionEnd());
        restPromotion.setPromotionRange(domain.getPromotionRange());
        return restPromotion;
    }

}