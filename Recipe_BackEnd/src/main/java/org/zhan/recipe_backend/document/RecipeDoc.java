package org.zhan.recipe_backend.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "recipes") // 告诉 ES，这个存在名叫 recipes 的索引库里
public class RecipeDoc {

    @Id
    private Long id;

    // 用于全文检索的字段 (Text)
    @Field(type = FieldType.Text, analyzer = "english")
    private String title;

    @Field(type = FieldType.Text, analyzer = "english")
    private String description;

    @Field(type = FieldType.Keyword)
    private String coverImage;

    // 数值类型
    @Field(type = FieldType.Integer)
    private Integer cookingTimeMin;

    @Field(type = FieldType.Double)
    private Double averageRating;

    // 用于精确匹配和过滤的标签数组 (Keyword)
    @Field(type = FieldType.Keyword)
    private String difficulty;

    @Field(type = FieldType.Text, analyzer = "english")
    private List<String> flavours;

    @Field(type = FieldType.Keyword)
    private List<String> courses;

    @Field(type = FieldType.Text,  analyzer = "english")
    private List<String> cuisines;

    // 🌟 重点：我们只存食材的名字用于搜索，不需要存 amount！
    @Field(type = FieldType.Text)
    private List<String> ingredients;
}