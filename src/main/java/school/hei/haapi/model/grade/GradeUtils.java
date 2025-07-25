package school.hei.haapi.model.grade;

import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;

import java.util.List;

public class GradeUtils {
    public static double weightedAverageOfGrades(List<Grade> grades) {
        double sumCoefficients = grades.stream().map(Grade::getExam).mapToDouble(Exam::getCoefficient).sum();
        return grades.stream().mapToDouble(grade -> grade.getScore() * grade.getExam().getCoefficient()).sum() / sumCoefficients;
    }
}
