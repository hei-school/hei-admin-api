package school.hei.haapi.endpoint.rest.validator;


import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class UpdateStudentCourseValidator implements Consumer<UpdateStudentCourse> {
  @Override
  public void accept(UpdateStudentCourse course) {
    StringBuilder messageBuilder = new StringBuilder();
    if (course.getCourseId() == null) {
      messageBuilder.append("CourseId is mandatory. ");
    }
    if (course.getStatus() == null) {
      messageBuilder.append("Status is mandatory.");
    }

    String errorMessage = messageBuilder.toString();
    if (!errorMessage.isEmpty()) {
      throw new BadRequestException(errorMessage);
    }
  }
}
