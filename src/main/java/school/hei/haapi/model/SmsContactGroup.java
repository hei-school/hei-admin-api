package school.hei.haapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Table(name = "sms_contact_group")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class SmsContactGroup implements Serializable {
  @Id private String id;

  private String name;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  private User owner;

  @ManyToMany
  @JoinTable(
      name = "sms_contact_group_member",
      joinColumns = @JoinColumn(name = "sms_contact_group_id"),
      inverseJoinColumns = @JoinColumn(name = "sms_contact_id"))
  @Builder.Default
  private List<SmsContact> members = new ArrayList<>();

  @Builder.Default private boolean isDeleted = false;

  @CreationTimestamp private Instant creationDatetime;
}
