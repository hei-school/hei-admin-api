package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.FeeType;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.FeeTypeRepository;
import school.hei.haapi.repository.dao.FeeTypeDao;

@Service
@AllArgsConstructor
public class FeeTypeService {

  private final FeeTypeRepository feeTypeRepository;
  private final FeeTypeDao feeTypeDao;

  public List<FeeType> getFeeTypes(
      String name,
      Integer totalAmount,
      Integer numberOfMonths,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return feeTypeDao.findByCriteria(name, totalAmount, numberOfMonths, pageable);
  }

  public FeeType getFeeTypeById(String id) {
    return feeTypeRepository.getById(id);
  }

  public FeeType createOrUpdateFeeTypes(FeeType domain) {
    return feeTypeRepository.save(domain);
  }
}
