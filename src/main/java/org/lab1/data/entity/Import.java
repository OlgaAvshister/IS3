package org.lab1.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lab1.bean.data.Identable;
import org.lab1.data.entity.enums.ImportStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Data
@NoArgsConstructor
public class Import implements Identable, Ownerable {
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary key, auto-generated

    @Column(nullable = false)
    private ImportStatus status;

    @Column(nullable = false)
    private String message;

    @Column(name = "file_url", length = 1024)
    private String fileUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner; // Cannot be null

    @Transient
    private Long ownerId;

    @Column(nullable = false)
    private Long createdEntitiesCount = 0L;

    @Override
    public long getId() {
            return id;
        }

}
