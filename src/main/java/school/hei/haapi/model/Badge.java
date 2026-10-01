package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Badge {
  private final String lastName;
  private final String firstName;
  private final String ref;
  private final String level;
  private final String photo;
  private final String qrCode;
  private final String lastNameFontSize;
}
