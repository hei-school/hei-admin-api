package school.hei.haapi.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "cor_last_comment")
@Immutable
public class CorLastComment extends CorCommentBase {}
