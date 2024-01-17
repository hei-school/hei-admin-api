package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.FeeTemplate;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.FeeTemplateRepository;
import school.hei.haapi.repository.dao.FeeTemplateDao;

@Service
@AllArgsConstructor
public class FeeTemplateService {

  private final FeeTemplateRepository feeTemplateRepository;
  private final FeeTemplateDao feeTemplateDao;

  public List<FeeTemplate> getFeeTypes(
      String name,
      Integer totalAmount,
      Integer numberOfMonths,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return feeTemplateDao.findByCriteria(name, totalAmount, numberOfMonths, pageable);
  }

  public FeeTemplate getFeeTypeById(String id) {
    return feeTemplateRepository.getById(id);
  }

  public FeeTemplate createOrUpdateFeeTypes(FeeTemplate domain) {
    return feeTemplateRepository.save(domain);
  }
}
