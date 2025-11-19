package school.hei.haapi.integration.test_data;

import school.hei.haapi.endpoint.rest.model.CancelRetakeExamRequest;
import school.hei.haapi.endpoint.rest.model.RetakeExamToCancel;

public class RequestRetakeExam {
  public static CancelRetakeExamRequest cancelRetakeExamRequest1() {
    return new CancelRetakeExamRequest().retakeExamId("retake_exam3_id").reason("Don't have money");
  }

  public static CancelRetakeExamRequest cancelRetakeExamRequest2() {
    return new CancelRetakeExamRequest()
        .retakeExamId("retake_exam4_id")
        .reason("I'll visit my parents");
  }

  public static RetakeExamToCancel retakeExamToCancel() {
    return new RetakeExamToCancel().retakeExamId("retake_exam2_id");
  }

  public static CancelRetakeExamRequest rejectRetakeExamRequest1() {
    return new CancelRetakeExamRequest()
        .retakeExamId("retake_exam3_id")
        .reason("you can pay it latter");
  }

  public static CancelRetakeExamRequest rejectRetakeExamRequest2() {
    return new CancelRetakeExamRequest()
        .retakeExamId("retake_exam3_id")
        .reason("you can go after the retake exam session");
  }
}
