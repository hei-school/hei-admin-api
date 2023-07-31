package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "\"transcript_version\"")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class TranscriptVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private int ref;
    @CreationTimestamp
    private Instant creationDatetime;
    private String pdfLink;
    @ManyToOne
    @JoinColumn(name = "transcript_id",nullable = false)
    private Transcript transcript;
    @ManyToOne
    @JoinColumn(name = "editor_id",nullable = false)
    private User editor;
}
