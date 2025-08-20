<template>
  <div class="blog-list-view">
    <el-row :gutter="30" class="full-height-layout">
      
      <!-- 左侧边栏 -->
      <el-col :xs="24" :sm="24" :md="5" class="sidebar-col">
        <div class="sidebar-content-wrapper scrollable-column">
          <el-card class="sidebar-card">
            <template #header>
              <div class="card-header">
                <el-icon><Collection /></el-icon>
                <span>文章分类</span>
              </div>
            </template>
            <ul class="category-list" v-loading="isCategoryLoading">
              <li @click="filterByCategory(null)" :class="{ active: !filterParams.categoryId && !filterParams.tagName }">
                <span class="category-name">全部文章</span>
              </li>
              <li v-for="cat in categories" :key="cat.id" @click="filterByCategory(cat.id)" :class="{ active: filterParams.categoryId === cat.id }">
                <span class="category-name">{{ cat.name }}</span>
              </li>
            </ul>
          </el-card>

          <el-card class="sidebar-card tag-card">
            <template #header>
              <div class="card-header">
                <el-icon><PriceTag /></el-icon>
                <span>热门标签</span>
              </div>
            </template>
            <div v-loading="isTagLoading" class="tag-list">
              <el-tag 
                v-for="tag in tags" 
                :key="tag.id" 
                class="tag-item" 
                @click="filterByTag(tag.name)"
                :class="{ active: filterParams.tagName === tag.name }"
                effect="light" 
                round
              >
                {{ tag.name }}
              </el-tag>
            </div>
            <el-empty v-if="!isTagLoading && tags.length === 0" description="暂无标签" :image-size="60" />
          </el-card>
        </div>
      </el-col>

      <!-- 中间主内容区 -->
      <el-col :xs="24" :sm="24" :md="13" class="main-content-col">
        <header class="blog-header">
          <h1 class="page-title">{{ pageTitle }}</h1>
          <el-button type="primary" :icon="Edit" @click="goToCreatePost" round>发表新文章</el-button>
        </header>

        <div class="post-list-wrapper scrollable-column">
          <transition-group 
            v-if="!isLoading && posts.length > 0" 
            name="post-list-fade"
            tag="div"
            class="post-list"
          >
            <div 
              v-for="(post, index) in posts" 
              :key="post.id" 
              class="post-item"
              :style="{ '--i': index }"
            >
              <h2 class="post-title" @click="goToPostDetail(post.id)">{{ post.title }}</h2>
              <p class="post-excerpt">在这里可以显示一段文章摘要，帮助用户快速了解内容。如果API不返回摘要，我们可以截取正文的前100个字符...</p>
              <div class="post-meta">
                <router-link :to="{ name: 'userProfile', params: { userId: post.author.id } }" class="author-link">
                    <span><el-icon><User /></el-icon>  {{ post.author.username }}</span>
                </router-link>
                <span><el-icon><Clock /></el-icon>  {{ formatTime(post.createTime) }}</span>
                <span><el-icon><View /></el-icon>  {{ post.viewCount }}</span>
                <span><el-icon><Pointer /></el-icon>  {{ post.likeCount }}</span>
              </div>
            </div>
          </transition-group>
          
          <div v-else-if="isLoading" class="loading-skeleton">
            <el-skeleton :rows="5" animated />
            <el-skeleton :rows="3" animated style="margin-top: 40px;" />
          </div>
          <el-empty v-else description="该分类下还没有文章哦" />
        </div>
      </el-col>
      
      <!-- 右侧边栏 -->
      <el-col :xs="24" :sm="24" :md="6" class="sidebar-col">
        <div class="sidebar-content-wrapper scrollable-column">
          <HotPostList />
          <el-card class="sidebar-card">
            <template #header>
              <div class="card-header">
                <el-icon><InfoFilled /></el-icon>
                <span>社区公约</span>
              </div>
            </template>
            <div class="notice-content">
              文明交流，理性发言，尊重原创
            </div>
          </el-card>
        </div>

        <footer class="pagination-container" v-if="!isLoading && pagination.total > pagination.size">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="pagination.total"
            :current-page="pagination.page"
            :page-size="pagination.size"
            @current-change="handlePageChange"
          />
        </footer>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import { Edit, User, Clock, View, Pointer, Collection, PriceTag, InfoFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import HotPostList from '@/components/HotPostList.vue';

const router = useRouter();
const posts = ref<any[]>([]);
const categories = ref<any[]>([]);
const tags = ref<any[]>([]);
const filterParams = reactive({ categoryId: null as number | null, tagName: null as string | null });
const isLoading = ref(true);
const isCategoryLoading = ref(true);
const isTagLoading = ref(true);
const pagination = reactive({ page: 1, size: 10, total: 0 });

const pageTitle = computed(() => {
    if (filterParams.categoryId) {
        const category = categories.value.find(c => c.id === filterParams.categoryId);
        return category ? category.name : '分类文章';
    }
    if (filterParams.tagName) { return `标签: #${filterParams.tagName}`; }
    return '技术分享与讨论';
});

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
const fetchCategories = async () => {
    isCategoryLoading.value = true;
    try {
        const response = await api.get('/categories');
        categories.value = response.data;
    } catch (error) {
        console.error("获取分类列表失败:", error);
        ElMessage.error("加载文章分类失败");
    } finally {
        isCategoryLoading.value = false;
    }
};
const fetchTags = async () => {
    isTagLoading.value = true;
    try {
        const response = await api.get('/tags');
        tags.value = response.data;
    } catch (error) {
        console.error("获取标签列表失败:", error);
        ElMessage.error("加载标签失败");
    } finally {
        isTagLoading.value = false;
    }
};
const fetchPosts = async () => {
  isLoading.value = true;
  try {
    const params: any = {
      page: pagination.page - 1,
      size: pagination.size,
    };
    if (filterParams.categoryId !== null) {
        params.categoryId = filterParams.categoryId;
    }
    if (filterParams.tagName !== null) {
        params.tagName = filterParams.tagName;
    }
    const response = await api.get('/posts', { params });
    posts.value = response.data.content;
    pagination.total = response.data.totalElements;
  } catch (error) {
    console.error("获取文章列表失败:", error);
    ElMessage.error("加载文章列表失败");
  } finally {
    isLoading.value = false;
  }
};
onMounted(() => {
  fetchCategories();
  fetchTags();
  fetchPosts();
});

const resetAndFetch = () => { pagination.page = 1; fetchPosts(); };
const filterByCategory = (categoryId: number | null) => { filterParams.categoryId = categoryId; filterParams.tagName = null; resetAndFetch(); };
const filterByTag = (tagName: string) => { filterParams.tagName = (filterParams.tagName === tagName) ? null : tagName; filterParams.categoryId = null; resetAndFetch(); };
const handlePageChange = (newPage: number) => { pagination.page = newPage; fetchPosts(); };
const goToCreatePost = () => router.push({ name: 'createPost' });
const goToPostDetail = (postId: number) => router.push({ name: 'postDetail', params: { id: postId } });
</script>

<style scoped>

.blog-list-view {
  height: 100vh;
  overflow: hidden;
  padding: 30px 24px;
  box-sizing: border-box;
  background-color: #f6f7fb;
  background-image: 
    radial-gradient(circle at 10% 20%, #fdebd0 0%, transparent 40%), 
    radial-gradient(circle at 90% 15%, #d9e2f3 0%, transparent 50%),
    radial-gradient(circle at 20% 90%, #f0d9e7 0%, transparent 50%);
}
.full-height-layout { 
  height: 100%; 
  display: flex;
}


.sidebar-col, .main-content-col {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.scrollable-column {
  overflow-y: auto;
  padding-right: 10px;
  -ms-overflow-style: none;
  scrollbar-width: thin;
  scrollbar-color: #dcdfe6 #f0f2f5;
}
.scrollable-column::-webkit-scrollbar { 
  width: 5px;
}
.scrollable-column::-webkit-scrollbar-track {
  background: #f0f2f5;
  border-radius: 3px;
}
.scrollable-column::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.sidebar-card, .post-list-wrapper {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border-radius: 16px; border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}
.sidebar-card { flex-shrink: 0; }
:deep(.el-card__header) { border-bottom: 1px solid rgba(0, 0, 0, 0.08); }
.card-header { font-size: 1.2rem; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 10px; }
.sidebar-content-wrapper { display: flex; flex-direction: column; gap: 30px; }


.category-list { list-style: none; padding: 0; margin: -10px; }
.category-list li {
  padding: 14px 20px; cursor: pointer; border-radius: 8px;
  transition: all 0.3s ease; font-size: 1rem; font-weight: 500;
  position: relative; color: #606266;
}
.category-list li:hover { background-color: rgba(0, 0, 0, 0.03); }
.category-list li.active { color: var(--el-color-primary); font-weight: 600; background-color: var(--el-color-primary-light-9); }
.tag-list { display: flex; flex-wrap: wrap; gap: 12px; }
.tag-item {
  cursor: pointer;
  transition: all 0.3s ease;
}
.tag-item:hover { transform: translateY(-2px); }


.blog-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px;
  flex-shrink: 0;
}
.page-title { font-size: 2rem; font-weight: 700; color: #303133; margin: 0; }
.post-list-wrapper { 
  flex-grow: 1;
  border-radius: 16px;
  padding: 12px;
  min-height: 0;
}
.post-item { 
  padding: 24px; margin-bottom: 16px; cursor: pointer; 
  border-radius: 12px;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}
.post-item:last-child { margin-bottom: 0; }
.post-item:hover { 
  transform: translateY(-5px); 
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
  background: #fff;
  border-color: rgba(0,0,0,0.05);
}
.post-title {
  margin: 0 0 12px 0; font-size: 1.5rem; font-weight: 600; color: #303133;
  transition: color 0.3s;
  display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 1; line-clamp: 1;
  overflow: hidden; text-overflow: ellipsis;
}
.post-item:hover .post-title { color: var(--el-color-primary); }
.post-excerpt {
  color: #606266; margin-bottom: 20px; line-height: 1.6;
  display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2; line-clamp: 2;
  overflow: hidden; text-overflow: ellipsis;
}
.post-meta {
  display: flex; align-items: center; flex-wrap: wrap;
  gap: 20px; font-size: 0.9rem; color: #909399;
  padding-top: 16px; border-top: 1px solid #e4e7ed;
}
.post-meta span { display: flex; align-items: center; }
.post-meta .el-icon { margin-right: 6px; }
.author-link { text-decoration: none; color: inherit; transition: color 0.3s; }
.author-link:hover { color: var(--el-color-primary); }

.loading-skeleton { padding: 20px; }
:deep(.el-skeleton__item) { background-color: #f0f2f5; }

/* --- 5. 右侧边栏与分页器 --- */
.sidebar-content-wrapper.scrollable-column {
  flex-grow: 1;
  min-height: 0;
}
.pagination-container {
  padding: 24px 0 0;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
  margin-top: auto;
}
:deep(.el-pagination.is-background .el-pager li),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .btn-prev) {
  background-color: #fff; border: 1px solid #e4e7ed;
}
:deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: var(--el-color-primary); color: #fff; border-color: var(--el-color-primary);
}
.notice-content { color: #606266; text-align: center; padding: 10px; font-size: 0.95rem; line-height: 1.6; }

/* 响应式调整 */
@media (max-width: 991px) {
  .blog-list-view { height: auto; }
  .full-height-layout { display: block; }
  .sidebar-col, .main-content-col { height: auto; display: block; }
  .scrollable-column { overflow-y: visible; padding: 15px; }
  .sidebar-col { margin-bottom: 20px; }
}
</style>