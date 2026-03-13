<script setup>
import RecipeCard from './recipeCard.vue'

const props = defineProps({
  recipes: {
    type: Array,
    default: () => [],
  },
  gutter: {
    type: Number,
    default: 24,
  },
  xs: {
    type: Number,
    default: 24,
  },
  sm: {
    type: Number,
    default: 12,
  },
  md: {
    type: Number,
    default: 8,
  },
  lg: {
    type: Number,
    default: 6,
  },
  xl: {
    type: Number,
    default: 4,
  },
})

const emit = defineEmits(['like-toggled'])

const forwardLikeToggled = (recipeId, newStatus) => {
  emit('like-toggled', recipeId, newStatus)
}
</script>

<template>
  <el-row :gutter="gutter" class="recipe-grid">
    <el-col
      v-for="recipe in props.recipes"
      :key="recipe.id"
      :xs="xs"
      :sm="sm"
      :md="md"
      :lg="lg"
      :xl="xl"
      class="recipe-grid-col"
    >
      <slot name="item" :recipe="recipe" :on-like-toggled="forwardLikeToggled">
        <RecipeCard :data="recipe" @like-toggled="forwardLikeToggled" />
      </slot>
    </el-col>

    <slot name="after" />
  </el-row>
</template>

<style scoped>
.recipe-grid {
  align-items: stretch;
}

.recipe-grid-col {
  display: flex;
  margin-bottom: 24px;
}
</style>
