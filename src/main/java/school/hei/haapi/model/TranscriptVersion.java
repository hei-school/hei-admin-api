package school.hei.haapi.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"transcript_version\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptVersion implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    private Integer ref;

    @CreationTimestamp
    private Instant creationDatetime;

    private String pdf_link;

    @ManyToOne
    @JoinColumn(name = "transcript_id", referencedColumnName = "id")
    private Transcript transcript;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        TranscriptVersion transcriptVersion = (TranscriptVersion) o;
        return id != null && Objects.equals(id, transcriptVersion.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
