package school.hei.haapi.model;

import lombok.Value;

@Value
public class Badge {
  String lastName;
  String firstName;
  String ref;
  String level;
  String photo;
  String qrCode;
  String lastNameFontSize;
}
