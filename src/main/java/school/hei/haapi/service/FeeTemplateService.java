package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.FeeTemplate;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.FeeTemplateRepository;
import school.hei.haapi.repository.dao.FeeTemplateDao;

@Service
@AllArgsConstructor
public class FeeTemplateService {

  private final FeeTemplateRepository feeTemplateRepository;
  private final FeeTemplateDao feeTemplateDao;

  public List<FeeTemplate> getFeeTemplates(
      String name,
      Integer totalAmount,
      Integer numberOfMonths,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return feeTemplateDao.findByCriteria(name, totalAmount, numberOfMonths, pageable);
  }

  public FeeTemplate getFeeTemplateById(String id) {
    return feeTemplateRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("FeeTemplate with id: " + id + " not found"));
  }

  public FeeTemplate getFeeTemplateByName(String name) {
    return feeTemplateRepository
        .findByName(name)
        .orElseThrow(() -> new NotFoundException("FeeTemplate with name: " + name + " not found"));
  }

  public FeeTemplate createOrUpdateFeeTemplate(FeeTemplate domain) {
    Optional<FeeTemplate> optionalFeeTemplate = feeTemplateRepository.findById(domain.getId());
    FeeTemplate feeTemplateToPersist =
        optionalFeeTemplate
            .map(
                feeTemplate -> {
                  feeTemplate.setAmount(domain.getAmount());
                  feeTemplate.setNumberOfPayments(domain.getNumberOfPayments());
                  feeTemplate.setType(domain.getType());
                  feeTemplate.setName(domain.getName());
                  return feeTemplate;
                })
            .orElseGet(() -> domain);
    return feeTemplateRepository.save(feeTemplateToPersist);
  }
}
