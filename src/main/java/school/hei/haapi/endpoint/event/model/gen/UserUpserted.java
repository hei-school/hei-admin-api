package school.hei.haapi.endpoint.event.model.gen;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("EventBridge")
public class UserUpserted implements Serializable {
  private static final long serialVersionUID = 1L;

  @JsonProperty("userId")
  private String userId = null;

  @JsonProperty("email")
  private String email = null;

  public UserUpserted userId(String userId) {
    this.userId = userId;
    return this;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public UserUpserted email(String email) {
    this.email = email;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserUpserted userUpserted = (UserUpserted) o;
    return Objects.equals(this.userId, userUpserted.userId)
        && Objects.equals(this.email, userUpserted.email);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(userId, email);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserUpserted {\n");

    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
