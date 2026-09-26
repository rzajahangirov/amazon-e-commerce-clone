package com.amazon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Role entity representing authorization roles in the system.
 * Corresponds to DBML Table roles.
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_roles_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
