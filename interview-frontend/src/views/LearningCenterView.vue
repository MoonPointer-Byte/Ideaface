<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">学习与提升中心</h1>
      <p class="page-subtitle">根据行业趋势和岗位需求，为您精选的学习路线和课程资源。</p>
    </div>

    <!-- 推荐学习路线 -->
    <h2 class="section-title">推荐学习路线</h2>
    <div v-loading="loadingPaths">
      <el-row :gutter="20">
        <el-col v-for="path in learningPaths" :key="path.id" :span="24">
          <el-card class="path-card">
            <div class="path-content">
              <el-icon class="path-icon" size="32"><component :is="path.icon" /></el-icon>
              <div class="path-text">
                <h3>{{ path.title }}</h3>
                <p>{{ path.description }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loadingPaths && learningPaths.length === 0" description="暂无学习路线推荐" />
    </div>

    <!-- 精选课程资源 -->
    <h2 class="section-title">精选课程资源</h2>
    
    <!-- 控制栏: 搜索、分类、排序 -->
    <el-card class="controls-bar">
      <div class="controls-container">
        <div class="control-item search-item">
          <el-input v-model="searchTerm" placeholder="搜索课程名称或描述..." clearable :prefix-icon="Search" />
        </div>
        <div class="control-item filter-item">
          <el-radio-group v-model="activeCategory" size="small">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button v-for="(name, key) in categoryMap" :key="key" :label="key">{{ name }}</el-radio-button>
          </el-radio-group>
        </div>
        <div class="control-item sort-item">
          <el-select v-model="sortBy" placeholder="排序方式" size="small">
            <el-option label="按热门度" value="hot" />
            <el-option label="难度从低到高" value="difficulty-asc" />
            <el-option label="难度从高到低" value="difficulty-desc" />
          </el-select>
        </div>
      </div>
    </el-card>

    <div v-loading="loadingCourses">
      <!-- 横向滚动容器 -->
      <div class="horizontal-scroll-container">
        <transition-group name="list-h">
          <div v-for="course in processedCourses" :key="course.id" class="course-card-wrapper">
            <el-card shadow="hover" class="course-card" @click="openCourse(course.url)">
                <div class="course-header">
                   <div class="course-platform">{{ course.platform }}</div>
                   <el-tag v-if="course.isHot" type="danger" size="small" effect="dark">热门</el-tag>
                </div>
                <h3 class="course-title">{{ course.title }}</h3>
                <div class="course-rating">
                  <el-rate :model-value="course.rating" disabled size="small" />
                  <span class="rating-text">{{ course.rating.toFixed(1) }}</span>
                </div>
                <p class="course-description">{{ course.description }}</p>

                <div class="course-stats">
                  <span><el-icon><User /></el-icon> {{ course.instructor }}</span>
                  <span><el-icon><PriceTag /></el-icon> {{ course.difficulty }}</span>
                  <span><el-icon><Clock /></el-icon> {{ course.duration }}</span>
                  <span><el-icon><CollectionTag /></el-icon> {{ course.learners.toLocaleString() }}+</span>
                </div>

                <div class="course-footer">
                  <el-tag size="small">{{ categoryMap[course.category] || '通用' }}</el-tag>
                  <el-button text bg type="primary" size="small">前往学习</el-button>
                </div>
            </el-card>
          </div>
        </transition-group>
      </div>
      <el-empty v-if="!loadingCourses && processedCourses.length === 0" :description="emptyText" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { 
  Flag, TrendCharts, Management, User, Search, Clock, PriceTag, CollectionTag 
} from '@element-plus/icons-vue';
import type { Component } from 'vue';

// --- 数据接口定义 ---
interface LearningPath { id: number; title: string; description: string; icon: Component; }
interface Course { id: number; title: string; description: string; url: string; platform: string; category: string; instructor: string; difficulty: '初级' | '中级' | '高级'; isHot?: boolean; duration: string; learners: number; rating: number; }

// --- 响应式状态 ---
const loadingPaths = ref(true);
const loadingCourses = ref(true);
const learningPaths = ref<LearningPath[]>([]);
const courses = ref<Course[]>([]);

// 控制项状态
const searchTerm = ref('');
const activeCategory = ref('all');
const sortBy = ref('hot');

const categoryMap: { [key: string]: string } = { 'ai-engineer': '人工智能', 'backend-developer': '后端开发', 'product-manager': '产品经理', 'frontend-developer': '前端开发' };
const difficultyMap = { '初级': 1, '中级': 2, '高级': 3 };

// --- 计算属性 ---
const processedCourses = computed(() => {
  let result = [...courses.value];
  if (activeCategory.value !== 'all') { result = result.filter(c => c.category === activeCategory.value); }
  if (searchTerm.value.trim()) {
    const lower = searchTerm.value.toLowerCase();
    result = result.filter(c => c.title.toLowerCase().includes(lower) || c.description.toLowerCase().includes(lower));
  }
  switch (sortBy.value) {
    case 'difficulty-asc': result.sort((a, b) => difficultyMap[a.difficulty] - difficultyMap[b.difficulty]); break;
    case 'difficulty-desc': result.sort((a, b) => difficultyMap[b.difficulty] - difficultyMap[a.difficulty]); break;
    default: result.sort((a, b) => (b.isHot ? 1 : 0) - (a.isHot ? 1 : 0) || b.learners - a.learners); break;
  }
  return result;
});

const emptyText = computed(() => searchTerm.value.trim() ? `未找到与 "${searchTerm.value}" 相关的课程` : '暂无匹配的课程资源');

// --- 生命周期钩子 ---
onMounted(fetchData);

// --- 方法 ---
async function fetchData() {
  try {
    const [pathRes, courseRes] = await Promise.all([
      getMockLearningPaths(),
      getMockCourses()
    ]);
    learningPaths.value = pathRes;
    courses.value = courseRes;
  } catch (error) { console.error("加载学习中心数据失败:", error); } 
  finally {
    loadingPaths.value = false;
    loadingCourses.value = false;
  }
}

const openCourse = (url: string) => { window.open(url, '_blank', 'noopener,noreferrer'); };

// --- 模拟数据函数 ---
const getMockLearningPaths = (): Promise<LearningPath[]> => new Promise(resolve => setTimeout(() => resolve([ { id: 1, title: 'AI工程师快速入门', description: '从零基础到掌握机器学习核心算法，构建完整的AI知识体系。', icon: TrendCharts }, { id: 2, title: '高级后端开发（Go方向）', description: '深入Go语言底层，掌握微服务、容器化和高并发架构设计。', icon: Flag }, { id: 3, title: 'B端产品经理实战', description: '学习需求挖掘、产品设计与项目管理，成为顶尖的B端产品专家。', icon: Management }, ]), 300));
const getMockCourses = (): Promise<Course[]> => new Promise(resolve => setTimeout(() => resolve([ { id: 1, title: '李宏毅 机器学习/深度学习 (2022)', description: '台湾大学李宏毅教授的机器学习课程，内容详实生动，华人圈顶级公开课。', url: 'https://www.bilibili.com/video/BV1Wv411h7kN/', platform: 'Bilibili', category: 'ai-engineer', instructor: '李宏毅', difficulty: '中级', isHot: true, duration: '90小时', learners: 250000, rating: 4.9 }, { id: 2, title: 'Go语言基础+项目实战', description: '尚硅谷出品，从Go语言基础语法到真实项目开发，全面覆盖。', url: 'https://www.bilibili.com/video/BV1ME411Y71i/', platform: 'Bilibili', category: 'backend-developer', instructor: '尚硅谷', difficulty: '初级', duration: '35小时', learners: 180000, rating: 4.7 }, { id: 3, title: '人人都是产品经理-产品入门', description: '系统化地讲解产品经理的工作流程和必备技能，适合零基础入门。', url: 'https://www.youtube.com/watch?v=__b2zV-32iE', platform: 'YouTube', category: 'product-manager', instructor: '产品大牛', difficulty: '初级', duration: '8小时', learners: 55000, rating: 4.5 }, { id: 4, title: 'Vue 3 + TypeScript + Vite', description: '油管大神 Net Ninja 的 Vue 3 实战教程，学习最新前端技术栈。', url: 'https://www.youtube.com/playlist?list=PL4cUxeGkcC9hYYGbV60Vq3IXYNfDk8at1', platform: 'YouTube', category: 'frontend-developer', instructor: 'The Net Ninja', difficulty: '中级', isHot: true, duration: '12小时', learners: 320000, rating: 4.8 }, { id: 5, title: '吴恩达 深度学习专项课程', description: 'AI领域泰斗吴恩达的经典课程，系统学习深度学习，打下坚实理论基础。', url: 'https://www.coursera.org/specializations/deep-learning', platform: 'Coursera', category: 'ai-engineer', instructor: 'Andrew Ng', difficulty: '高级', isHot: true, duration: '70小时', learners: 980000, rating: 4.9 }, { id: 6, title: '系统设计面试 System Design Interview', description: '帮助你准备技术面试中的系统设计环节，深入探讨架构设计原则。', url: 'https://www.youtube.com/watch?v=gKqE_2l-4zQ', platform: 'YouTube', category: 'backend-developer', instructor: 'Gaurav Sen', difficulty: '高级', duration: '25小时', learners: 450000, rating: 4.8 }, { id: 7, 'title': 'Google UX Design Professional Certificate', 'description': '谷歌官方出品的UX设计职业证书课程，全面学习用户体验设计流程。', 'url': 'https://www.coursera.org/professional-certificates/google-ux-design', 'platform': 'Coursera', 'category': 'product-manager', 'instructor': 'Google', 'difficulty': '中级', 'duration': '120小时', 'learners': 850000, 'rating': 4.8 }, { id: 8, title: 'React - The Complete Guide', description: '最全面的React教程之一，从基础到高级特性，包含Hooks, Redux, Next.js等。', url: 'https://www.udemy.com/course/react-the-complete-guide-incl-redux/', platform: 'Udemy', category: 'frontend-developer', instructor: 'Maximilian', difficulty: '中级', isHot: true, duration: '50小时', learners: 760000, rating: 4.7 } ]), 500));
</script>

<style scoped>
/* --- 页面与标题样式 --- */
.page-container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.page-header { text-align: center; margin-bottom: 50px; }
.page-title { font-size: 2.2rem; font-weight: bold; margin-bottom: 15px; }
.page-subtitle { font-size: 1.1rem; color: var(--app-secondary-text-color); }
.section-title { font-size: 1.8rem; margin: 40px 0 20px; padding-bottom: 10px; border-bottom: 2px solid var(--el-color-primary); display: inline-block; }

/* --- 学习路线卡片 --- */
.path-card { margin-bottom: 20px; border-left: 4px solid var(--el-color-primary-light-3); transition: all 0.3s ease; }
.path-card:hover { transform: translateX(5px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.path-card :deep(.el-card__body) { padding: 25px; }
.path-content { display: flex; align-items: center; }
.path-icon { color: var(--el-color-primary); margin-right: 20px; flex-shrink: 0; }
.path-text h3 { margin: 0 0 8px; font-size: 1.2rem; }
.path-text p { margin: 0; color: var(--app-secondary-text-color); font-size: 0.95rem; }

/* --- 控制栏 --- */
.controls-bar { margin-bottom: 30px; }
.controls-container { display: flex; flex-wrap: wrap; align-items: center; gap: 20px; }
.control-item { display: flex; align-items: center; }
.search-item { flex-grow: 1; min-width: 250px; }
.filter-item { flex-shrink: 0; }
.sort-item { flex-shrink: 0; width: 150px; }

/* --- 横向滚动容器 --- */
.horizontal-scroll-container { display: flex; overflow-x: auto; padding-bottom: 20px; gap: 20px; }
.horizontal-scroll-container::-webkit-scrollbar { height: 8px; }
.horizontal-scroll-container::-webkit-scrollbar-track { background: #f1f1f1; border-radius: 4px; }
.horizontal-scroll-container::-webkit-scrollbar-thumb { background: #ccc; border-radius: 4px; }
.horizontal-scroll-container::-webkit-scrollbar-thumb:hover { background: #aaa; }

/* --- 课程卡片 --- */
.course-card-wrapper { transition: all 0.5s cubic-bezier(0.55, 0, 0.1, 1); }
.course-card { width: 380px; flex-shrink: 0; cursor: pointer; border-radius: 12px; transition: all 0.3s ease; height: 100%; display: flex; flex-direction: column; border: 1px solid var(--app-border-color); }
.course-card:hover { transform: translateY(-5px); box-shadow: 0 8px 20px rgba(0,0,0,0.1); border-color: var(--el-color-primary); }
.el-card :deep(.el-card__body) { flex-grow: 1; display: flex; flex-direction: column; padding: 20px; }

.course-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.course-platform { font-size: 0.8rem; color: #999; font-weight: bold; }
.course-title { font-size: 1.25rem; margin: 0 0 8px; line-height: 1.4; font-weight: 600; }
.course-rating { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.rating-text { color: #E6A23C; font-weight: bold; font-size: 0.9rem; }
.course-description { font-size: 0.9rem; color: #666; height: 3.6em; overflow: hidden; margin-bottom: auto; }

.course-stats { font-size: 0.85rem; color: #888; display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; margin-top: 15px; padding-bottom: 10px; }
.course-stats span { display: flex; align-items: center; gap: 6px; }

.course-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 15px; border-top: 1px solid var(--app-border-color); padding-top: 15px; }

/* 动画效果适配 */
.list-h-enter-from, .list-h-leave-to { opacity: 0; transform: translateX(30px); }
.list-h-leave-active { position: absolute; }
</style>