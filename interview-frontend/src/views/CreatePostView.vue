<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发表新文章</span>
        </div>
      </template>
      <el-form :model="postForm" label-position="top" ref="postFormRef" :rules="rules">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="文章分类" prop="categoryId">
              <el-select v-model="postForm.categoryId" placeholder="请选择分类" style="width: 100%;">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="文章标签 (回车创建)">
              <el-select
                v-model="postForm.tagNames"
                multiple
                filterable
                allow-create
                default-first-option
                :reserve-keyword="false"
                placeholder="输入标签后按回车"
                style="width: 100%;"
              >
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="文章内容 (Markdown)" prop="content">
          <div id="vditor" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitPost" :loading="isSubmitting">
            立即发布
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import Vditor from 'vditor';
import 'vditor/dist/index.css';
import api from '@/services/api';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';

const router = useRouter();
const userStore = useUserStore();
const postFormRef = ref<FormInstance>();

const postForm = reactive({ 
    title: '', 
    content: '',
    categoryId: null as number | null,
    tagNames: [] as string[]
});

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择文章分类', trigger: 'change' }],
  content: [{ required: true, message: '文章内容不能为空', trigger: 'blur' }],
});

const categories = ref<any[]>([]);
const isSubmitting = ref(false);
let vditorInstance: Vditor | null = null;

onMounted(async () => {
  const token = userStore.getToken();

  if (!token) {
      ElMessage.error("用户未登录，无法初始化编辑器！");
      return;
  }

  vditorInstance = new Vditor('vditor', {
    height: 600,
    mode: 'ir',
    placeholder: '在这里开始你的创作吧...',
    cache: { enable: false },
    upload: {
      url: 'http://localhost:8080/api/files/upload', 
      headers: { 'Authorization': `Bearer ${token}` },
      fieldName: 'file', 
      linkToImgUrl: 'data.succMap',
      format(_files, responseText) {
        try {
            const response = JSON.parse(responseText);
            return JSON.stringify(response);
        } catch (e) {
            return responseText;
        }
      },
      error(msg) {
        ElMessage.error(`图片上传失败: ${msg}`);
      }
    },
  });

  try {
    const response = await api.get('/categories');
    categories.value = response.data;
  } catch (error) {
    console.error("获取分类列表失败:", error);
    ElMessage.error("无法加载文章分类");
  }
});

onUnmounted(() => {
  if (vditorInstance) {
    vditorInstance.destroy();
  }
});

const submitPost = async () => {
  if (!postFormRef.value) return;

  postForm.content = vditorInstance?.getValue() || '';

  await postFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      try {
        const response = await api.post('/posts', postForm);
        ElMessage.success('发布成功！');
        router.push({ name: 'postDetail', params: { id: response.data.id } });
      } catch (error) {
        console.error('发布文章失败:', error);
        ElMessage.error('发布失败，请稍后重试');
      } finally {
        isSubmitting.value = false;
      }
    } else {
      ElMessage.warning('请检查表单是否填写完整');
    }
  });
};
</script>

<style scoped>
#vditor {
  border-radius: 6px;
  border: 1px solid var(--el-border-color);
}
.page-container {
  padding-top: 24px;
}
</style>