<template>
  <div class="page-container">
    <div class="search-header">
      <h1>搜索结果: "{{ currentKeyword }}"</h1>
      <p v-if="!isLoading">共找到 {{ pagination.total }} 条相关文章</p>
    </div>

    <el-card v-loading="isLoading" class="post-list-card">
      <div v-if="posts.length > 0">
        <div v-for="post in posts" :key="post.id" class="post-item" @click="goToPostDetail(post.id)">
          <h2 class="post-title">{{ post.title }}</h2>
          <div class="post-meta">
            <span><el-icon><User /></el-icon> 作者: {{ post.author.username }}</span>
            <span><el-icon><Clock /></el-icon> 发布于: {{ formatTime(post.createTime) }}</span>
            <span><el-icon><View /></el-icon> 阅读: {{ post.viewCount }}</span>
            <span><el-icon><Pointer /></el-icon> 点赞: {{ post.likeCount }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else :description="`未能找到与 '${currentKeyword}' 相关的文章`" />

      <div class="pagination-container" v-if="pagination.total > pagination.size">
        <el-pagination
          background
          layout="prev, pager, next, jumper, ->, total"
          :total="pagination.total"
          :current-page="pagination.page"
          :page-size="pagination.size"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/services/api';
import { ElMessage } from 'element-plus';
import { User, Clock, View, Pointer } from '@element-plus/icons-vue';
import type { LocationQueryValue } from 'vue-router'; // 【新增】导入类型

const route = useRoute();
const router = useRouter();
const posts = ref<any[]>([]);
const isLoading = ref(true);
const currentKeyword = ref('');

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
});

const fetchSearchResults = async (keyword: string) => {
  if (!keyword) {
    posts.value = [];
    pagination.total = 0;
    isLoading.value = false;
    currentKeyword.value = '';
    return;
  }
  
  isLoading.value = true;
  currentKeyword.value = keyword;
  try {
    const params = {
      keyword: keyword,
      page: pagination.page - 1,
      size: pagination.size,
    };
    const response = await api.get('/posts/search', { params });
    posts.value = response.data.content;
    pagination.total = response.data.totalElements;
  } catch (error) {
    console.error("搜索文章失败:", error);
    ElMessage.error("搜索失败，请稍后重试");
    posts.value = [];
    pagination.total = 0;
  } finally {
    isLoading.value = false;
  }
};

// 【【【新增】】】: 创建一个辅助函数来安全地处理查询参数
const getKeywordFromQuery = (queryValue: LocationQueryValue | LocationQueryValue[]): string => {
    if (Array.isArray(queryValue)) {
        // 如果是数组，取第一个非空的元素
        return queryValue.length > 0 ? queryValue[0] || '' : '';
    }
    // 如果是字符串或null，直接返回（null会变成空字符串）
    return queryValue || '';
};


onMounted(() => {
  const keyword = getKeywordFromQuery(route.query.q);
  fetchSearchResults(keyword);
});

watch(() => route.query.q, (newQueryValue) => {
    const newKeyword = getKeywordFromQuery(newQueryValue);
    if (newKeyword && newKeyword !== currentKeyword.value) {
        pagination.page = 1;
        fetchSearchResults(newKeyword);
    }
});

const handlePageChange = (newPage: number) => {
  pagination.page = newPage;
  fetchSearchResults(currentKeyword.value);
};

const goToPostDetail = (postId: number) => {
  router.push({ name: 'postDetail', params: { id: postId } });
};

const formatTime = (timeData: any): string => {
  if (!timeData) return 'N/A';
  if (Array.isArray(timeData)) {
    const [year, month, day] = timeData;
    const date = new Date(year, month - 1, day);
    if (isNaN(date.getTime())) return 'Invalid Date';
    return date.toLocaleDateString();
  } else if (typeof timeData === 'string') {
    const date = new Date(timeData.replace('T', ' '));
    if (isNaN(date.getTime())) return 'Invalid Date';
    return date.toLocaleDateString();
  }
  return 'Unknown Format';
};
</script>

<style scoped>
/* 样式保持不变 */
.page-container { padding-top: 24px; }
.search-header { margin-bottom: 24px; padding: 0 10px; }
.search-header h1 { font-size: 1.8rem; color: #303133; }
.search-header p { color: var(--el-text-color-secondary); margin-top: 8px; }
.post-list-card { border-radius: 12px; }
.post-item { padding: 20px; border-bottom: 1px solid var(--el-border-color-light); cursor: pointer; transition: background-color 0.3s; }
.post-item:last-child { border-bottom: none; }
.post-item:hover { background-color: #f9fafc; }
.post-title { font-size: 1.3rem; font-weight: 600; margin: 0 0 10px 0; color: var(--el-text-color-primary); }
.post-meta { font-size: 0.9rem; color: var(--el-text-color-secondary); display: flex; align-items: center; gap: 20px; flex-wrap: wrap; }
.post-meta span { display: flex; align-items: center; }
.post-meta .el-icon { margin-right: 6px; }
.pagination-container { display: flex; justify-content: center; margin-top: 24px; }
</style>