package school.hei.haapi.model;

public record PersonSnapshot(String fullName, String nic, String address, String phone) {
  public PersonSnapshot(User user) {
    this(
        user.getFirstName() + " " + user.getLastName(),
        user.getNic(),
        user.getAddress(),
        user.getPhone());
  }
}
