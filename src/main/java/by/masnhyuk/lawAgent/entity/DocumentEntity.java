package by.masnhyuk.lawAgent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String title;

    @Column(unique = true)
    private String number;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "document_categories", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "category")
    private Set<DocumentCategory> category= new HashSet<>();
    private String sourceUrl;

    @Lob
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String details;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentVersion> versions = new ArrayList<>();
}