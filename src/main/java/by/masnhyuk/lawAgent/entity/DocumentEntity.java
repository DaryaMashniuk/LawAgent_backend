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
    @Column(nullable = false, length = 1000)
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String title;

    @Column(nullable = true)
    private Integer number;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "document_categories", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "category")
    private Set<DocumentCategory> categories = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private DocumentCategory groupCategory;

    @Column(name = "source_url")
    private String sourceUrl;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String details;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentVersion> versions = new ArrayList<>();
}