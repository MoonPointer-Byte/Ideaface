<template>
  <div class="page-container">
    <div v-if="isLoading" class="flex-center loading-container">
      <el-card>
        <el-skeleton :rows="10" animated />
      </el-card>
    </div>
    
    <el-card v-else-if="post" class="report-card">
      <template #header>
        <div class="detail-header">
          <h1 class="post-title">{{ post.title }}</h1>
          <div v-if="isAuthor" class="author-actions">
            <el-button type="primary" plain :icon="Edit" @click="goToEdit">编辑</el-button>
            <el-button type="danger" plain :icon="Delete" @click="handleDelete">删除</el-button>
          </div>
        </div>
        <div class="post-meta">
          <router-link :to="{ name: 'userProfile', params: { userId: post.author.id } }" class="author-link">
            <span>作者: <strong>{{ post.author.username }}</strong></span>
          </router-link>
          <span>发布于: {{ formattedCreateTime }}</span>
          <span>阅读量: {{ post.viewCount }}</span>
        </div>
      </template>
      
      <div id="post-content" class="markdown-body" v-html="renderedContent"></div>

      <div class="post-actions">
        <el-button 
          :type="isLiked ? 'primary' : 'default'" 
          :icon="Pointer" 
          circle 
          size="large"
          @click="toggleLike"
          :loading="isLiking"
        />
        <span class="like-count">{{ post.likeCount }} 人点赞</span>
      </div>

      <el-divider content-position="left">参与讨论</el-divider>
      <BlogCommentSection :post-id="Number(postId)" />
    </el-card>
    
    <div v-else class="flex-center">
      <el-empty description="文章不存在或已被删除">
         <el-button type="primary" @click="$router.push('/blog')">返回博客列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import api from '@/services/api';
import { marked } from 'marked';
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-dark.css';
import BlogCommentSection from '@/components/BlogCommentSection.vue';
import 'github-markdown-css/github-markdown-light.css';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Pointer, Edit, Delete } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const post = ref<any>(null);
const isLoading = ref(true);
const postId = ref(route.params.id as string);
const renderedContent = ref('');
const isLiked = ref(false);
const isLiking = ref(false);

const isAuthor = computed(() => {
    if (!userStore.isLoggedIn || !post.value?.author) {
        return false;
    }
    return userStore.currentUser?.id === post.value.author.id;
});

const formattedCreateTime = computed(() => {
    const timeData = post.value?.createTime;
    if (!timeData) return 'N/A';
    if (Array.isArray(timeData)) {
        const [year, month, day, hour, minute] = timeData;
        const date = new Date(year, month - 1, day, hour, minute);
        if (isNaN(date.getTime())) return 'Invalid Date';
        return date.toLocaleString();
    } else if (typeof timeData === 'string') {
        const date = new Date(timeData.replace('T', ' '));
        if (isNaN(date.getTime())) return 'Invalid Date';
        return date.toLocaleString();
    }
    return 'Unknown Format';
});

onMounted(async () => {
  if (!postId.value) {
    isLoading.value = false;
    return;
  };
  
  try {
    const response = await api.get(`/posts/${postId.value}`);
    post.value = response.data;
    isLiked.value = response.data.isLiked || false; 
    
    if (post.value && post.value.content) {
        renderedContent.value = await marked.parse(post.value.content, {
            async: false,
            gfm: true,
            pedantic: false,
            highlight: (code: string, lang: string) => {
                const language = hljs.getLanguage(lang) ? lang : 'plaintext';
                return hljs.highlight(code, { language }).value;
            },
        } as any);
    }

  } catch (error) {
    console.error("获取文章失败:", error);
    post.value = null;
  } finally {
    isLoading.value = false;
  }
});

const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
      return ElMessage.warning("请先登录再点赞");
  }
  isLiking.value = true;
  try {
    const response = await api.post(`/posts/${post.value.id}/like`);
    post.value.likeCount = response.data.likeCount;
    isLiked.value = response.data.isLiked;
  } catch (error) {
    ElMessage.error("操作失败，请稍后重试");
  } finally {
    isLiking.value = false;
  }
};

const goToEdit = () => {
    router.push({ name: 'editPost', params: { id: post.value.id }});
};

const handleDelete = () => {
    ElMessageBox.confirm(
        '您确定要删除这篇文章吗？此操作不可恢复。',
        '警告',
        {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(async () => {
        try {
            await api.delete(`/posts/${post.value.id}`);
            ElMessage.success('文章已删除');
            router.push({ name: 'blog' });
        } catch (error) {
            console.error("删除文章失败: ", error);
            ElMessage.error('删除失败，请稍后重试');
        }
    }).catch(() => {
        // 用户点击了取消，无需操作
    });
};

</script>

<style scoped>
.page-container { padding: 24px; }
.loading-container .el-card { width: 100%; max-width: 980px; }
.report-card { max-width: 980px; margin: 0 auto; }
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}
.author-actions {
  flex-shrink: 0;
}
.markdown-body { box-sizing: border-box; width: 100%; padding: 20px; }
.post-title { font-size: 2rem; margin-bottom: 10px; color: #303133; }
.post-meta { font-size: 0.9rem; color: #909399; margin-top: 5px; }
.post-meta span { margin-right: 20px; }
.post-meta strong { color: #606266; }
.author-link { text-decoration: none; color: inherit; transition: color 0.3s; }
.author-link:hover strong, .author-link:hover span { color: var(--el-color-primary); }
.flex-center { display: flex; justify-content: center; align-items: center; height: 60vh; }
.markdown-body :deep(pre) { background-color: #282c34; padding: 1em; border-radius: 6px; overflow-x: auto; color: #abb2bf; }
.markdown-body :deep(code) { font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace; font-size: 14px; }
.post-actions { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; margin-top: 40px; padding: 20px 0; }
.like-count { font-size: 0.9rem; color: #606266; }
</style>