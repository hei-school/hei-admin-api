package school.hei.haapi.endpoint.rest.security.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
  STUDENT,
  TEACHER,
  MANAGER;

  public String getRole() {
    return name();
  }

  @Override
  public String getAuthority() {
    return "ROLE_" + getRole();
  }
}
