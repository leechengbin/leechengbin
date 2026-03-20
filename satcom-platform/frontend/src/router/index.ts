import { createRouter, createWebHistory } from "vue-router";
import { ElMessage } from "element-plus";
import Dashboard from "../views/Dashboard.vue";
import Equipment from "../views/Equipment.vue";
import Monitoring from "../views/Monitoring.vue";
import StationMonitoring from "../views/StationMonitoring.vue";
import Billing from "../views/Billing.vue";
import Dispatch from "../views/Dispatch.vue";
import Users from "../views/Users.vue";
import Menus from "../views/Menus.vue";
import Roles from "../views/Roles.vue";
import Profile from "../views/Profile.vue";
import Login from "../views/Login.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", redirect: "/dashboard" },
    { path: "/login", component: Login, meta: { requiresAuth: false } },
    { path: "/dashboard", component: Dashboard, meta: { requiresAuth: true } },
    { path: "/equipment", component: Equipment, meta: { requiresAuth: true } },
    { path: "/monitoring", component: Monitoring, meta: { requiresAuth: true } },
    { path: "/station-monitoring", component: StationMonitoring, meta: { requiresAuth: true } },
    { path: "/billing", component: Billing, meta: { requiresAuth: true } },
    { path: "/dispatch", component: Dispatch, meta: { requiresAuth: true } },
    { path: "/users", component: Users, meta: { requiresAuth: true } },
    { path: "/menus", component: Menus, meta: { requiresAuth: true } },
    { path: "/roles", component: Roles, meta: { requiresAuth: true } },
    { path: "/profile", component: Profile, meta: { requiresAuth: true } }
  ]
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false);
  const token = localStorage.getItem('token');
  const expiresAt = localStorage.getItem('tokenExpiresAt');
  const isExpired = expiresAt ? Date.now() > Number(expiresAt) : false;
  const isLoggedIn = !!token && !isExpired;

  if (requiresAuth && (!token || isExpired)) {
    if (isExpired) {
      localStorage.removeItem('token');
      localStorage.removeItem('tokenExpiresAt');
      localStorage.removeItem('role');
      localStorage.removeItem('displayName');
      ElMessage.error('登录已过期，请重新登录');
    }
    next('/login');
  } else if (to.path === '/login' && isLoggedIn) {
    next('/dashboard');
  } else {
    next();
  }
});

export default router;
