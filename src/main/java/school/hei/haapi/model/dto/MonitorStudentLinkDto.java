package school.hei.haapi.model.dto;

public record MonitorStudentLinkDto(String id, String monitorId, String studentId, Status status) {
  public enum Status {
    LINKED,
    DENIED,
    PENDING,
  }
}
