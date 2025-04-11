package school.hei.haapi.integration.conf.utils;

import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum.WORKER_STUDENT;
import static school.hei.haapi.endpoint.rest.model.Sex.F;
import static school.hei.haapi.endpoint.rest.model.Sex.M;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.COMMON_CORE;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.NOT_WORKING;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.WORKING;
import static school.hei.haapi.integration.conf.utils.TestUtils.coordinatesWithNullValues;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.Student;

public class MockObjects {
  public static Student student1() {
    Student student = new Student();
    student.setId("student1_id");
    student.setFirstName("Ryan");
    student.setLastName("Andria");
    student.setEmail("test+ryan@hei.school");
    student.setRef("STD21001");
    student.setPhone("0322411123");
    student.setStatus(ENABLED);
    student.setSex(M);
    student.setBirthDate(LocalDate.parse("2000-01-01"));
    student.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    student.setAddress("Adr 1");
    student.setNic("");
    student.setSpecializationField(COMMON_CORE);
    student.setBirthPlace("");
    student.setCoordinates(new Coordinates().longitude(-123.123).latitude(123.0));
    student.setHighSchoolOrigin("Lycée Andohalo");
    student.setWorkStudyStatus(WORKING);
    student.setProfessionalExperience(WORKER_STUDENT);
    student.setCommitmentBeginDate(Instant.parse("2021-11-08T08:25:24Z"));
    student.setGroups(List.of(TestUtils.group1(), TestUtils.group2()));
    student.setIsRepeatingYear(false);
    return student;
  }

  public static Student student2() {
    Student student = new Student();
    student.setId("student2_id");
    student.setFirstName("Two");
    student.setLastName("Student");
    student.setEmail("test+student2@hei.school");
    student.setRef("STD21002");
    student.setPhone("0322411124");
    student.setStatus(ENABLED);
    student.setSex(F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    student.setBirthPlace("");
    student.setNic("");
    student.setSpecializationField(COMMON_CORE);
    student.setCoordinates(new Coordinates().longitude(255.255).latitude(-255.255));
    student.setHighSchoolOrigin("Lycée Andohalo");
    student.setWorkStudyStatus(WORKING);
    student.setProfessionalExperience(WORKER_STUDENT);
    student.setCommitmentBeginDate(Instant.parse("2021-11-08T08:25:24.00Z"));
    student.setGroups(List.of(TestUtils.group1()));
    student.setIsRepeatingYear(false);
    return student;
  }

  public static Student student3() {
    Student student = new Student();
    student.setId("student3_id");
    student.setFirstName("Three");
    student.setLastName("Student");
    student.setEmail("test+student3@hei.school");
    student.setRef("STD21003");
    student.setPhone("0322411124");
    student.setStatus(ENABLED);
    student.setSex(F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    student.setBirthPlace("Befelatanana");
    student.setNic("0000000000");
    student.setSpecializationField(COMMON_CORE);
    student.setCoordinates(coordinatesWithNullValues());
    student.setHighSchoolOrigin("Lycée Analamahitsy");
    student.setWorkStudyStatus(NOT_WORKING);
    student.setGroups(List.of());
    student.setIsRepeatingYear(false);
    return student;
  }
}
