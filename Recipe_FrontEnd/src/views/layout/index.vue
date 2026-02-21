<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userInfo = ref({ name: 'Visitor', isLoggedIn: false })

const checkLoginStatus = () => {
  const loginUserStr = localStorage.getItem('loginUser')
  if (loginUserStr) {
    try {
      const loginUser = JSON.parse(loginUserStr)
      if (loginUser && loginUser.name) {
        userInfo.value = { name: loginUser.name, isLoggedIn: true }
      }
    } catch (e) {
      console.error('Data Error', e)
    }
  } else {
    userInfo.value = { name: 'Visitor', isLoggedIn: false }
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('Are you sure to quit?', 'Warning', {
    confirmButtonText: 'Quit',
    cancelButtonText: 'Cancell',
    type: 'warning',
  })
    .then(() => {
      localStorage.removeItem('loginUser')
      userInfo.value = { name: 'Visitor', isLoggedIn: false }
      ElMessage.success('Logged out')
      router.push('/index')
    })
    .catch(() => {})
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {})
</script>

<template>
  <div class="layout-container">
    <el-container class="full-height">
      <el-header class="my-header">
        <div class="header-content">
          <div class="left-section">
            <span class="app-title">What Should I EAT?</span>
            <el-menu
              mode="horizontal"
              :ellipsis="false"
              background-color="transparent"
              text-color="#ffffff"
              active-text-color="#ffd04b"
              router
              class="nav-menu"
            >
              <el-menu-item index="/recipe">Recipe</el-menu-item>
              <el-menu-item index="/recomm">Daily Reccomendation</el-menu-item>
              <el-menu-item index="/profile">Profile</el-menu-item>
            </el-menu>
          </div>

          <div class="right-section">
            <template v-if="userInfo.isLoggedIn">
              <span class="user-info">
                <el-icon><UserFilled /></el-icon> {{ userInfo.name }}
              </span>
              <span class="divider">|</span>
              <span class="action-btn" @click="handleLogout">
                <el-icon><SwitchButton /></el-icon> Quit
              </span>
            </template>
            <template v-else>
              <span class="user-info">Visitor</span>
              <span class="divider">|</span>
              <span class="action-btn" @click="goToLogin">
                <el-icon><EditPen /></el-icon> Sign in
              </span>
            </template>
          </div>
        </div>
      </el-header>

      <el-main class="main-bg">
        <div class="content-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.layout-container {
  min-height: 100vh; /* 改用 min-height，防止内容多了之后截断 */
  display: flex;
  flex-direction: column;
}

.full-height {
  height: 100%;
  display: flex;
}

/* --- Header 修复 --- */
.my-header {
  padding: 0;
  height: 60px;
  background-image: linear-gradient(to right, #00547d, #007fa4, #00aaa0, #00d072, #a8eb12);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  position: sticky; /* 可选：让导航栏吸顶，不随页面滚动 */
  top: 0;
  z-index: 1000;
  width: 100%; /* 确保不超宽 */
}

.header-content {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  /* 因为 App.vue 加了 box-sizing: border-box，所以这里的 padding 不会再撑大宽度了！ */
}

.left-section {
  display: flex;
  align-items: center;
  gap: 30px;
}

.app-title {
  color: white;
  font-size: 22px;
  font-weight: 900;
  white-space: nowrap; /* 防止标题换行 */
}

.nav-menu {
  border-bottom: none !important;
  min-width: 300px;
}
:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.15) !important;
}

.right-section {
  color: white;
  display: flex;
  align-items: center;
  font-size: 14px;
  white-space: nowrap; /* 防止右侧按钮换行 */
}

.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: bold;
}

.divider {
  margin: 0 15px;
  opacity: 0.5;
}

.action-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}
.action-btn:hover {
  opacity: 0.8;
  text-decoration: underline;
}

/* --- Main Content --- */
.main-bg {
  background-color: #f0f2f5;
  padding: 0;
  display: flex;
  justify-content: center;
}

.content-wrapper {
  width: 70%;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  min-height: calc(100vh - 120px);
}
</style>
