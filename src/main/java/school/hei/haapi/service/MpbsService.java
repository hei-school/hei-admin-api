package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsStatusHistory;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.MpbsStatusHistoryRepository;

@Service
@AllArgsConstructor
@Slf4j
public class MpbsService {
  private final MpbsRepository mpbsRepository;
  private final FeeService feeService;
  private final MultipartFileConverter multipartFileConverter;
  private final MpbsStatusHistoryRepository mpbsStatusHistoryRepository;

  public List<Mpbs> saveAll(List<Mpbs> toSave) {
    var mpbs = mpbsRepository.saveAll(toSave);
    mpbsStatusHistoryRepository.saveAll(toSave.stream().map(MpbsStatusHistory::fromMpbs).toList());
    return mpbs;
  }

  public Mpbs save(Mpbs toSave) {
    return saveAll(List.of(toSave)).getFirst();
  }

  public Mpbs saveMpbs(Mpbs mobilePaymentByStudentToSave) {
    Fee fee = mobilePaymentByStudentToSave.getFee();
    mobilePaymentByStudentToSave.setFee(feeService.pendFeeForMpbs(fee));
    return save(mobilePaymentByStudentToSave);
  }

  public List<Mpbs> getStudentMobilePaymentByFeeId(String studentId, String feeId) {
    return mpbsRepository.findByStudentIdAndFeeId(studentId, feeId);
  }

  public Mpbs getByPspId(String pspId) {
    return mpbsRepository
        .findByPspId(pspId)
        .orElseThrow(() -> new NotFoundException("Mpbs not found #" + pspId));
  }

  public Long countPendingOfStudent(String studentId) {
    return mpbsRepository.countMpbsByStatusAndStudentId(MpbsStatus.PENDING, studentId);
  }
}
