package school.hei.haapi.model;

@Entity
@Table(name = "\"linked_or_unliked\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Linked_or_unliked {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;
    @OneToMany
    private String User_id;
    @OneToMany
    private String course_id;
    private Status status;
    public enum Status{
        LINKED,UNLIKED
    }
}
