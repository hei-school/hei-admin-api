package school.hei.haapi.integration.test_data;

import school.hei.haapi.endpoint.rest.model.Reason;

public class RetakeExamCancelReason {
  public static Reason toCancelReason() {
    return new Reason().reason("Don't have money");
  }

  public static Reason rejectionReason() {
    return new Reason().reason("Not valid, you can pay it latter");
  }
}
