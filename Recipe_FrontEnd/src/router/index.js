import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '../views/layout/index.vue'
import LoingView from '../views/login/index.vue'
import HomeView from '../views/home/index.vue'
import RecipesView from '../views/recipes/index.vue'
import ProfileView from '../views/profile/index.vue'
import RecommendView from '../views/recommend/index.vue'
import RecipeDetailView from '../views/recipes/recipe.vue'
import RecipeCreateView from '../views/recipes/RecipeCreate.vue'
import AdminRecipesView from '../views/admin/recipes.vue'
import { isAdminFromLoginStorage, isStoredLoginActive } from '../utils/auth'

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
        name: 'RecipeCreate',
        component: RecipeCreateView,
        meta: { requiresAuth: true },
      },
      {
        path: '/recipe/edit/:id',
        name: 'RecipeEdit',
        component: RecipeCreateView,
        props: true,
        meta: { requiresAuth: true },
      },
      {
        path: 'admin/recipes',
        name: 'AdminRecipes',
        component: AdminRecipesView,
        meta: { requiresAuth: true, requiresAdmin: true },
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
  if (to.meta.requiresAuth) {
    if (!isStoredLoginActive()) {
      ElMessage.warning('Please login first.')
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      })
      return
    }
  }

  if (to.meta.requiresAdmin && !isAdminFromLoginStorage()) {
    ElMessage.error('Admin access only.')
    next('/')
    return
  }

  next()
})

export default router
