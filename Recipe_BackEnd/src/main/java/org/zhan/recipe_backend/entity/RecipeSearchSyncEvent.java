package org.zhan.recipe_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.zhan.recipe_backend.common.RecipeSearchSyncOperation;
import org.zhan.recipe_backend.common.RecipeSearchSyncStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_search_sync_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchSyncEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecipeSearchSyncOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecipeSearchSyncStatus status;

    @Column(nullable = false)
    private Integer attempts;

    @Column(name = "next_retry_at", nullable = false)
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
