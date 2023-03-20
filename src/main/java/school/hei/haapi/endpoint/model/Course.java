package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.model.CourseTemplate;
import io.swagger.model.Teacher;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Course
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2023-03-20T17:20:51.776723221Z[GMT]")


public class Course extends CourseTemplate  {
  @JsonProperty("main_teacher")
  private Teacher mainTeacher = null;

  public Course mainTeacher(Teacher mainTeacher) {
    this.mainTeacher = mainTeacher;
    return this;
  }

  /**
   * Get mainTeacher
   * @return mainTeacher
   **/
  @Schema(description = "")
  
    @Valid
    public Teacher getMainTeacher() {
    return mainTeacher;
  }

  public void setMainTeacher(Teacher mainTeacher) {
    this.mainTeacher = mainTeacher;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Course course = (Course) o;
    return Objects.equals(this.mainTeacher, course.mainTeacher) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mainTeacher, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Course {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    mainTeacher: ").append(toIndentedString(mainTeacher)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
