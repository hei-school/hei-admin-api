package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.*;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.FeeTemplateRepository;
import school.hei.haapi.repository.V2FeeTemplateRepository;
import school.hei.haapi.repository.dao.FeeTemplateDao;

@Service
@AllArgsConstructor
public class FeeTemplateService {
  private final V2FeeTemplateRepository v2FeeTemplateRepository;
  private final FeeTemplateRepository feeTemplateRepository;
  private final FeeTemplateDao feeTemplateDao;

  public List<V2FeeTemplateContent> getFeeTemplateContentsByTemplateId(String templateIdentifier) {
    var optionalFeeTemplate = v2FeeTemplateRepository.findById(templateIdentifier);
    if (optionalFeeTemplate.isEmpty()) {
      throw new NotFoundException("FeeTemplate.id=" + templateIdentifier + " not found");
    }
    return optionalFeeTemplate.get().getFeeTemplateContents();
  }

  public List<V2FeeTemplate> getV2FeeTemplates(Integer page, Integer pageSize) {
    return v2FeeTemplateRepository.findAll(PageRequest.of(page - 1, pageSize)).getContent();
  }

  public List<V2FeeTemplate> crupdateV2FeeTemplates(List<V2FeeTemplate> v2FeeTemplates) {
    var feeTemplates =
        v2FeeTemplates.stream()
            .map(
                provided -> {
                  var optionalV2FeeTemplate = v2FeeTemplateRepository.findById(provided.getId());
                  var actualFeeTemplate = optionalV2FeeTemplate.orElse(provided);
                  return actualFeeTemplate.toBuilder()
                      .label(provided.getLabel())
                      .type(provided.getType())
                      .category(provided.getCategory())
                      .build();
                })
            .toList();
    return v2FeeTemplateRepository.saveAll(feeTemplates);
  }

  public List<V2FeeTemplateContent> crupdateV2FeeTemplateContents(
      String templateIdentifier, List<V2FeeTemplateContent> v2FeeTemplateContents) {
    var v2FeeTemplate =
        v2FeeTemplateRepository
            .findById(templateIdentifier)
            .orElseThrow(
                () ->
                    new NotFoundException("Fee template id=" + templateIdentifier + " not found"));
    v2FeeTemplateContents.forEach(content -> content.setFeeTemplate(v2FeeTemplate));
    var savedFeeTemplate =
        v2FeeTemplateRepository.save(
            v2FeeTemplate.toBuilder().feeTemplateContents(v2FeeTemplateContents).build());
    return savedFeeTemplate.getFeeTemplateContents();
  }

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
                  feeTemplate.setCategory(domain.getCategory());
                  feeTemplate.setFrequency(domain.getFrequency());
                  return feeTemplate;
                })
            .orElseGet(() -> domain);
    return feeTemplateRepository.save(feeTemplateToPersist);
  }
}
