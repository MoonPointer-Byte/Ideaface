<template>
  <div id="app-layout">
    <el-header class="app-header">
      <div class="header-content">
        <div class="logo-area" @click="navigateTo('/')">
          <img src="/logo.svg" alt="App Logo" class="logo-img" /> 
          <span class="app-title">Ideaface质面未来</span>
        </div>

        <el-menu
          :default-active="activeIndex"
          class="main-menu"
          mode="horizontal"
          :ellipsis="false"
          router
        >
          <el-menu-item index="/">首 页</el-menu-item>
          <el-menu-item index="/interview">开始面试</el-menu-item>
          <el-menu-item index="/AgentHub">智能体</el-menu-item>
          <el-menu-item index="/questions">题库中心</el-menu-item>
          <el-menu-item index="/blog">博客论坛</el-menu-item>
          <el-menu-item index="/algorithm">算法测试</el-menu-item>
          <el-menu-item index="/learning">学习中心</el-menu-item>
        </el-menu>

        <SearchInput />

        <div class="user-area">
          <el-dropdown v-if="userStore.isLoggedIn" trigger="click">
            <span class="user-avatar-link">
              <el-avatar :size="32" :src="userStore.userAvatarUrl" />
              <span class="username">{{ userStore.currentUser?.username }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="navigateTo('/profile')">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="navigateTo('/history')">
                  <el-icon><Clock /></el-icon>面试历史
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <div v-else class="auth-buttons">
            <el-button type="primary" plain @click="navigateTo('/login')">登录</el-button>
            <el-button type="primary" @click="navigateTo('/register')">注册</el-button>
          </div>
        </div>
      </div>
    </el-header>

    <el-main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { ElMessage } from 'element-plus';
import { ArrowDown, User, Clock, SwitchButton } from '@element-plus/icons-vue';
import SearchInput from '@/components/SearchInput.vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const activeIndex = computed(() => {
  if (route.path.startsWith('/blog')) return '/blog';
  if (route.path.startsWith('/questions')) return '/questions';
  if (route.path.startsWith('/algorithm')) return '/algorithm';
  return route.path;
});

const navigateTo = (path: string) => {
  router.push(path);
};

const handleLogout = () => {
  userStore.clearAuth();
  ElMessage.success('您已成功退出登录');
  router.push('/login');
};

</script>

<style scoped>
#app-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f7f8fc;
  background-image:
    radial-gradient(circle at 15% 25%, #fbe9ef 0%, transparent 35%),
    radial-gradient(circle at 85% 20%, #fdebd0 0%, transparent 30%),
    radial-gradient(circle at 50% 50%, #e9eef6 0%, transparent 30%),
    radial-gradient(circle at 20% 85%, #fbe9ef 0%, transparent 35%),
    radial-gradient(circle at 80% 80%, #dde7f5 0%, transparent 30%);
  
  /* 移除 overflow: hidden，允许子元素滚动 */
}

.app-header {
  --el-header-padding: 0 20px;
  border-bottom: 1px solid var(--el-border-color);
  background-color: #f7f8fc;
  background-image:
    radial-gradient(circle at 15% 25%, #fbe9ef 0%, transparent 35%),
    radial-gradient(circle at 85% 20%, #fdebd0 0%, transparent 30%),
    radial-gradient(circle at 50% 50%, #e9eef6 0%, transparent 30%),
    radial-gradient(circle at 20% 85%, #fbe9ef 0%, transparent 35%),
    radial-gradient(circle at 80% 80%, #dde7f5 0%, transparent 30%);
  position: sticky;
  top: 0;
  z-index: 1000;
}
.el-icon--right{
   background-color: transparent; /* 确保背景色为透明 */
}

.header-content {
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
   background-color: transparent; /* 确保背景色为透明 */
   
}

.logo-area {
  display: flex;
  align-items: center;
  cursor: pointer;
   background-color: transparent; /* 确保背景色为透明 */
    margin-right: auto;
}

.logo-img {
  height: 32px;
  width: 32px;
  margin-right: 12px;
   background-color: transparent; /* 确保背景色为透明 */
}

.app-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
   background-color: transparent; /* 确保背景色为透明 */
}

.main-menu {
  flex-grow: 1;
  justify-content: center;
  border-bottom: none;
  background-color: transparent;
  --el-menu-item-height: 59px; /* 确保与header高度一致 */
}

.el-menu--horizontal > .el-menu-item.is-active {
  border-bottom-width: 2px;
   background-color: transparent; /* 确保背景色为透明 */
}

.user-area {
  display: flex;
  align-items: center;
   /* background-color: #f7f8fc; */
  /* background-image:
    radial-gradient(circle at 15% 25%, #fbe9ef 0%, transparent 35%),
    radial-gradient(circle at 85% 20%, #fdebd0 0%, transparent 30%),
    radial-gradient(circle at 50% 50%, #e9eef6 0%, transparent 30%),
    radial-gradient(circle at 20% 85%, #fbe9ef 0%, transparent 35%),
    radial-gradient(circle at 80% 80%, #dde7f5 0%, transparent 30%); */
  background-color: transparent; /* 确保背景色为透明 */
}

.user-avatar-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  outline: none;
   /* color: var(--el-text-color-regular); */
   background-color: transparent; /* 确保背景色为透明 */
}

.username {
  margin-left: 8px;
  /* color: var(--el-text-color-regular); */
  background-color: transparent; /* 确保背景色为透明 */
}

.auth-buttons {
  display: flex;
  gap: 10px;
   background-color: transparent; /* 确保背景色为透明 */
}

.app-main {
  flex-grow: 1;
  overflow-y: auto;
   background-color: #f7f8fc;
  background-image:
    radial-gradient(circle at 15% 25%, #fbe9ef 0%, transparent 35%),
    radial-gradient(circle at 85% 20%, #fdebd0 0%, transparent 30%),
    radial-gradient(circle at 50% 50%, #e9eef6 0%, transparent 30%),
    radial-gradient(circle at 20% 85%, #fbe9ef 0%, transparent 35%),
    radial-gradient(circle at 80% 80%, #dde7f5 0%, transparent 30%);
}

/* 页面切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>