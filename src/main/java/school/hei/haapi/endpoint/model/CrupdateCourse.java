package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.model.CourseTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * CrupdateCourse
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2023-03-20T17:20:51.776723221Z[GMT]")


public class CrupdateCourse extends CourseTemplate  {
  @JsonProperty("main_teacher_id")
  private String mainTeacherId = null;

  public CrupdateCourse mainTeacherId(String mainTeacherId) {
    this.mainTeacherId = mainTeacherId;
    return this;
  }

  /**
   * Get mainTeacherId
   * @return mainTeacherId
   **/
  @Schema(description = "")
  
    public String getMainTeacherId() {
    return mainTeacherId;
  }

  public void setMainTeacherId(String mainTeacherId) {
    this.mainTeacherId = mainTeacherId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CrupdateCourse crupdateCourse = (CrupdateCourse) o;
    return Objects.equals(this.mainTeacherId, crupdateCourse.mainTeacherId) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mainTeacherId, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CrupdateCourse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    mainTeacherId: ").append(toIndentedString(mainTeacherId)).append("\n");
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
