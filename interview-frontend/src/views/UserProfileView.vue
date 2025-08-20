<template>
  <div class="page-container">
    <div v-if="isLoadingProfile" class="loading-state">
      <el-card>
        <el-skeleton :rows="3" animated />
      </el-card>
    </div>
    <div v-else-if="userProfile">
      <el-card class="profile-header-card">
        <div class="profile-header">
          <el-avatar :size="80" :src="userProfile.avatar" />
          <h1 class="username">{{ userProfile.username }}</h1>
        </div>
      </el-card>

      <h2 class="section-title">TA 发表的文章</h2>
      
      <el-card v-loading="isLoadingPosts" class="post-list-card">
        <div v-if="posts.length > 0">
          <el-card v-for="post in posts" :key="post.id" class="post-item-card" shadow="hover" @click="goToPostDetail(post.id)">
              <h3 class="post-title">{{ post.title }}</h3>
              <div class="post-meta">
                <span><el-icon><Clock /></el-icon> 发布于: {{ formatTime(post.createTime) }}</span>
                <span><el-icon><View /></el-icon> 阅读: {{ post.viewCount }}</span>
                <span><el-icon><Pointer /></el-icon> 点赞: {{ post.likeCount }}</span>
              </div>
          </el-card>
        </div>
        <el-empty v-else :description="`${userProfile.username} 还没有发表任何文章`" />

        <div class="pagination-container" v-if="pagination.total > pagination.size">
          <el-pagination
              background
              layout="prev, pager, next"
              :total="pagination.total"
              :current-page="pagination.page"
              :page-size="pagination.size"
              @current-change="handlePageChange"
            />
        </div>
      </el-card>
    </div>
    <el-empty v-else description="用户不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/services/api';
import { ElMessage } from 'element-plus';
import { Clock, View, Pointer } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userProfile = ref<any>(null);
const posts = ref<any[]>([]);
const isLoadingProfile = ref(true);
const isLoadingPosts = ref(true);

const pagination = reactive({
  page: 1,
  size: 5, // 用户主页的文章列表可以少一些
  total: 0,
});

const fetchUserProfile = async (userId: string) => {
    isLoadingProfile.value = true;
    try {
        const response = await api.get(`/user/${userId}/public-profile`);
        userProfile.value = response.data;
    } catch (error) {
        console.error("获取用户信息失败:", error);
        ElMessage.error("找不到该用户");
        userProfile.value = null;
    } finally {
        isLoadingProfile.value = false;
    }
};

const fetchUserPosts = async (userId: string) => {
    isLoadingPosts.value = true;
    try {
        const params = {
            page: pagination.page - 1,
            size: pagination.size
        };
        const response = await api.get(`/user/${userId}/posts`, { params });
        posts.value = response.data.content;
        pagination.total = response.data.totalElements;
    } catch (error) {
        console.error("获取用户文章列表失败:", error);
    } finally {
        isLoadingPosts.value = false;
    }
};

onMounted(() => {
  const userId = route.params.userId as string;
  if (userId) {
    fetchUserProfile(userId);
    fetchUserPosts(userId);
  } else {
    isLoadingProfile.value = false;
    isLoadingPosts.value = false;
  }
});

watch(() => route.params.userId, (newUserId) => {
    if (newUserId) {
        const userId = newUserId as string;
        pagination.page = 1;
        fetchUserProfile(userId);
        fetchUserPosts(userId);
    }
});

const handlePageChange = (newPage: number) => {
    pagination.page = newPage;
    fetchUserPosts(route.params.userId as string);
};

const goToPostDetail = (postId: number) => {
    router.push({ name: 'postDetail', params: { id: postId } });
};

const formatTime = (timeData: any): string => {
  if (!timeData) return 'N/A';
  if (Array.isArray(timeData)) {
    const [year, month, day] = timeData;
    const date = new Date(year, month - 1, day);
    return isNaN(date.getTime()) ? 'Invalid Date' : date.toLocaleDateString();
  } else if (typeof timeData === 'string') {
    const date = new Date(timeData.replace('T', ' '));
    return isNaN(date.getTime()) ? 'Invalid Date' : date.toLocaleDateString();
  }
  return 'Unknown Format';
};
</script>

<style scoped>
.page-container {
  padding-top: 24px;
}
.loading-state .el-card {
  max-width: 980px;
  margin: 0 auto;
}
.profile-header-card {
  max-width: 980px;
  margin: 0 auto 24px auto;
  border-radius: 12px;
}
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
}
.username {
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0;
}
.section-title {
  font-size: 1.5rem;
  margin-bottom: 20px;
  max-width: 980px;
  margin: 0 auto 24px auto;
  color: #303133;
}
.post-list-card {
  max-width: 980px;
  margin: 0 auto;
  border-radius: 12px;
}
.post-item-card {
  margin-bottom: 15px;
  cursor: pointer;
}
.post-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 10px 0;
}
.post-meta {
  font-size: 0.9rem;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  gap: 20px;
}
.post-meta span {
  display: flex;
  align-items: center;
}
.post-meta .el-icon {
  margin-right: 6px;
}
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>