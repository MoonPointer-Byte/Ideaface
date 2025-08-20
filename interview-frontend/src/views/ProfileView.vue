<template>
  <div class="page-container">
    <el-card class="profile-card">
      <div class="profile-header">
        
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :http-request="handleAvatarUpload"
          :before-upload="beforeAvatarUpload"
        >
          <el-avatar :size="100" :src="userStore.userAvatarUrl" />
        </el-upload>

        <el-button type="primary" link @click="regenerateAvatar" :loading="isRegenerating">
          <el-icon><Refresh /></el-icon> 随机换一个
        </el-button>
        
        <div class="user-info">
          <h2 class="username">{{ userStore.currentUser?.username }}</h2>
          <p class="email">{{ userStore.currentUser?.email }}</p>
        </div>
      </div>

      <el-descriptions :column="1" border class="info-table">
        <el-descriptions-item label="用户ID">{{ userStore.currentUser?.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ userStore.currentUser?.username }}</el-descriptions-item>
        <el-descriptions-item label="电子邮箱">{{ userStore.currentUser?.email }}</el-descriptions-item>
        <el-descriptions-item label="账户状态">
          <el-tag type="success">正常</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div class="action-buttons">
        <el-button type="primary" :icon="Edit" @click="openEditDialog">修改资料</el-button>
        <el-button type="danger" :icon="Lock" @click="openPasswordDialog">修改密码</el-button>
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="修改个人资料" width="400px" :close-on-click-modal="false">
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpdate" :loading="isSubmitting">
          确认修改
        </el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px" :close-on-click-modal="false">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
         <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNewPassword" :loading="isSubmittingPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useUserStore } from '@/store/user';
import api from '@/services/api';
import { ElMessage, type UploadProps, type UploadRequestOptions, type FormInstance, type FormRules } from 'element-plus';
import { Edit, Lock, Refresh } from '@element-plus/icons-vue';

const userStore = useUserStore();

const editDialogVisible = ref(false);
const isSubmitting = ref(false);
const editFormRef = ref<FormInstance>();
const editForm = reactive({ username: '', email: '' });

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] },
  ],
});

const passwordDialogVisible = ref(false);
const isSubmittingPassword = ref(false);
const passwordFormRef = ref<FormInstance>();
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' });

const validatePass = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入新密码'));
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'));
  } else {
    if (passwordForm.confirmPassword !== '') {
        if (!passwordFormRef.value) return;
        passwordFormRef.value.validateField('confirmPassword', () => {});
    }
    callback();
  }
};
const validateConfirmPass = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error("两次输入的新密码不一致"));
  } else {
    callback();
  }
};
const passwordRules = reactive<FormRules>({
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ validator: validatePass, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPass, trigger: 'blur' }],
});

const isRegenerating = ref(false);

const openEditDialog = () => {
  if (userStore.currentUser) {
    editForm.username = userStore.currentUser.username;
    editForm.email = userStore.currentUser.email;
  }
  editDialogVisible.value = true;
};

const submitUpdate = async () => {
  if (!editFormRef.value) return;
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      try {
        await api.put('/user/profile', {
          username: editForm.username,
          email: editForm.email,
        });
        if (userStore.currentUser) {
            const updatedUser = { ...userStore.currentUser, ...editForm };
            const token = userStore.getToken()!;
            userStore.setAuth({ token, user: updatedUser });
        }
        ElMessage.success('个人资料更新成功！');
        editDialogVisible.value = false;
      } catch (error: any) {
        const message = error.response?.data?.error || '更新失败，请稍后重试';
        ElMessage.error(message);
      } finally {
        isSubmitting.value = false;
      }
    }
  });
};

const openPasswordDialog = () => {
    passwordFormRef.value?.resetFields();
    Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' });
    passwordDialogVisible.value = true;
};

const submitNewPassword = async () => {
    if (!passwordFormRef.value) return;
    await passwordFormRef.value.validate(async (valid) => {
        if (valid) {
            isSubmittingPassword.value = true;
            try {
                await api.post('/user/password', {
                    oldPassword: passwordForm.oldPassword,
                    newPassword: passwordForm.newPassword,
                });
                ElMessage.success('密码修改成功！');
                passwordDialogVisible.value = false;
            } catch (error: any) {
                ElMessage.error(error.response?.data?.error || '密码修改失败');
            } finally {
                isSubmittingPassword.value = false;
            }
        }
    });
};

const regenerateAvatar = async () => {
    isRegenerating.value = true;
    try {
        const response = await api.post('/user/avatar/regenerate');
        const token = userStore.getToken()!;
        userStore.setAuth({ token, user: response.data });
        ElMessage.success('新头像已生成！');
    } catch (error) {
        ElMessage.error('更换头像失败');
    } finally {
        isRegenerating.value = false;
    }
};

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('头像图片只能是 JPG, PNG, 或 GIF 格式!');
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

const handleAvatarUpload = async (options: UploadRequestOptions) => {
  const formData = new FormData();
  formData.append('avatarFile', options.file);
  try {
    const response = await api.post('/user/avatar/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    const token = userStore.getToken()!;
    userStore.setAuth({ token, user: response.data });
    ElMessage.success('头像更新成功！');
  } catch (error) {
    console.error("头像上传失败:", error);
    ElMessage.error('头像上传失败，请稍后重试');
  }
};
</script>

<style scoped>
.profile-card { max-width: 800px; margin: 0 auto; }
.profile-header { display: flex; align-items: center; flex-direction: column; gap: 10px; padding: 20px 0; border-bottom: 1px solid var(--el-border-color); margin-bottom: 20px; }
.avatar-uploader .el-upload { border-radius: 50%; cursor: pointer; position: relative; overflow: hidden; transition: var(--el-transition-duration-fast); }
.avatar-uploader .el-upload:hover { border-color: var(--el-color-primary); opacity: 0.8; }
.user-info { text-align: center; }
.username { font-size: 1.5rem; font-weight: 600; margin: 0; }
.email { color: var(--el-text-color-secondary); margin: 5px 0 0; }
.info-table { margin-top: 20px; }
.action-buttons { display: flex; justify-content: flex-end; margin-top: 30px; padding-top: 20px; border-top: 1px solid var(--el-border-color); }
</style>