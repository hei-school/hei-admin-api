package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkDocument;

public class WorkDocumentTestData {
  public static WorkDocument aWorkDocument(
      User student,
      String filename,
      ProfessionalExperienceFileTypeEnum professionalExperience,
      Instant commitmentBegin) {
    return WorkDocument.builder()
        .id(randomUUID().toString())
        .student(student)
        .filename(filename)
        .professionalExperienceType(professionalExperience)
        .commitmentBegin(commitmentBegin)
        .filePath("STUDENT/%s/WORK_DOCUMENT/%s.pdf".formatted(student.getRef(), randomUUID()))
        .build();
  }
}
