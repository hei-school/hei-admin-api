package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MpbsVerification;

@Component
public class MpbsVerificationMapper {

  public MpbsVerification toRest(school.hei.haapi.model.Mpbs.MpbsVerification domain) {
    return new MpbsVerification()
        .feeId(domain.getFee().getId())
        .amountInPsp(domain.getAmountInPsp())
        .creationDatetime(domain.getCreationDatetime())
        .pspType(domain.getMobileMoneyType())
        .comment(domain.getComment())
        .amountOfFeeRemainingPayment(domain.getAmountOfFeeRemainingPayment())
        .creationDatetimeOfMpbs(domain.getCreationDatetimeOfMpbs())
        .pspId(domain.getPspId())
        .studentId(domain.getStudent().getId());
  }
}
