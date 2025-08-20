<template>
  <div class="page-container">
    <el-card v-loading="isLoading">
      <template #header>
        <div class="card-header">
          <span>编辑文章</span>
        </div>
      </template>
      <el-form v-if="postForm" :model="postForm" label-position="top" ref="postFormRef" :rules="rules">
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
          <el-button type="primary" @click="submitUpdate" :loading="isSubmitting">
            确认修改
          </el-button>
           <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
      <el-empty v-else description="加载文章失败或您无权编辑" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import Vditor from 'vditor';
import 'vditor/dist/index.css';
import api from '@/services/api';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const postFormRef = ref<FormInstance>();
const isLoading = ref(true);

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

const initializeVditor = (initialContent = '') => {
  const token = userStore.getToken();
  vditorInstance = new Vditor('vditor', {
    height: 600,
    value: initialContent,
    mode: 'ir',
    placeholder: '在这里开始你的创作吧...',
    cache: { enable: false },
    upload: {
      url: 'http://localhost:8080/api/files/upload', 
      headers: { 'Authorization': `Bearer ${token}` },
      fieldName: 'file', 
      linkToImgUrl: 'data.succMap',
      format: (_files, responseText) => JSON.stringify(JSON.parse(responseText)),
      error: (msg) => ElMessage.error(`图片上传失败: ${msg}`),
    },
  });
};

const fetchCategories = async () => {
    try {
        const response = await api.get('/categories');
        categories.value = response.data;
    } catch (error) {
        console.error("获取分类列表失败:", error);
        ElMessage.error("无法加载文章分类");
    }
};

onMounted(async () => {
  const postId = route.params.id as string;
  if (!postId) {
    ElMessage.error("无效的文章ID");
    router.push('/blog');
    return;
  }

  try {
    const postResponse = await api.get(`/posts/${postId}`);
    const postData = postResponse.data;
    
    if (userStore.currentUser?.id !== postData.author.id) {
      ElMessage.error("您没有权限编辑此文章");
      router.push({ name: 'postDetail', params: { id: postId }});
      return;
    }

    postForm.title = postData.title;
    postForm.content = postData.content;
    postForm.categoryId = postData.category?.id || null;
    postForm.tagNames = postData.tags?.map((tag: any) => tag.name) || [];

    await fetchCategories();
    initializeVditor(postData.content);
    
  } catch (error) {
    ElMessage.error("加载文章内容失败");
  } finally {
    isLoading.value = false;
  }
});

onUnmounted(() => {
  if (vditorInstance) {
    vditorInstance.destroy();
  }
});

const submitUpdate = async () => {
  if (!postFormRef.value) return;

  postForm.content = vditorInstance?.getValue() || '';

  await postFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      try {
        const postId = route.params.id;
        await api.put(`/posts/${postId}`, postForm);
        ElMessage.success('文章更新成功！');
        router.push({ name: 'postDetail', params: { id: postId } });
      } catch (error) {
        console.error('更新文章失败:', error);
        ElMessage.error('更新失败，请稍后重试');
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