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
  private final MpbsStatusHistoryRepository mpbsStatusHistoryRepository;

  /**
   * Use MpbsService.saveAll to update mpbs status history
   *
   * @param toSave the mpbs to save
   * @return the saved mpbs
   */
  public List<Mpbs> saveAll(List<Mpbs> toSave) {
    var mpbs = mpbsRepository.saveAll(toSave);
    var mpbsStatusHistoryToUpdate =
        mpbs.stream().map(Mpbs::getStatusHistory).flatMap(List::stream).toList();
    mpbsStatusHistoryRepository.saveAll(
        toSave.stream()
            .map(MpbsStatusHistory::fromMpbs)
            .map(
                mpbsStatusHistory ->
                    mpbsStatusHistoryToUpdate.stream()
                        .filter(mpbsStatusHistory::sameMpbsAndStatus)
                        .findFirst()
                        .orElse(mpbsStatusHistory))
            .toList());
    return mpbs;
  }

  /**
   * Use MpbsService.save to update mpbs status history
   *
   * @param toSave the mpbs to save
   * @return the saved mpbs
   */
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
