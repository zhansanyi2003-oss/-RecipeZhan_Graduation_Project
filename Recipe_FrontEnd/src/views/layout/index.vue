<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router' // 🌟 新增 useRoute
import { ElMessage, ElMessageBox } from 'element-plus'
// 🌟 修复 1：必须引入你用到的图标
import { UserFilled, SwitchButton, EditPen } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const userInfo = ref({ name: 'Visitor', isLoggedIn: false })

// 检查登录状态的核心逻辑
const checkLoginStatus = () => {
  const loginUserStr = localStorage.getItem('loginUser')
  if (loginUserStr) {
    try {
      const loginUser = JSON.parse(loginUserStr)
      if (loginUser && loginUser.username) {
        // 🚨 注意：之前我们在登录页存的叫 username，不是 name
        userInfo.value = { name: loginUser.username, isLoggedIn: true }
      }
    } catch (e) {
      console.error('Data Error', e)
    }
  } else {
    userInfo.value = { name: 'Visitor', isLoggedIn: false }
  }
}

// 退出登录逻辑
const handleLogout = () => {
  ElMessageBox.confirm('Are you sure to quit?', 'Warning', {
    confirmButtonText: 'Quit',
    cancelButtonText: 'Cancel', // 顺手修了个拼写小错误 Cancell -> Cancel
    type: 'warning',
  })
    .then(() => {
      // 1. 清除本地存储
      localStorage.removeItem('loginUser')
      // 2. 恢复游客状态
      userInfo.value = { name: 'Visitor', isLoggedIn: false }
      ElMessage.success('Logged out successfully')
      // 3. 跳转到首页 (根据你的路由配置决定)
      router.push('/')
    })
    .catch(() => {})
}

// 跳转登录页
const goToLogin = () => {
  // 🌟 高级技巧：带上当前页面的路径，方便登录后跳回来
  router.push(`/login?redirect=${route.path}`)
}

// 🌟 修复 2：页面刚加载时，立刻检查一次状态！
onMounted(() => {
  checkLoginStatus()
})

// 🌟 修复 3：监听路由变化！
// 只要页面路径一变（比如刚从 /login 登录成功跳回来），立刻重新检查状态！
watch(
  () => route.path,
  () => {
    checkLoginStatus()
  },
)
</script>

<template>
  <div class="layout-container">
    <el-container class="full-height">
      <el-header class="modern-header">
        <div class="header-content">
          <div class="left-section" @click="router.push('/')">
            <span class="app-title">What Should I EAT?</span>
          </div>

          <div class="center-section">
            <el-menu mode="horizontal" :ellipsis="false" router class="modern-menu">
              <el-menu-item index="/">Home</el-menu-item>
              <el-menu-item index="/recipe">Explore</el-menu-item>
            </el-menu>
          </div>

          <div class="right-section">
            <template v-if="userInfo.isLoggedIn">
              <el-button
                color="#4ea685"
                class="create-btn"
                round
                @click="router.push('/recipe/create')"
              >
                <el-icon><Plus /></el-icon> Create Recipe
              </el-button>

              <el-dropdown trigger="hover" class="user-dropdown">
                <div class="avatar-wrapper">
                  <el-avatar :size="38" style="background: #4ea685; font-weight: bold">
                    {{ userInfo.name.charAt(0).toUpperCase() }}
                  </el-avatar>
                  <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
                </div>

                <template #dropdown>
                  <el-dropdown-menu class="custom-dropdown">
                    <el-dropdown-item @click="router.push('/profile')">
                      <el-icon><User /></el-icon> My Profile
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout" class="logout-item">
                      <el-icon><SwitchButton /></el-icon> Log Out
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>

            <template v-else>
              <el-button color="#4ea685" class="signup-btn" round @click="goToLogin">
                Sign up
              </el-button>
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
.modern-header {
  padding: 0;
  height: 64px;
  /* 核心魔法：半透明白底 + 毛玻璃模糊效果 */
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06); /* 极其微弱的底边线 */
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%;
}

.header-content {
  max-width: 1400px; /* 限制最大宽度，在大屏幕上更好看 */
  margin: 0 auto;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
}

/* --- 左侧 Logo --- */
.left-section {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.app-title {
  color: #4ea685; /* Logo用你的主题绿，反差感拉满 */
  font-size: 24px;
  font-weight: 900;
  letter-spacing: -0.5px; /* 现代字体喜欢稍微紧凑一点 */
}

/* --- 中间 导航 --- */
.center-section {
  flex: 1;
  display: flex;
  justify-content: center;
}
.modern-menu {
  border-bottom: none !important;
  background-color: transparent !important;
}
/* 覆盖 Element Menu 的默认丑样式 */
:deep(.el-menu-item) {
  font-size: 16px;
  font-weight: 500;
  color: #606266 !important; /* 默认灰黑色 */
  background-color: transparent !important;
  transition: all 0.3s ease;
}
:deep(.el-menu-item:hover) {
  color: #4ea685 !important; /* 悬浮变绿 */
}
:deep(.el-menu-item.is-active) {
  color: #4ea685 !important;
  font-weight: bold;
  border-bottom: 3px solid #4ea685 !important; /* 底部绿色指示条 */
}

/* --- 右侧 用户区 --- */
.right-section {
  display: flex;
  align-items: center;
  gap: 20px; /* 元素之间的呼吸间距 */
}

/* 按钮样式 */
.create-btn,
.signup-btn {
  font-weight: bold;
  box-shadow: 0 4px 10px rgba(78, 166, 133, 0.3); /* 专属绿色发光阴影 */
  transition: transform 0.2s;
}
.create-btn:hover,
.signup-btn:hover {
  transform: translateY(-2px); /* 悬浮轻微上浮 */
}

.login-text {
  color: #606266;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s;
}
.login-text:hover {
  color: #4ea685;
}

/* 头像下拉框包裹器 */
.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px;
  border-radius: 50px;
  transition: background-color 0.2s;
}
.avatar-wrapper:hover {
  background-color: #f0f2f5; /* 鼠标放上去有灰底反馈 */
}
.dropdown-icon {
  color: #909399;
  font-size: 12px;
}

/* 下拉菜单内部样式定制 */
.logout-item {
  color: #f56c6c !important; /* 退出按钮标红更明显 */
}
.logout-item:hover {
  background-color: #fef0f0 !important;
}
/* ========================================================= */

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
