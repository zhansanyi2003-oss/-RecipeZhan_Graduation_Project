package org.zhan.recipe_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingStatsDto {
    private Double newAverageRating;
    private Integer newRatingCount;
}
