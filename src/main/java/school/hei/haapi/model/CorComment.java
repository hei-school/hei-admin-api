package school.hei.haapi.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "\"cor_comment\"")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class CorComment extends CorCommentBase {}
