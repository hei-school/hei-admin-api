package school.hei.haapi.integration.testData;

import school.hei.haapi.endpoint.rest.model.CancelRetakeExamRequest;
import school.hei.haapi.endpoint.rest.model.RetakeExamToCancel;

/** Requests are built against the retake exams the calling test owns, never against seeded ids. */
public class RequestRetakeExam {
  public static CancelRetakeExamRequest cancelRequest(String retakeExamId, String reason) {
    return new CancelRetakeExamRequest().retakeExamId(retakeExamId).reason(reason);
  }

  public static RetakeExamToCancel toCancel(String retakeExamId) {
    return new RetakeExamToCancel().retakeExamId(retakeExamId);
  }
}
