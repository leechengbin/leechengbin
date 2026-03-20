<template>
  <el-container class="app-container">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar" :class="{ collapsed: isCollapse }">
      <div class="brand" :class="{ 'collapsed': isCollapse }">
        <div class="brand-title" v-if="!isCollapse">卫星通信综合管理平台</div>
        <div class="brand-icon" v-else>卫管</div>
        <el-button 
          type="text" 
          class="collapse-btn" 
          @click="isCollapse = !isCollapse"
          :icon="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
        />
      </div>
      <el-menu 
        :default-active="getActiveMenu()" 
        router 
        class="menu" 
        :collapse="isCollapse"
        collapse-transition
        :unique-opened="true"
      >
        <template v-for="menu in menuList" :key="menu.id">
          <!-- 有子菜单的情况 -->
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
            <template #title>
              <el-icon class="menu-icon"><component :is="getIconComponent(menu.icon)" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item 
              v-for="child in menu.children" 
              :key="child.id" 
              :index="child.path"
            >
              <template #icon>
                <el-icon class="menu-icon"><component :is="getIconComponent(child.icon)" /></el-icon>
              </template>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
          <!-- 没有子菜单的情况 -->
          <el-menu-item v-else :index="menu.path">
            <template #icon>
              <el-icon class="menu-icon"><component :is="getIconComponent(menu.icon)" /></el-icon>
            </template>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left"></div>
        <div class="header-right">
          <el-tag type="success" effect="plain">在线</el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user">
              {{ displayName || '系统管理员' }}
              <el-icon class="el-icon--right"><i-ep-arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useRouter } from 'vue-router';
import { 
  DataAnalysis, 
  Tools, 
  Monitor, 
  Wallet, 
  AlarmClock, 
  User,
  Setting,
  UserFilled,
  Menu,
  Fold,
  ArrowDown,
  HomeFilled,
  VideoCamera,
  Money,
  Guide,
  OfficeBuilding
} from "@element-plus/icons-vue";
import { ElMessage } from 'element-plus';
import http from "./api/http";

const route = useRoute();
const router = useRouter();
const isCollapse = ref(false);
const menuList = ref<any[]>([]);

const displayName = computed(() => {
  return localStorage.getItem('displayName');
});

const getActiveMenu = () => {
  const path = route.path;
  // 处理系统管理子菜单的激活状态
  if (path === '/users' || path === '/roles' || path === '/menus') {
    return '/system';
  }
  return path;
};

// 图标映射
const iconMap: Record<string, any> = {
  'DataAnalysis': DataAnalysis,
  'Tools': Tools,
  'Monitor': Monitor,
  'Wallet': Wallet,
  'AlarmClock': AlarmClock,
  'User': User,
  'Setting': Setting,
  'UserFilled': UserFilled,
  'Menu': Menu,
  'HomeFilled': HomeFilled,
  'VideoCamera': VideoCamera,
  'Money': Money,
  'Guide': Guide,
  'Avatar': UserFilled,
  'OfficeBuilding': OfficeBuilding
};

// 获取图标组件
const getIconComponent = (iconName: string) => {
  return iconMap[iconName] || Menu;
};

// 构建菜单树
const buildMenuTree = (menus: any[]) => {
  const menuMap = new Map<number, any>();
  
  // 首先将所有菜单添加到Map中
  menus.forEach(menu => {
    menuMap.set(menu.id, { ...menu, children: [] });
  });
  
  // 构建菜单树结构
  const rootMenus: any[] = [];
  menus.forEach(menu => {
    if (menu.parentId === 0) {
      // 根菜单
      rootMenus.push(menuMap.get(menu.id));
    } else {
      // 子菜单
      const parent = menuMap.get(menu.parentId);
      if (parent) {
        parent.children.push(menuMap.get(menu.id));
      }
    }
  });
  
  return rootMenus;
};

// 获取菜单列表
const fetchMenus = async () => {
  try {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    console.log('获取菜单，token:', token);
    console.log('获取菜单，角色:', role);
    if (!token || !role) {
      // 如果没有token或角色信息，重定向到登录页
      console.log('没有token或角色信息，重定向到登录页');
      router.push('/login');
      return;
    }
    
    // 根据角色获取菜单
    console.log('发送请求获取菜单:', `/menus/role/${role}`);
    const res: any = await http.get(`/menus/role/${role}`);
    console.log('获取菜单响应:', res);
    if (res.success) {
      // 构建菜单树
      console.log('获取菜单成功，数据:', res.data);
      menuList.value = buildMenuTree(res.data);
      console.log('构建菜单树:', menuList.value);
    } else {
      console.error('获取菜单失败，响应:', res);
      ElMessage.error('获取菜单失败: ' + res.message);
    }
  } catch (error) {
    console.error('获取菜单失败:', error);
    ElMessage.error('获取菜单失败');
  }
};

const handleCommand = (command: string) => {
  if (command === 'logout') {
    // 登出
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('displayName');
    ElMessage.success('退出登录成功');
    router.push('/login');
  } else if (command === 'profile') {
    // 个人中心
    router.push('/profile');
  }
};

// 页面加载时获取菜单
onMounted(() => {
  // 只有登录后才获取菜单
  if (localStorage.getItem('token')) {
    console.log('登录状态，获取菜单');
    fetchMenus();
  } else {
    console.log('未登录状态，不获取菜单');
  }
  // 设置初始侧边栏宽度
  document.documentElement.style.setProperty('--sidebar-width', isCollapse.value ? '64px' : '240px');
});

// 监听侧边栏折叠状态
watch(isCollapse, (collapsed) => {
  document.documentElement.style.setProperty('--sidebar-width', collapsed ? '64px' : '240px');
});

// 监听路由变化，刷新菜单（确保登录后菜单能正确加载）
watch(() => route.path, (newPath) => {
  if (newPath !== '/login' && localStorage.getItem('token')) {
    fetchMenus();
  }
});
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: #f0f2f5;
}

:root {
  --sidebar-width: 240px;
}

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  background: #0f1b2d;
  color: #fff;
  transition: width 0.3s ease;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.sidebar.collapsed {
  width: 64px !important;
}

.brand {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.3s ease;
  height: 60px;
  box-sizing: border-box;
}

.brand.collapsed {
  padding: 24px 16px;
  justify-content: center;
}

.brand-title {
  font-size: 18px;
  font-weight: 600;
  transition: opacity 0.3s ease;
}

.brand-icon {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.collapse-btn {
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
  transition: transform 0.3s ease;
}

.collapse-btn:hover {
  color: #fff;
  transform: scale(1.1);
}

.menu {
  border-right: none;
  background: transparent;
  color: #fff;
  height: calc(100vh - 80px);
  overflow-y: auto;
}

.menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.8);
  height: 56px;
  line-height: 56px;
  margin: 4px 16px;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(64, 158, 255, 0.12) !important;
  color: #fff !important;
  transform: translateX(4px);
}

.menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(64, 158, 255, 0.12)) !important;
  color: #409eff !important;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
  transform: translateX(4px);
}

.menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #409eff;
  border-radius: 0 2px 2px 0;
}

.menu :deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.8);
  height: 56px;
  line-height: 56px;
  margin: 4px 16px;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.menu :deep(.el-sub-menu__title:hover) {
  background: rgba(64, 158, 255, 0.12) !important;
  color: #fff !important;
  transform: translateX(4px);
}

.menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(64, 158, 255, 0.12)) !important;
  color: #409eff !important;
  transform: translateX(4px);
}

.menu :deep(.el-sub-menu.is-active > .el-sub-menu__title::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #409eff;
  border-radius: 0 2px 2px 0;
}

.menu :deep(.el-sub-menu__title .el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  transition: all 0.3s ease;
  margin-left: auto;
}

.menu :deep(.el-sub-menu.is-active > .el-sub-menu__title .el-sub-menu__icon-arrow) {
  transform: rotate(180deg);
  color: #409eff;
}

.menu :deep(.el-menu-item-group) {
  padding: 0;
}

.menu :deep(.el-menu-item-group__title) {
  padding: 0;
  height: auto;
  line-height: normal;
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  padding: 12px 16px 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 子菜单容器样式 */
.menu :deep(.el-sub-menu .el-menu) {
  background: rgba(15, 27, 45, 0.8) !important;
  border-radius: 0 0 8px 8px;
  margin: 0 16px 4px;
  overflow: hidden;
}

/* 子菜单项样式 */
.menu :deep(.el-sub-menu .el-menu-item) {
  margin: 2px 8px;
  border-radius: 6px;
  font-size: 13px;
  height: 48px;
  line-height: 48px;
}

.menu :deep(.el-sub-menu .el-menu-item:hover) {
  background: rgba(64, 158, 255, 0.15) !important;
  transform: translateX(2px);
}

.menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.25), rgba(64, 158, 255, 0.15)) !important;
  transform: translateX(2px);
}

/* 子菜单项图标样式 */
.menu :deep(.el-sub-menu .el-menu-item .menu-icon) {
  font-size: 16px;
  width: 20px;
  height: 20px;
  margin-right: 10px;
}

.menu :deep(.el-sub-menu .el-menu-item:hover) .menu-icon {
  background: rgba(64, 158, 255, 0.2);
  transform: scale(1.05);
}

.menu :deep(.el-sub-menu .el-menu-item.is-active) .menu-icon {
  color: #409eff;
  background: rgba(64, 158, 255, 0.25);
  transform: scale(1.05);
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
}

.menu :deep(.el-menu-item:hover) .menu-icon,
.menu :deep(.el-sub-menu__title:hover) .menu-icon {
  background: rgba(64, 158, 255, 0.2);
  transform: scale(1.1);
}

.menu :deep(.el-menu-item.is-active) .menu-icon,
.menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) .menu-icon {
  color: #409eff;
  background: rgba(64, 158, 255, 0.25);
  transform: scale(1.1);
}

.menu :deep(.el-sub-menu__title .menu-icon) {
  margin-right: 12px;
}

.menu :deep(.el-menu--collapse .el-sub-menu__title .menu-icon) {
  margin-right: 0;
}

.menu :deep(.el-menu--collapse .el-menu-item .menu-icon) {
  margin-right: 0;
}

.menu :deep(.el-menu--collapse .el-menu-item.is-active::before) {
  display: none;
}

.menu :deep(.el-menu--collapse .el-sub-menu.is-active > .el-sub-menu__title::before) {
  display: none;
}

.header {
  position: fixed;
  top: 0;
  left: var(--sidebar-width, 240px);
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(90deg, #ffffff 0%, #fafbfc 100%);
  border-bottom: 1px solid #e8e8ed;
  padding: 0 28px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  z-index: 99;
  transition: left 0.3s ease;
}

.sidebar.collapsed ~ .el-container .header {
  left: 64px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user {
  color: #fff;
  font-weight: 500;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.user:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #79bbff 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transform: translateY(-1px);
}

.content {
  margin-left: var(--sidebar-width, 240px);
  margin-top: 60px;
  padding: 20px 24px;
  transition: margin-left 0.3s ease;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
  overflow-y: auto;
}

/* 滚动条样式 */
.menu::-webkit-scrollbar {
  width: 6px;
}

.menu::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>

<style>
/* 全局弹窗样式优化 */
.el-dialog {
  border-radius: 16px !important;
  overflow: hidden;
}

.el-dialog__header {
  padding: 20px 24px 16px !important;
  margin-right: 0 !important;
  border-bottom: 1px solid #f0f0f5;
  background: #fafbfc;
}

.el-dialog__title {
  font-size: 16px !important;
  font-weight: 600 !important;
  color: #1a1a2e !important;
}

.el-dialog__headerbtn {
  top: 20px !important;
  right: 20px !important;
}

.el-dialog__headerbtn .el-dialog__close {
  font-size: 18px !important;
  color: #8c8c9a !important;
  transition: color 0.2s;
}

.el-dialog__headerbtn:hover .el-dialog__close {
  color: #409eff !important;
}

.el-dialog__body {
  padding: 24px !important;
  color: #26263a !important;
}

.el-dialog__footer {
  padding: 16px 24px 20px !important;
  border-top: 1px solid #f0f0f5;
  background: #fafbfc;
}

.el-form-item__label {
  font-weight: 500 !important;
  color: #26263a !important;
}

.el-dialog .el-button {
  border-radius: 8px !important;
  font-weight: 500 !important;
}

.el-dialog .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3) !important;
}

.el-dialog .el-button--primary:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #79bbff 100%) !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4) !important;
}

.el-dialog .el-button--default {
  background: #fff !important;
  border: 1px solid #dcdfe6 !important;
  color: #606266 !important;
}

.el-dialog .el-button--default:hover {
  border-color: #409eff !important;
  color: #409eff !important;
  background: #ecf5ff !important;
}

/* 遮罩层优化 */
.el-overlay {
  background-color: rgba(0, 0, 0, 0.4) !important;
  backdrop-filter: blur(2px);
}

.el-dialog__wrapper {
  transition: all 0.3s ease;
}
</style>
