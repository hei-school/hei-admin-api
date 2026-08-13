package school.hei.haapi.model;

public record PersonSnapshot(String fullName, String nic, String address, String phone) {
  public PersonSnapshot(User user) {
    this(
        user.getFirstName() + " " + user.getLastName(),
        user.getNic(),
        user.getAddress(),
        user.getPhone());
  }

  public String getAddressField() {
    return address;
  }

  public String getPhoneField() {
    return phone;
  }

  public String getNicField() {
    return nic;
  }

  public String field(String labelKeyword) {
    return switch (labelKeyword) {
      case "adresse personnelle" -> address;
      case "telephone" -> phone;
      case "titulaire de la cin" -> nic;
      default -> null;
    };
  }
}
