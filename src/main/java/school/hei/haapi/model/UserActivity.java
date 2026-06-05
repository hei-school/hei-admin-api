package school.hei.haapi.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_activity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "user_email")
  private String userEmail;

  @Column(name = "endpoint", nullable = false)
  private String endpoint;

  @Column(name = "http_method")
  private String httpMethod;

  @Column(name = "request_body", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String requestBody;

  @Column(name = "created_at")
  private Instant createdAt;
}
