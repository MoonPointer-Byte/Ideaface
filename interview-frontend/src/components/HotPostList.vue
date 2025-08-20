<template>
  <el-card class="hot-post-card" shadow="never">
    <template #header>
      <div class="card-header">
        <el-icon><HotWater /></el-icon>
        <span>热门文章</span>
        <el-radio-group v-model="sortBy" size="small" class="sort-switcher" @change="fetchHotPosts">
          <el-radio-button label="hot">按热度</el-radio-button>
          <el-radio-button label="likes">按点赞</el-radio-button>
        </el-radio-group>
      </div>
    </template>

    <el-skeleton :rows="5" animated v-if="isLoading" />

    <ul class="hot-post-list" v-else-if="hotPosts.length > 0">
      <li v-for="(post, index) in hotPosts" :key="post.id" @click="goToPost(post.id)">
        <span :class="['rank-badge', `rank-${index + 1}`]">{{ index + 1 }}</span>
        <span class="hot-post-title" :title="post.title">{{ post.title }}</span>
        <span class="hot-post-metric">
          <el-icon v-if="sortBy === 'likes'"><Pointer /></el-icon>
          <el-icon v-else><Compass /></el-icon>
          {{ sortBy === 'likes' ? post.likeCount : (post.likeCount * 5 + post.viewCount) }}
        </span>
      </li>
    </ul>
    
     <el-empty v-else description="暂无热门文章" :image-size="60" />
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
// 【【【核心修复】】】: 同时导入 useRoute
import { useRouter, useRoute } from 'vue-router'; 
import api from '@/services/api';
import { HotWater, Pointer, Compass } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const router = useRouter();
const route = useRoute(); // 【【【核心修复】】】: 定义 route 变量
const hotPosts = ref<any[]>([]);
const isLoading = ref(true);
const sortBy = ref('hot');

const fetchHotPosts = async () => {
  isLoading.value = true;
  try {
    const response = await api.get('/posts/hot', {
      params: {
        sortBy: sortBy.value
      }
    });
    hotPosts.value = response.data;
  } catch (error) {
    console.error("获取热门文章失败:", error);
    ElMessage.error("无法加载热门文章");
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
    fetchHotPosts();
});

const goToPost = (id: number) => {
  router.push({ name: 'postDetail', params: { id } }).then(() => {
    // 检查当前是否已经在目标详情页，如果是，则强制刷新
    if (route.name === 'postDetail' && String(route.params.id) === String(id)) {
        window.location.reload();
    }
  });
};
</script>

<style scoped>
/* 样式保持不变 */
.hot-post-card { border-radius: 12px; border: 1px solid var(--el-border-color-light); }
.card-header { display: flex; align-items: center; width: 100%; font-weight: 600; font-size: 1.1rem; }
.card-header span { margin-right: auto; margin-left: 8px; }
.sort-switcher { margin-left: auto; }
.hot-post-list { list-style: none; padding: 0; margin: 0; }
.hot-post-list li { display: flex; align-items: center; padding: 12px 5px; cursor: pointer; border-bottom: 1px solid #f0f2f5; transition: background-color 0.2s; }
.hot-post-list li:last-child { border-bottom: none; }
.hot-post-list li:hover .hot-post-title { color: var(--el-color-primary); }
.rank-badge { font-weight: bold; font-style: italic; width: 20px; text-align: center; margin-right: 15px; font-size: 1rem; color: #909399; }
.rank-1, .rank-2, .rank-3 { color: #f56c6c; }
.hot-post-title { flex-grow: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-size: 0.95rem; }
.hot-post-metric { flex-shrink: 0; color: #909399; font-size: 0.9em; display: flex; align-items: center; gap: 4px; }
</style>