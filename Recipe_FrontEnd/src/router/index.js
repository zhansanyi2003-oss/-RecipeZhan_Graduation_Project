import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/layout/index.vue'
import LoingView from '../views/login/index.vue'
import HomeView from '../views/home/index.vue'
import RecipesView from '../views/recipes/index.vue'
import ProfileView from '../views/profile/index.vue'
import RecommendView from '../views/recommend/index.vue'

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
export default router
