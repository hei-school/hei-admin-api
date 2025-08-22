package school.hei.haapi.model.notEntity;

import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;

public record UpdateGrade(Grade grade, User student, String comment, Exam exam) {}
