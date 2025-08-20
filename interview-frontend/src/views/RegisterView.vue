<template>
  <div class="auth-page-container">
    <el-card class="auth-card">
      <h2 class="auth-title">创建您的专属账户</h2>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        
        <!-- 【【【新增】】】: 头像上传区域 -->
        <el-form-item>
            <div class="avatar-upload-container">
                <el-upload
                    class="avatar-uploader"
                    action="#"
                    :show-file-list="false"
                    :http-request="() => {}" 
                    :before-upload="beforeAvatarUpload"
                    @change="handleAvatarChange"
                >
                    <img v-if="avatarPreviewUrl" :src="avatarPreviewUrl" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div class="upload-tip">
                    <p>上传头像</p>
                    <small>（可选）</small>
                </div>
            </div>
        </el-form-item>

        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="电子邮箱" :prefix-icon="Message" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="isLoading" class="auth-button" size="large">
            立即注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-switch">
        已有账号？ <el-link type="primary" @click="$router.push('/login')">直接登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import { ElMessage, type FormInstance, type FormRules, type UploadProps, type UploadRawFile, type UploadFile } from 'element-plus';
import { User, Lock, Message, Plus } from '@element-plus/icons-vue';

const router = useRouter();
const formRef = ref<FormInstance>();
const isLoading = ref(false);

const form = reactive({
  username: '',
  email: '',
  password: '',
});

// 【新增】
const avatarFile = ref<UploadRawFile | null>(null);
const avatarPreviewUrl = ref('');

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入电子邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度至少为6位', trigger: 'blur' }],
});

const handleRegister = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      isLoading.value = true;
      
      const formData = new FormData();
      // 1. 将用户信息表单转换为JSON字符串并添加到FormData
      formData.append('registerRequest', new Blob([JSON.stringify(form)], { type: 'application/json' }));
      // 2. 如果用户选择了头像文件，也添加到FormData
      if (avatarFile.value) {
        formData.append('avatarFile', avatarFile.value);
      }

      try {
        await api.post('/auth/register', formData, {
          headers: {
            // Content-Type 会由浏览器根据 FormData 自动设置，无需手动指定
          },
        });
        ElMessage.success('注册成功！正在跳转到登录页...');
        router.push('/login');
      } catch (error: any) {
        ElMessage.error(error.response?.data?.error || '注册失败');
      } finally {
        isLoading.value = false;
      }
    }
  });
};

// 【新增】头像上传前的校验
const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isJpgOrPng = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png';
  if (!isJpgOrPng) ElMessage.error('头像只能是 JPG/PNG 格式!');
  const isLt2M = rawFile.size / 1024 / 1024 < 2;
  if (!isLt2M) ElMessage.error('头像大小不能超过 2MB!');
  return isJpgOrPng && isLt2M;
};

// 【新增】处理头像选择
const handleAvatarChange = (uploadFile: UploadFile) => {
    avatarFile.value = uploadFile.raw!;
    avatarPreviewUrl.value = URL.createObjectURL(uploadFile.raw!);
};

</script>

<style scoped>
/* 这里提供一套美化的样式，您可以根据喜好调整 */
.auth-page-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px); /* 减去header高度 */
  background: linear-gradient(120deg, #fdfbfb 0%, #ebedee 100%);
}
.auth-card {
  width: 400px;
  border-radius: 12px;
}
.auth-title {
  text-align: center;
  font-size: 1.5rem;
  font-weight: 600;
  color: #303133;
  margin-bottom: 25px;
}
.auth-button {
  width: 100%;
}
.auth-switch {
  margin-top: 15px;
  text-align: center;
  font-size: 0.9rem;
  color: #606266;
}
/* 新增的头像上传样式 */
.avatar-upload-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}
.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
}
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
}
.upload-tip {
    text-align: center;
    margin-top: 8px;
    color: var(--el-text-color-secondary);
}
.upload-tip p { margin: 0; }
.upload-tip small { font-size: 0.8rem; }
</style>