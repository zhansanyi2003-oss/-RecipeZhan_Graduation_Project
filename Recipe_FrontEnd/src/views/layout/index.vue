<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const mobileNavOpen = ref(false)

const userInfo = ref({ name: 'Visitor', isLoggedIn: false })

const checkLoginStatus = () => {
  const loginUserStr = localStorage.getItem('loginUser')
  if (!loginUserStr) {
    userInfo.value = { name: 'Visitor', isLoggedIn: false }
    return
  }

  try {
    const loginUser = JSON.parse(loginUserStr)
    if (loginUser?.username) {
      userInfo.value = { name: loginUser.username, isLoggedIn: true }
      return
    }
  } catch (error) {
    console.error('login data error', error)
  }

  userInfo.value = { name: 'Visitor', isLoggedIn: false }
}

const handleLogout = () => {
  ElMessageBox.confirm('Are you sure to quit?', 'Warning', {
    confirmButtonText: 'Quit',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      localStorage.removeItem('loginUser')
      userInfo.value = { name: 'Visitor', isLoggedIn: false }
      mobileNavOpen.value = false
      ElMessage.success('Logged out successfully')
      router.push('/')
    })
    .catch(() => {})
}

const goToLogin = () => {
  mobileNavOpen.value = false
  router.push(`/login?redirect=${route.path}`)
}

const navigate = (path) => {
  mobileNavOpen.value = false
  router.push(path)
}

onMounted(() => {
  checkLoginStatus()
})

watch(
  () => route.path,
  () => {
    checkLoginStatus()
    mobileNavOpen.value = false
  },
)
</script>

<template>
  <div class="layout-container">
    <el-container class="full-height">
      <el-header class="modern-header">
        <div class="header-content">
          <div class="left-section" @click="navigate('/')">
            <span class="app-title">What Should I EAT?</span>
          </div>

          <div class="center-section">
            <el-menu mode="horizontal" :ellipsis="false" router class="modern-menu">
              <el-menu-item index="/">Home</el-menu-item>
              <el-menu-item index="/recipe">Explore</el-menu-item>
              <el-menu-item index="/recomm" v-if="isLoggedIn">Recommend</el-menu-item>
            </el-menu>
          </div>

          <div class="right-section desktop-actions">
            <template v-if="userInfo.isLoggedIn">
              <el-button
                color="#4ea685"
                class="create-btn"
                round
                @click="navigate('/recipe/create')"
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
                    <el-dropdown-item @click="navigate('/profile')">
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
              <el-button color="#4ea685" class="signup-btn" round @click="goToLogin"
                >Sign up</el-button
              >
            </template>
          </div>

          <div class="mobile-nav-trigger">
            <el-button class="menu-trigger" circle text @click="mobileNavOpen = true">
              <el-icon :size="22"><Menu /></el-icon>
            </el-button>
          </div>
        </div>
      </el-header>

      <el-main class="main-bg">
        <div class="content-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>

    <el-drawer
      v-model="mobileNavOpen"
      direction="rtl"
      size="280px"
      :with-header="false"
      class="mobile-drawer"
    >
      <div class="mobile-menu">
        <div class="mobile-menu-title">Menu</div>

        <el-button text class="mobile-link" @click="navigate('/')">Home</el-button>
        <el-button text class="mobile-link" @click="navigate('/recipe')">Explore</el-button>
        <el-button text class="mobile-link" @click="navigate('/recomm')">Recommend</el-button>

        <div class="mobile-divider"></div>

        <template v-if="userInfo.isLoggedIn">
          <el-button class="mobile-link" text @click="navigate('/profile')">My Profile</el-button>
          <el-button class="mobile-danger" text @click="handleLogout">Log Out</el-button>
        </template>
        <template v-else>
          <el-button class="mobile-primary" color="#4ea685" round @click="goToLogin"
            >Sign up</el-button
          >
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.full-height {
  min-height: 100vh;
}

.modern-header {
  padding: 0;
  height: 68px;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1440px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 20px;
}

.left-section {
  display: flex;
  align-items: center;
  cursor: pointer;
  min-width: fit-content;
}

.app-title {
  color: #4ea685;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: -0.5px;
}

.center-section {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 0;
}

.modern-menu {
  border-bottom: none !important;
  background-color: transparent !important;
}

:deep(.el-menu-item) {
  min-height: 48px;
  font-size: 16px;
  font-weight: 500;
  color: #606266 !important;
  background-color: transparent !important;
}

:deep(.el-menu-item:hover) {
  color: #4ea685 !important;
}

:deep(.el-menu-item.is-active) {
  color: #4ea685 !important;
  font-weight: 700;
  border-bottom: 3px solid #4ea685 !important;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: fit-content;
}

.create-btn,
.signup-btn {
  min-height: 44px;
  font-weight: 700;
  box-shadow: 0 4px 10px rgba(78, 166, 133, 0.25);
  transition: transform 0.2s ease;
}

.create-btn:hover,
.signup-btn:hover {
  transform: translateY(-2px);
}

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
  background-color: #f0f2f5;
}

.dropdown-icon {
  color: #909399;
  font-size: 12px;
}

.logout-item {
  color: #f56c6c !important;
}

.logout-item:hover {
  background-color: #fef0f0 !important;
}

.mobile-nav-trigger {
  display: none;
}

.menu-trigger {
  min-width: 44px;
  min-height: 44px;
  color: #2c3e50;
}

.main-bg {
  padding: 0;
  display: flex;
  justify-content: center;
}

.content-wrapper {
  width: 100%;
  max-width: 1280px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 24px;
  margin: 16px;
  min-height: calc(100vh - 68px - 32px);
}

.mobile-menu {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 8px;
}

.mobile-menu-title {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 6px;
}

.mobile-link {
  justify-content: flex-start;
  min-height: 44px;
  padding: 0;
  color: #2c3e50;
}

.mobile-primary {
  min-height: 44px;
  font-weight: 700;
}

.mobile-danger {
  justify-content: flex-start;
  min-height: 44px;
  padding: 0;
  color: #f56c6c;
}

.mobile-divider {
  height: 1px;
  background: #ebeef5;
  margin: 8px 0;
}

@media (max-width: 992px) {
  .center-section,
  .desktop-actions {
    display: none;
  }

  .mobile-nav-trigger {
    display: flex;
    align-items: center;
  }

  .header-content {
    padding: 0 12px;
  }

  .app-title {
    font-size: 19px;
  }

  .content-wrapper {
    margin: 0;
    border-radius: 0;
    box-shadow: none;
    padding: 14px;
    min-height: calc(100vh - 68px);
  }
}
</style>
