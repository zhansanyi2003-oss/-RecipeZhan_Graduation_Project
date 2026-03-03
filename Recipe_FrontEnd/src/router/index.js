import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/layout/index.vue'
import LoingView from '../views/login/index.vue'
import HomeView from '../views/home/index.vue'
import RecipesView from '../views/recipes/index.vue'
import ProfileView from '../views/profile/index.vue'
import RecommendView from '../views/recommend/index.vue'
import RecipeDetailView from '../views/recipes/recipe.vue'
import RecipeCreateView from '../views/recipes/RecipeCreate.vue'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: 'index',
        component: HomeView,
      },
      {
        path: 'recipe',
        component: RecipesView,
      },
      {
        path: 'recomm',
        component: RecommendView,
      },
      {
        path: 'profile',
        component: ProfileView,
      },

      {
        path: '/recipe/:id',
        name: 'RecipeDetail',
        component: RecipeDetailView,
        props: true,
      },
      {
        path: '/recipe/create',
        name: RecipeCreateView,
        component: RecipeCreateView,
        meta: { requiresAuth: true },
      },
    ],
  },
  {
    path: '/login',
    component: LoingView,
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  // 1. 判断你要去的页面，门上有没有贴 "requiresAuth" 的标签
  if (to.meta.requiresAuth) {
    // 2. 去包里（localStorage）翻一下有没有 Token
    const token = localStorage.getItem('loginUser') // 注意：这里换成你实际存 Token 的 key

    if (token) {
      // ✅ 有 Token，说明登录过了，放行！打开页面！
      next()
    } else {
      // ❌ 没有 Token，没登录！
      // 提示用户 (如果你引入了 Element Plus 可以弹个窗)
      ElMessage.warning('请先登录后再发布食谱哦~')

      // 强制一脚踢回登录页
      // 并且偷偷把原本想去的路径记录下来，留给下一步用
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      })
    }
  } else {
    // 3. 门上没贴标签（比如首页、登录页），直接放行
    next()
  }
})

export default router
