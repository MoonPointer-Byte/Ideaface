<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <template #header>
        <h2 class="auth-title">欢迎登录 AI模拟面试</h2>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="formRef" @submit.prevent="handleLogin" size="large"
        label-position="top">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password
            :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%;">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        还没有账号？ <router-link to="/register" class="link">立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import api from '@/services/api';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const formRef = ref<FormInstance>();
const loading = ref(false);
const loginForm = reactive({ username: '', password: '' });

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
});

const handleLogin = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
    
        const response = await api.post('/auth/login', loginForm);

     
        userStore.setAuth(response.data);

        ElMessage.success('登录成功！');
    
        const redirectPath = route.query.redirect as string || '/';
        router.push(redirectPath);
      } catch (error) {
        console.error("登录失败:", error);
       
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 65px);
}

.auth-card {
  width: 100%;
  max-width: 400px;
  border-radius: 12px;
}

.auth-title {
  text-align: center;
  margin: 0;
  font-size: 1.5rem;
  color: var(--app-text-color);
}

.auth-footer {
  text-align: center;
  margin-top: 15px;
  color: var(--app-secondary-text-color);
  font-size: 0.9rem;
}

.link {
  color: var(--el-color-primary);
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}
</style>