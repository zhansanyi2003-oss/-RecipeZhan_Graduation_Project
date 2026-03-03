<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
// 引入拖拽组件
import draggable from 'vuedraggable'
// 引入图标
import { CameraFilled, Operation, MoreFilled, Plus, Picture } from '@element-plus/icons-vue'
// 引入你的API
import { createRecipeApi } from '../../api/recipe'
import { ElMessage } from 'element-plus'

const router = useRouter()
const submitting = ref(false)
const ruleFormRef = ref()

// 📝 表单数据
const form = ref({
  title: '',
  description: '',
  coverImage: '',
  cookingTimeMin: undefined, // 使用 undefined 或 null 避免初始显示 0
  difficulty: null,
  courses: [],
  cuisines: [],
  flavours: [],

  // 🥬 初始化给 2 个空食材，引导用户填写
  ingredientsList: [
    { id: Date.now(), name: '', amount: '', note: '' },
    { id: Date.now() + 1, name: '', amount: '', note: '' },
  ],

  // 🍳 初始化给 2 个空步骤
  steps: [
    { id: Date.now(), content: '', imageUrls: [] },
    { id: Date.now() + 1, content: '', imageUrls: [] },
  ],
})

// 📏 校验规则 (针对普通字段)
const rules = ref({
  title: [
    { required: true, message: 'Please type the title', trigger: 'blur' },
    { min: 3, max: 40, message: 'Length must be 3 - 40 characters', trigger: 'blur' },
  ],
  description: [{ required: true, message: 'Please type the description', trigger: 'blur' }],
  coverImage: [{ required: true, message: 'Please upload a cover image', trigger: 'change' }],
  cookingTimeMin: [{ required: true, message: 'Please enter cooking time', trigger: 'blur' }],
  difficulty: [{ required: true, message: 'Please select difficulty', trigger: 'change' }],
  courses: [
    {
      type: 'array',
      required: true,
      message: 'Please select at least one course',
      trigger: 'change',
    },
  ],
  cuisines: [
    { type: 'array', required: true, message: 'Please select cuisines', trigger: 'change' },
  ],
  flavours: [
    { type: 'array', required: true, message: 'Please select flavours', trigger: 'change' },
  ],
})

const uploadActionUrl = 'http://localhost:8888/api/upload'

const handleCoverSuccess = (response) => {
  if (response.code === 1) {
    form.value.coverImage = response.data
    ElMessage.success('Cover image uploaded!')
    // 上传成功后，手动触发一次校验，消除红字报错
    ruleFormRef.value.validateField('coverImage')
  } else {
    ElMessage.error('Upload failed: ' + response.msg)
  }
}

const handleStepImgSuccess = (response, index) => {
  if (response.code === 1) {
    form.value.steps[index].imageUrls = [response.data]
    ElMessage.success('Step image uploaded!')
  } else {
    ElMessage.error('Upload failed')
  }
}

const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 5
  if (!isJPG) ElMessage.error('JPG/PNG format only!')
  if (!isLt2M) ElMessage.error('Image size must be less than 5MB!')
  return isJPG && isLt2M
}

// === 🥬 食材操作逻辑 ===
const addIngredient = () => {
  form.value.ingredientsList.push({ id: Date.now(), name: '', amount: '', note: '' })
}

const handleIngCommand = (command, index) => {
  if (command === 'delete') {
    form.value.ingredientsList.splice(index, 1)
  }
}

// === 🍳 步骤操作逻辑 ===
const addStep = () => {
  form.value.steps.push({ id: Date.now(), content: '', imageUrls: [] })
}

const handleStepCommand = (command, index) => {
  if (command === 'delete') {
    form.value.steps.splice(index, 1)
  }
}

// === 🚀 提交逻辑 (核心校验) ===
const submitRecipe = async () => {
  if (!ruleFormRef.value) return

  // 1️⃣ 手动校验食材列表
  const ingredients = form.value.ingredientsList
  if (ingredients.length < 2) {
    ElMessage.warning('Please add at least 2 ingredients!')
    return
  }
  for (let i = 0; i < ingredients.length; i++) {
    const item = ingredients[i]
    if (!item.name?.trim() || !item.amount?.trim()) {
      ElMessage.warning(`Ingredient #${i + 1} is incomplete. Please fill in name and amount.`)
      return
    }
  }

  // 2️⃣ 手动校验步骤列表
  const steps = form.value.steps
  if (steps.length < 2) {
    ElMessage.warning('Please add at least 2 steps!')
    return
  }
  for (let i = 0; i < steps.length; i++) {
    const step = steps[i]
    if (!step.content?.trim()) {
      ElMessage.warning(`Step #${i + 1} description cannot be empty.`)
      return
    }
  }

  // 3️⃣ Element Plus 统一校验普通字段
  await ruleFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      // ✅ 校验全部通过
      submitting.value = true
      try {
        const res = await createRecipeApi(form.value)
        if (res.code === 1) {
          ElMessage.success('Recipe published successfully! 🎉')
          router.push('/')
        } else {
          ElMessage.error(res.msg || 'Publish failed')
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('Network error, please try again.')
      } finally {
        submitting.value = false
      }
    } else {
      // ❌ 校验失败
      ElMessage.error('Please check the form for errors (marked in red).')
      return false
    }
  })
}
</script>

<template>
  <div class="create-page">
    <div class="page-header">
      <h2 class="page-title">Create New Recipe</h2>
      <div class="actions">
        <el-button type="primary" size="large" @click="submitRecipe" :loading="submitting">
          Publish
        </el-button>
      </div>
    </div>

    <el-form :model="form" :rules="rules" ref="ruleFormRef" label-position="top" class="main-form">
      <div class="section-card upload-section">
        <el-form-item prop="coverImage" style="margin-bottom: 0">
          <el-upload
            class="cover-uploader"
            :action="uploadActionUrl"
            name="file"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-image" />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><CameraFilled /></el-icon>
              <div class="upload-text">Upload Cover</div>
            </div>
          </el-upload>
        </el-form-item>

        <div class="basic-inputs">
          <el-form-item prop="title">
            <el-input
              v-model="form.title"
              placeholder="Give your recipe a title"
              class="title-input"
              size="large"
            />
          </el-form-item>

          <el-form-item prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="Share the story behind your recipe..."
              class="desc-input"
            />
          </el-form-item>
        </div>
      </div>

      <div class="section-card meta-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="Cooking time" prop="cookingTimeMin">
              <el-input-number v-model="form.cookingTimeMin" :min="1" :step="5" style="width: 100%">
                <template #suffix>mins</template>
              </el-input-number>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="Difficulty" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="Select level" style="width: 100%">
                <el-option label="Easy" value="EASY" />
                <el-option label="Medium" value="MEDIUM" />
                <el-option label="Hard" value="HARD" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="Courses" prop="courses">
              <el-select
                v-model="form.courses"
                multiple
                placeholder="Select courses"
                style="width: 100%"
              >
                <el-option label="Breakfast" value="BREAKFAST" />
                <el-option label="Lunch" value="LUNCH" />
                <el-option label="Dinner" value="DINNER" />
                <el-option label="Dessert" value="DESSERT" />
                <el-option label="Snack" value="SNACK" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="Cuisines" prop="cuisines">
              <el-select
                v-model="form.cuisines"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="e.g. Italian"
                style="margin-bottom: 5px; width: 100%"
              />
              <el-select
                v-model="form.flavours"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="Flavours (e.g. Spicy)"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="content-split">
        <el-row :gutter="40">
          <el-col :span="10">
            <div class="section-header">
              <h3>Ingredients</h3>
            </div>
            <draggable
              v-model="form.ingredientsList"
              item-key="id"
              handle=".drag-handle"
              animation="200"
            >
              <template #item="{ element, index }">
                <div class="list-item ingredient-item">
                  <el-icon class="drag-handle"><Operation /></el-icon>
                  <div class="inputs">
                    <el-input v-model="element.name" placeholder="Item name (e.g. Egg)" />
                    <el-input v-model="element.amount" placeholder="Amount (e.g. 2 pcs)" />
                    <el-input v-model="element.note" placeholder="Note" size="small" />
                  </div>
                  <el-dropdown trigger="click" @command="handleIngCommand($event, index)">
                    <el-icon class="more-btn"><MoreFilled /></el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="delete" style="color: red"
                          >Delete</el-dropdown-item
                        >
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </draggable>
            <el-button class="add-btn" @click="addIngredient" plain icon="Plus"
              >Add ingredient</el-button
            >
          </el-col>

          <el-col :span="14">
            <div class="section-header">
              <h3>Steps</h3>
            </div>
            <draggable v-model="form.steps" item-key="id" handle=".drag-handle" animation="200">
              <template #item="{ element, index }">
                <div class="list-item step-item">
                  <div class="step-left">
                    <el-icon class="drag-handle"><Operation /></el-icon>
                    <span class="step-index">{{ index + 1 }}</span>
                  </div>
                  <div class="step-content">
                    <el-input
                      v-model="element.content"
                      type="textarea"
                      :rows="3"
                      placeholder="Explain this step..."
                    />
                    <div class="step-image-uploader">
                      <el-upload
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
                        </div>
                        <div v-else class="step-img-placeholder">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </el-upload>
                    </div>
                  </div>
                  <el-dropdown trigger="click" @command="handleStepCommand($event, index)">
                    <el-icon class="more-btn"><MoreFilled /></el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="delete" style="color: red"
                          >Delete</el-dropdown-item
                        >
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </draggable>
            <el-button class="add-btn" @click="addStep" plain icon="Plus">Add step</el-button>
          </el-col>
        </el-row>
      </div>
    </el-form>
  </div>
</template>

<style scoped>
.create-page {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.main-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 封面上传区 */
.upload-section {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}
.cover-uploader {
  width: 240px;
  height: 240px;
  flex-shrink: 0;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}
.cover-uploader:hover {
  border-color: #409eff;
}
.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.upload-placeholder {
  text-align: center;
  color: #909399;
}
.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.basic-inputs {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.title-input :deep(.el-input__wrapper) {
  box-shadow: none;
  border-bottom: 1px solid #dcdfe6;
  padding-left: 0;
  border-radius: 0;
}
.title-input :deep(.el-input__inner) {
  font-size: 24px;
  font-weight: bold;
}

/* 列表通用样式 */
.content-split {
  margin-top: 20px;
}
.section-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 15px;
}
.section-header h3 {
  margin: 0;
  font-size: 18px;
}

/* 拖拽项样式 */
.list-item {
  background: #f8f9fa;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 6px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  transition: background 0.3s;
}
.list-item:hover {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.drag-handle {
  cursor: grab;
  color: #909399;
  margin-top: 10px;
  font-size: 18px;
}
.more-btn {
  cursor: pointer;
  color: #c0c4cc;
  margin-top: 10px;
  transform: rotate(90deg);
}
.more-btn:hover {
  color: #409eff;
}

/* 食材项特有 */
.ingredient-item .inputs {
  flex: 1;
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 10px;
}
.ingredient-item .inputs .el-input:last-child {
  grid-column: 1 / -1;
}

/* 步骤项特有 */
.step-item .step-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  width: 30px;
}
.step-index {
  font-weight: bold;
  font-size: 18px;
  color: #5d007d;
}
.step-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.step-image-uploader {
  width: 100px;
  height: 100px;
  background: #eee;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}
.step-img-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.step-img-placeholder {
  font-size: 12px;
  color: #999;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.add-btn {
  width: 100%;
  border-style: dashed;
  margin-top: 10px;
}
</style>
