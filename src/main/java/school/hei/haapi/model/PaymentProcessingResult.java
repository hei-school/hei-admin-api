package school.hei.haapi.model;

import java.util.List;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsVerification;

public record PaymentProcessingResult(
    List<MpbsVerification> verifiedMpbs, List<Mpbs> unverifiedMpbs) {}
