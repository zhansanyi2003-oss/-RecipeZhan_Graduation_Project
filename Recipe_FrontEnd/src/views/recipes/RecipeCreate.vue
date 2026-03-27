<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import draggable from 'vuedraggable'
import { CameraFilled, Operation, Plus, Picture, Delete } from '@element-plus/icons-vue'
import { createRecipeApi, getRecipeApi, updateRecipeApi } from '../../api/recipe'
import { getAllFlavoursApi, getAllCuisinesApi } from '../../api/recipeCard'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const submitting = ref(false)
const ruleFormRef = ref()
let localIdSeed = Date.now()

const nextLocalId = () => {
  localIdSeed += 1
  return localIdSeed
}

const createDefaultIngredients = () => [
  { id: nextLocalId(), name: '', amount: '', note: '' },
  { id: nextLocalId(), name: '', amount: '', note: '' },
]

const createDefaultSteps = () => [
  { id: nextLocalId(), content: '', imageUrls: [] },
  { id: nextLocalId(), content: '', imageUrls: [] },
]

const form = ref({
  title: '',
  description: '',
  coverImage: '',
  cookingTimeMin: undefined,
  difficulty: null,
  courses: [],
  cuisines: [],
  flavours: [],
  dietTypes: [],
  ingredients: createDefaultIngredients(),
  steps: createDefaultSteps(),
})

const recipeId = computed(() => {
  const id = Number(route.params.id)
  return Number.isFinite(id) ? id : null
})
const isEditMode = computed(() => recipeId.value !== null && recipeId.value > 0)
const pageTitle = computed(() => (isEditMode.value ? 'Edit Recipe' : 'Create a Masterpiece'))
const submitButtonText = computed(() => (isEditMode.value ? 'Update Recipe' : 'Publish Recipe'))

const rules = ref({
  title: [
    { required: true, message: 'Please enter a recipe title', trigger: 'blur' },
    { min: 3, max: 60, message: 'Length must be 3 - 60 characters', trigger: 'blur' },
  ],
  description: [{ required: true, message: 'Please add a short description', trigger: 'blur' }],
  coverImage: [{ required: true, message: 'Please upload a cover image', trigger: 'change' }],
  cookingTimeMin: [{ required: true, message: 'Required', trigger: 'blur' }],
  difficulty: [{ required: true, message: 'Required', trigger: 'change' }],
  courses: [{ type: 'array', required: true, message: 'Required', trigger: 'change' }],
})

const uploadActionUrl = 'http://localhost:8888/api/upload'

const handleCoverSuccess = (response) => {
  if (response.code === 1) {
    form.value.coverImage = response.data
    ElMessage.success('Cover image uploaded!')
    ruleFormRef.value?.validateField('coverImage')
  } else {
    ElMessage.error('Upload failed: ' + response.msg)
  }
}

const handleStepImgSuccess = (response, index) => {
  if (response.code === 1) {
    form.value.steps[index].imageUrls = [response.data]
  } else {
    ElMessage.error('Upload failed')
  }
}

const beforeUpload = (file) => {
  const isImage =
    file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp'
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('JPG/PNG/WEBP format only!')
  if (!isLt5M) ElMessage.error('Image size must be less than 5MB!')
  return isImage && isLt5M
}

const addIngredient = () => {
  form.value.ingredients.push({ id: nextLocalId(), name: '', amount: '', note: '' })
}
const removeIngredient = (index) => {
  form.value.ingredients.splice(index, 1)
}

const addStep = () => {
  form.value.steps.push({ id: nextLocalId(), content: '', imageUrls: [] })
}
const removeStep = (index) => {
  form.value.steps.splice(index, 1)
}

const buildPayload = () => ({
  title: form.value.title,
  description: form.value.description,
  coverImage: form.value.coverImage,
  cookingTimeMin: form.value.cookingTimeMin,
  difficulty: form.value.difficulty,
  courses: form.value.courses,
  cuisines: form.value.cuisines,
  flavours: form.value.flavours,
  dietTypes: form.value.dietTypes,
  ingredients: form.value.ingredients
    .map((item) => ({
      name: item.name?.trim() || '',
      amount: item.amount || '',
      note: item.note || '',
    }))
    .filter((item) => item.name),
  steps: form.value.steps
    .map((item) => ({
      content: item.content?.trim() || '',
      imageUrls: Array.isArray(item.imageUrls) ? item.imageUrls : [],
    }))
    .filter((item) => item.content),
})

const fillFormForEdit = (data) => {
  form.value = {
    ...form.value,
    title: data?.title || '',
    description: data?.description || '',
    coverImage: data?.coverImage || '',
    cookingTimeMin: data?.cookingTimeMin,
    difficulty: data?.difficulty || null,
    courses: data?.courses || [],
    cuisines: data?.cuisines || [],
    flavours: data?.flavours || [],
    dietTypes: data?.dietTypes || data?.diets || [],
    ingredients: (data?.ingredients || []).length
      ? data.ingredients.map((item) => ({
          id: nextLocalId(),
          name: item?.name || '',
          amount: item?.amount || '',
          note: item?.note || '',
        }))
      : createDefaultIngredients(),
    steps: (data?.steps || []).length
      ? data.steps.map((item) => ({
          id: nextLocalId(),
          content: item?.content || '',
          imageUrls: item?.imageUrls || [],
        }))
      : createDefaultSteps(),
  }
}

const loadRecipeForEdit = async () => {
  if (!isEditMode.value) return

  try {
    const res = await getRecipeApi(recipeId.value)
    if (res.code === 1) {
      fillFormForEdit(res.data)
    } else {
      ElMessage.error(res.msg || 'Failed to load recipe data.')
      router.replace('/profile')
    }
  } catch (error) {
    ElMessage.error('Failed to load recipe data.')
    router.replace('/profile')
  }
}

const submitRecipe = async () => {
  if (!ruleFormRef.value) return

  const hasIngredient = form.value.ingredients.some((item) => item.name && item.name.trim())
  if (!hasIngredient) {
    ElMessage.warning('Please add at least one ingredient.')
    return
  }

  const hasStep = form.value.steps.some((item) => item.content && item.content.trim())
  if (!hasStep) {
    ElMessage.warning('Please add at least one step.')
    return
  }

  await ruleFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('Please check the form for missing fields.')
      window.scrollTo({ top: 0, behavior: 'smooth' })
      return
    }

    submitting.value = true
    try {
      const payload = buildPayload()
      const res = isEditMode.value
        ? await updateRecipeApi(recipeId.value, payload)
        : await createRecipeApi(payload)

      if (res.code === 1) {
        ElMessage.success(isEditMode.value ? 'Recipe updated successfully!' : 'Recipe published successfully!')
        if (isEditMode.value) {
          router.push(`/recipe/${recipeId.value}`)
        } else {
          router.push('/')
        }
      } else {
        ElMessage.error(res.msg || (isEditMode.value ? 'Update failed' : 'Publish failed'))
      }
    } catch (error) {
      ElMessage.error('Network error, please try again.')
    } finally {
      submitting.value = false
    }
  })
}

const flavours = ref([])
const cuisines = ref([])

const getFlavours = async () => {
  const result = await getAllFlavoursApi()
  if (result.code) {
    flavours.value = result.data
  }
}
const getCuisines = async () => {
  const result = await getAllCuisinesApi()
  if (result.code) {
    cuisines.value = result.data
  }
}

onMounted(async () => {
  await Promise.all([getFlavours(), getCuisines()])
  await loadRecipeForEdit()
})
</script>

<template>
  <div class="create-page-wrapper">
    <div class="sticky-header">
      <div class="header-content">
        <h2 class="page-title">{{ pageTitle }}</h2>
      </div>
    </div>

    <div class="create-container">
      <el-form :model="form" :rules="rules" ref="ruleFormRef" label-position="top">
        <div class="meta-card top-overview-card">
          <el-row :gutter="40">
            <el-col :xs="24" :md="10">
              <el-form-item prop="coverImage" class="m-0">
                <el-upload
                  class="classic-uploader"
                  :action="uploadActionUrl"
                  name="file"
                  :show-file-list="false"
                  :on-success="handleCoverSuccess"
                  :before-upload="beforeUpload"
                >
                  <div v-if="form.coverImage" class="cover-image-preview">
                    <img :src="form.coverImage" />
                    <div class="cover-hover-mask">
                      <el-icon><CameraFilled /></el-icon> Change Cover
                    </div>
                  </div>
                  <div v-else class="upload-placeholder">
                    <div class="icon-circle">
                      <el-icon><Picture /></el-icon>
                    </div>
                    <span class="upload-title">Upload Cover Photo</span>
                    <span class="upload-hint">Format: JPG/PNG, Max: 5MB</span>
                  </div>
                </el-upload>
              </el-form-item>
            </el-col>

            <el-col :xs="24" :md="14" class="right-info-col">
              <el-form-item label="Recipe Title" prop="title">
                <el-input
                  v-model="form.title"
                  placeholder="e.g. Grandma's Apple Pie"
                  class="modern-input title-input"
                  size="large"
                />
              </el-form-item>

              <el-form-item label="Description" prop="description" class="m-0">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="6"
                  placeholder="Share the story behind your recipe. What makes it special? Any tips for the perfect result?"
                  class="modern-input desc-input"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="meta-card">
          <h3 class="section-title">Recipe Details</h3>
          <el-row :gutter="30">
            <el-col :xs="12" :sm="8">
              <el-form-item label="Cooking Time" prop="cookingTimeMin">
                <el-input-number
                  v-model="form.cookingTimeMin"
                  :min="1"
                  :step="5"
                  class="w-full"
                  size="large"
                >
                  <template #suffix>mins</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :xs="12" :sm="8">
              <el-form-item label="Difficulty" prop="difficulty">
                <el-select
                  v-model="form.difficulty"
                  placeholder="Select"
                  class="w-full"
                  size="large"
                >
                  <el-option label="Easy" value="EASY" />
                  <el-option label="Medium" value="MEDIUM" />
                  <el-option label="Hard" value="HARD" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="12" :sm="8">
              <el-form-item label="Course" prop="courses">
                <el-select
                  v-model="form.courses"
                  multiple
                  placeholder="e.g. Dinner"
                  class="w-full"
                  size="large"
                >
                  <el-option label="Breakfast" value="BREAKFAST" />
                  <el-option label="Lunch" value="LUNCH" />
                  <el-option label="Dinner" value="DINNER" />
                  <el-option label="Dessert" value="DESSERT" />
                  <el-option label="Snack" value="SNACK" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="30" class="mt-4">
            <el-col :xs="24" :sm="8">
              <el-form-item label="Cuisine (Optional)" prop="cuisines">
                <el-select
                  v-model="form.cuisines"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="e.g. Italian"
                  class="w-full"
                  size="large"
                >
                  <el-option v-for="item in cuisines" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item label="Flavours (Optional)" prop="flavours">
                <el-select
                  v-model="form.flavours"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="e.g. Spicy"
                  class="w-full"
                  size="large"
                >
                  <el-option v-for="item in flavours" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item label="Dietary (Optional)" prop="dietTypes">
                <el-select
                  v-model="form.dietTypes"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="e.g. Vegan"
                  class="w-full"
                  size="large"
                >
                  <el-option label="Vegetarian" value="Vegetarian" />
                  <el-option label="Vegan" value="Vegan" />
                  <el-option label="Gluten-Free" value="Gluten-Free" />
                  <el-option label="Dairy-Free" value="Dairy-Free" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="content-split">
          <el-row :gutter="40">
            <el-col :xs="24" :lg="8">
              <div class="section-header">
                <h3>Ingredients</h3>
              </div>
              <draggable
                v-model="form.ingredients"
                item-key="id"
                handle=".drag-handle"
                animation="200"
                class="drag-list"
              >
                <template #item="{ element, index }">
                  <div class="modern-list-item">
                    <el-icon class="drag-handle"><Operation /></el-icon>
                    <div class="item-inputs ingredient-inputs">
                      <el-input
                        v-model="element.name"
                        placeholder="Name (e.g. Flour)"
                        class="ghost-input fw-bold"
                      />
                      <el-input
                        v-model="element.amount"
                        placeholder="Amount (e.g. 200g)"
                        class="ghost-input"
                      />
                      <el-input
                        v-model="element.note"
                        placeholder="Prep note (e.g. sifted)"
                        class="ghost-input text-small"
                      />
                    </div>
                    <el-button
                      type="danger"
                      text
                      circle
                      :icon="Delete"
                      @click="removeIngredient(index)"
                      class="delete-btn"
                    />
                  </div>
                </template>
              </draggable>
              <el-button
                class="add-btn btn-ui btn-ui--soft btn-ui--wide"
                @click="addIngredient"
              >
                <el-icon class="mr-1"><Plus /></el-icon> Add Ingredient
              </el-button>
            </el-col>

            <el-col :xs="24" :lg="16">
              <div class="section-header">
                <h3>Instructions</h3>
              </div>
              <draggable
                v-model="form.steps"
                item-key="id"
                handle=".drag-handle"
                animation="200"
                class="drag-list"
              >
                <template #item="{ element, index }">
                  <div class="modern-list-item step-item">
                    <el-icon class="drag-handle"><Operation /></el-icon>
                    <div class="step-number">{{ index + 1 }}</div>

                    <div class="item-inputs step-content">
                      <el-input
                        v-model="element.content"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 5 }"
                        placeholder="Explain this step in detail..."
                        class="ghost-textarea"
                      />

                      <div class="step-image-area">
                        <el-upload
                          class="step-uploader"
                          :action="uploadActionUrl"
                          name="file"
                          :show-file-list="false"
                          :on-success="(res) => handleStepImgSuccess(res, index)"
                        >
                          <div
                            v-if="element.imageUrls && element.imageUrls.length > 0"
                            class="step-img-preview"
                          >
                            <img :src="element.imageUrls[0]" />
                            <div class="img-hover-mask">
                              <el-icon><CameraFilled /></el-icon>
                            </div>
                          </div>
                          <div v-else class="step-img-placeholder">
                            <el-icon><Picture /></el-icon>
                            <span>Add Photo</span>
                          </div>
                        </el-upload>
                      </div>
                    </div>

                    <el-button
                      type="danger"
                      text
                      circle
                      :icon="Delete"
                      @click="removeStep(index)"
                      class="delete-btn step-del-btn"
                    />
                  </div>
                </template>
              </draggable>
              <el-button
                class="add-btn btn-ui btn-ui--soft btn-ui--wide"
                @click="addStep"
              >
                <el-icon class="mr-1"><Plus /></el-icon> Add Next Step
              </el-button>
            </el-col>
          </el-row>
        </div>
      </el-form>

      <div class="form-end-actions">
        <el-button class="bottom-cancel-btn btn-ui btn-ui--outline" @click="router.back()">
          Cancel
        </el-button>
        <el-button
          size="large"
          @click="submitRecipe"
          :loading="submitting"
          class="publish-btn bottom-publish-btn btn-ui btn-ui--brand"
        >
          {{ submitButtonText }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.create-page-wrapper {
  background-color: #f9fafb;
  min-height: 100vh;
  padding-bottom: 40px;
}

/* ================= ć‚¬ćµ®ĺ¸éˇ¶ Header ================= */
.sticky-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #eef0f4;
  padding: 15px 0;
}
.header-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-title {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
  font-weight: 800;
}
.publish-btn {
  padding-inline: 28px;
}

.form-end-actions {
  margin-top: 8px;
  padding: 12px 0 4px 0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.bottom-cancel-btn,
.bottom-publish-btn {
  min-height: 44px;
}

.create-container {
  max-width: 1000px;
  margin: 30px auto;
  padding: 0 20px;
}

/* ================= đźŚź é‡Ťç‚ąäĽĺŚ–ďĽšé€šç”¨ç™˝č‰˛ĺŤˇç‰‡ ================= */
.meta-card {
  background: white;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  margin-bottom: 40px;
  border: 1px solid #f0f2f5;
}

/* ================= đźŚź é‡Ťç‚ąäĽĺŚ–ďĽšç»Źĺ…¸ĺ°éť˘ä¸ŠäĽ ĺŚş ================= */
.classic-uploader {
  width: 100%;
  height: 280px; /* ĺ›şĺ®šé«ĺş¦ďĽŚĺ®ŚçľŽĺĄ‘ĺĺŹłäľ§čˇ¨ĺŤ•é«ĺş¦ */
  border-radius: 12px;
  border: 2px dashed #d5ebe1;
  background: #fafdfb;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}
.classic-uploader:hover {
  border-color: #4ea685;
  background: #eef7f4;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #4ea685;
}
.icon-circle {
  width: 56px;
  height: 56px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(78, 166, 133, 0.15);
  margin-bottom: 16px;
}
.upload-title {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 4px;
}
.upload-hint {
  font-size: 13px;
  color: #909399;
}
.cover-image-preview {
  position: relative;
  width: 100%;
  height: 100%;
}
.cover-image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-hover-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  opacity: 0;
  transition: opacity 0.3s;
  gap: 8px;
}
.classic-uploader:hover .cover-hover-mask {
  opacity: 1;
}

/* ================= đźŚź é‡Ťç‚ąäĽĺŚ–ďĽšĺ¸¦čľąćˇ†çš„é«çş§čľ“ĺ…Ąćˇ† ================= */
:deep(.modern-input .el-input__wrapper),
:deep(.modern-input .el-textarea__inner) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #dcdfe6 inset; /* ćŽçˇ®çš„é»č®¤čľąćˇ† */
  background-color: #fafdfb;
  transition: all 0.2s ease;
}
:deep(.modern-input .el-input__wrapper:hover),
:deep(.modern-input .el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #4ea685 inset;
}
:deep(.modern-input .el-input__wrapper.is-focus),
:deep(.modern-input .el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px #4ea685 inset !important; /* čŽ·ĺľ—ç„¦ç‚ąć—¶ĺŹç˛—č–„čŤ·ç»ż */
  background-color: white;
}
/* ć ‡é˘čľ“ĺ…Ąćˇ†ĺŠ ç˛—ć”ľĺ¤§ */
:deep(.title-input .el-input__inner) {
  font-size: 20px;
  font-weight: bold;
  height: 40px;
  color: #2c3e50;
}
:deep(.desc-input .el-textarea__inner) {
  font-size: 15px;
  color: #606266;
  resize: none;
}
/* ç»™ Form Label ĺ˘žĺŠ é«çş§ć„ź */
:deep(.el-form-item__label) {
  font-weight: bold;
  color: #2c3e50;
  padding-bottom: 8px;
}

/* ================= ĺş•é¨ĺŤˇç‰‡é€šç”¨ ================= */
.section-title {
  font-size: 20px;
  color: #2c3e50;
  margin: 0 0 20px 0;
}
.w-full {
  width: 100%;
}
.mt-4 {
  margin-top: 20px;
}
.m-0 {
  margin: 0;
}

/* ================= ć‹–ć‹˝ĺ—čˇ¨ ================= */
.content-split {
  margin-top: 20px;
}
.section-header h3 {
  font-size: 22px;
  color: #2c3e50;
  margin-bottom: 20px;
}

.modern-list-item {
  background: white;
  border: 1px solid #ebeef5;
  padding: 15px;
  margin-bottom: 12px;
  border-radius: 12px;
  display: flex;
  align-items: flex-start;
  gap: 15px;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}
.modern-list-item:hover {
  border-color: #d5ebe1;
  box-shadow: 0 6px 16px rgba(78, 166, 133, 0.08);
}

.drag-handle {
  cursor: grab;
  color: #c0c4cc;
  margin-top: 10px;
  font-size: 20px;
}

.item-inputs {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
:deep(.ghost-input .el-input__wrapper),
:deep(.ghost-textarea .el-textarea__inner) {
  box-shadow: none !important;
  background: transparent;
  padding: 4px 8px;
  transition: background 0.2s;
}
:deep(.ghost-input .el-input__wrapper:hover),
:deep(.ghost-textarea .el-textarea__inner:hover) {
  background: #f5f7fa;
}
:deep(.ghost-input .el-input__wrapper.is-focus),
:deep(.ghost-textarea .el-textarea__inner:focus) {
  background: white;
  box-shadow: 0 0 0 1px #4ea685 inset !important;
}
.fw-bold :deep(.el-input__inner) {
  font-weight: bold;
  color: #2c3e50;
}
.text-small :deep(.el-input__inner) {
  font-size: 13px;
  color: #909399;
}

.delete-btn {
  margin-top: 6px;
  opacity: 0;
  transition: opacity 0.2s;
}
.modern-list-item:hover .delete-btn {
  opacity: 1;
}

.step-number {
  width: 28px;
  height: 28px;
  background: #4ea685;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  margin-top: 5px;
  flex-shrink: 0;
}
.step-del-btn {
  margin-top: 0;
}

.step-image-area {
  margin-top: 10px;
  margin-left: 8px;
}
.step-uploader {
  width: 120px;
  height: 120px;
}
.step-img-placeholder {
  width: 120px;
  height: 120px;
  background: #f5f7fa;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  transition: all 0.3s;
}
.step-img-placeholder:hover {
  border-color: #4ea685;
  color: #4ea685;
  background: #eef7f4;
}
.step-img-placeholder .el-icon {
  font-size: 24px;
  margin-bottom: 5px;
}
.step-img-preview {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}
.step-img-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.img-hover-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  opacity: 0;
  transition: opacity 0.2s;
}
.step-img-preview:hover .img-hover-mask {
  opacity: 1;
}

.add-btn {
  width: 100%;
  height: 48px;
  margin-top: 10px;
}
.mr-1 {
  margin-right: 5px;
}

@media (max-width: 992px) {
  .right-info-col {
    margin-top: 30px;
  }
  .classic-uploader {
    height: 240px;
  }
}

@media (max-width: 768px) {
  .sticky-header {
    padding: 10px 0;
  }

  .page-title {
    font-size: 18px;
  }

  .create-container {
    margin: 16px auto;
    padding: 0 10px;
  }

  .meta-card {
    padding: 18px 14px;
    margin-bottom: 20px;
  }

  :deep(.title-input .el-input__inner) {
    font-size: 18px;
  }

  .modern-list-item {
    padding: 12px 10px;
    gap: 10px;
  }

  .delete-btn {
    opacity: 1;
  }

  .step-image-area {
    margin-left: 0;
  }

  .step-uploader,
  .step-img-placeholder,
  .step-img-preview {
    width: 96px;
    height: 96px;
  }

  .form-end-actions {
    padding: 6px 0;
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 480px) {
  .form-end-actions {
    grid-template-columns: 1fr;
  }

  .add-btn {
    min-height: 44px;
  }

  .modern-list-item {
    flex-wrap: wrap;
  }
}
</style>
