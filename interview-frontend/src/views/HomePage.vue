<template>
  <div class="home-page">
    <!-- 英雄/主视觉区域 -->
    <div class="hero-section">
      <div class="hero-content page-container">
        <h1 class="hero-title">Ideaface质面未来</h1>
        <p class="hero-subtitle">智拟真面  精评速破</p>
        <el-button type="primary" size="large" @click="router.push('/interview')" round class="hero-cta-button">
          <el-icon><CaretRight /></el-icon>
          立即开始模拟面试
        </el-button>
      </div>
    </div>

    <!-- 核心功能区域 -->
    <div class="section-container feature-section">
      <div class="page-container">
        <h2 class="section-title">释放您的面试潜力</h2>
        <p class="section-description">我们不仅仅是模拟，更是您口袋里的私人面试教练。</p>
        <el-row :gutter="30">
          <el-col v-for="feature in features" :key="feature.title" :xs="24" :sm="12" :md="6">
            <div class="feature-card">
              <div class="feature-icon-wrapper" :style="{ backgroundColor: feature.bgColor }">
                <el-icon :size="32" class="feature-icon"><component :is="feature.icon" /></el-icon>
              </div>
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.description }}</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 工作流程区域 -->
    <!-- <div class="section-container process-section">
      <div class="page-container">
        <h2 class="section-title">三步开启自信之旅</h2>
        <p class="section-description">简单直观的操作，让您专注于面试表现本身。</p>
        <div class="process-steps-wrapper">
          <div v-for="(step, index) in processSteps" :key="step.title" class="process-step">
            <div class="step-icon-wrapper">
              <el-icon :size="32"><component :is="step.icon" /></el-icon>
            </div>
            <div class="step-content">
              <h4>{{ step.title }}</h4>
              <p>{{ step.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div> -->

    <!-- 热门岗位区域 -->
    <!-- <div class="section-container positions-section">
        <div class="page-container">
            <h2 class="section-title">覆盖海量热门岗位</h2>
            <p class="section-description">无论您身处哪个热门赛道，我们都有丰富的题库为您准备。</p>
            <div class="positions-list">
                <el-tag v-for="pos in positions" :key="pos" type="info" size="large" round>{{ pos }}</el-tag>
            </div>
        </div>
    </div> -->

    <!-- 热门课程区域 -->
    <div class="course-section">
      <div class="page-container">
        <h2 class="section-title light">精选提升课程</h2>
        <p class="section-description light">根据您的表现，智能推荐高质量课程，助您弥补能力短板。</p>
        <el-row :gutter="20" v-loading="loading">
          <el-col v-for="course in courses" :key="course.id" :xs="24" :sm="12" :md="8">
            <el-card shadow="hover" class="course-card" @click="openCourse(course.url)">
              <div class="course-platform">{{ course.platform }}</div>
              <h3 class="course-title">{{ course.title }}</h3>
              <p class="course-description">{{ course.description }}</p>
              <div class="course-footer">
                <el-tag size="small">{{ categoryMap[course.category] || '通用' }}</el-tag>
                <el-button text bg type="primary" size="small">前往学习</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
    
    <!-- 页脚 -->
    <footer class="page-footer">
      <!-- <div class="page-container footer-container">
        <div class="footer-brand">
            <h3>IdeaFace</h3>
            <p>您的私人AI面试教练</p>
        </div>
        <div class="footer-links">
            <a href="#">功能</a>
            <a href="#">课程</a>
            <a href="#">关于我们</a>
            <a href="#">联系合作</a>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© {{ new Date().getFullYear() }} IdeaFace. All Rights Reserved.</p>
      </div> -->
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import { ChatDotRound, DataAnalysis, List, MagicStick, CaretRight, /*EditPen, VideoCamera, Files*/ } from '@element-plus/icons-vue';

interface Course { id: number; title: string; description: string; url: string; platform: string; category: string; }

const router = useRouter();
const loading = ref(true);
const courses = ref<Course[]>([]);

const features = ref([
    { icon: ChatDotRound, title: "交互式面试", description: "与AI面试官进行多轮问答，模拟真实面试场景，告别紧张。", bgColor: '#ecf5ff' },
    { icon: DataAnalysis, title: "多模态分析", description: "综合分析您的语言、表情和内容，提供全面客观的评估报告。", bgColor: '#f0f9eb' },
    { icon: List, title: "海量行业题库", description: "覆盖多行业多岗位，提供丰富的面试真题进行无限次练习。", bgColor: '#fdf6ec' },
    { icon: MagicStick, title: "个性化建议", description: "根据您的表现，智能推荐提升方向和精选学习资源。", bgColor: '#faf0f0' }
]);
// const processSteps = ref([
//     { icon: EditPen, title: "创建面试", description: "选择您心仪的岗位，系统将为您生成专属面试。" },
//     { icon: VideoCamera, title: "开始模拟", description: "与AI面试官沉浸式视频对话，展现最真实的自己。" },
//     { icon: Files, title: "获取报告", description: "获取详细的多维度能力分析报告与提升建议。" }
// ]);
// const testimonials = ref([
//     { name: "张伟", role: "后端开发工程师", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026704d", quote: "AI面试官的问题非常专业，直击要点。分析报告让我清楚地看到了自己的逻辑漏洞，面试前用它突击真的太有用了！" },
//     { name: "王芳", role: "产品经理", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026703d", quote: "作为一个社恐，我真的很怕面试。IdeaFace让我可以在家反复练习，大大缓解了我的紧张情绪，现在面对真人面试官自信多了。" },
//     { name: "李娜", role: "算法工程师", avatar: "https://i.pravatar.cc/150?u=a04258114e29026702d", quote: "最让我惊艳的是它的多模态分析，连我的一些不自觉的小动作和微表情都能捕捉到并给出建议，非常细节，非常强大。" }
// ]);
// const positions = ref(['Java开发', '前端开发', '产品经理', 'Go开发', '算法工程师', '数据分析师', 'UI设计师', '测试工程师', 'C++开发']);
const categoryMap: { [key: string]: string } = { 'ai-engineer': '人工智能', 'backend-developer': '后端开发', 'product-manager': '产品' };

onMounted(async () => {
    try {
        const response = await api.get('/content/courses');
        courses.value = response.data.slice(0, 3);
    } catch (error) { console.error("获取推荐课程失败:", error); }
    finally { loading.value = false; }
});

const openCourse = (url: string) => window.open(url, '_blank');
</script>

<style scoped>
/* --- 全局与动画 --- */
.home-page { 
    background-color: #F9FAFB;

    font-family: Montserrat, "PingFang SC", "Microsoft YaHei UI", "Microsoft YaHei", "Noto Sans SC", "Source Han Sans SC", "Helvetica Neue", Arial, sans-serif;
}
@keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

/* --- 通用区块样式 --- */
.page-container { max-width: 1100px; margin: 0 auto; padding: 0 20px; }
.section-container { padding: 80px 0; }
.section-title { text-align: center; font-size: 2rem; font-weight: 700; margin-bottom: 16px; color: #1F2937; }
.section-title.light { color: white; }
.section-description { text-align: center; font-size: 1.1rem; color: #6B7280; margin-bottom: 48px; max-width: 600px; margin-left: auto; margin-right: auto; }
.section-description.light { color: rgba(255, 255, 255, 0.8); }

/* --- 英雄区 --- */
.hero-section {
    padding: 96px 0;
    background: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('https://images.unsplash.com/photo-1521737604893-d14cc237f11d?ixlib=rb-4.0.3&q=85&fm=jpg&crop=entropy&cs=srgb&w=1600') no-repeat center center;
    background-size: cover; color: white; text-align: center;
}
.hero-content { animation: fadeIn 1s ease-in-out; }
.hero-title { font-size: clamp(2.5rem, 6vw, 3.5rem); font-weight: 800; margin-bottom: 24px; text-shadow: 0 2px 4px rgba(0,0,0,0.5); }
.hero-subtitle { font-size: clamp(1.1rem, 2.5vw, 1.25rem); margin-bottom: 40px; max-width: 600px; margin-left: auto; margin-right: auto; opacity: 0.9; line-height: 1.6; }
.hero-cta-button { transform: scale(1.1); }

/* --- 特色功能区 --- */
.feature-section { background-color: white; }
.feature-card { text-align: center; padding: 24px; }
.feature-icon-wrapper {
    margin: 0 auto 24px; width: 64px; height: 64px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
    box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}
.feature-icon { color: var(--el-color-primary); }
.feature-card h3 { font-size: 1.2rem; font-weight: 600; margin-bottom: 8px; color: #374151; }
.feature-card p { font-size: 1rem; color: #6B7280; line-height: 1.6; }

/* --- 工作流程区 --- */
.process-steps-wrapper { display: flex; justify-content: space-between; position: relative; }
.process-steps-wrapper::before {
    content: ''; position: absolute; top: 32px; left: 15%; right: 15%; height: 2px;
    background-image: linear-gradient(to right, #D1D5DB 50%, transparent 50%); background-size: 16px 2px; background-repeat: repeat-x; z-index: 1;
}
.process-step { display: flex; flex-direction: column; align-items: center; text-align: center; max-width: 250px; position: relative; z-index: 2;}
.step-icon-wrapper { width: 64px; height: 64px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background-color: white; border: 2px solid #E5E7EB; margin-bottom: 24px; color: var(--el-color-primary); }
.process-step h4 { font-size: 1.1rem; font-weight: 600; color: #374151; margin-bottom: 8px; }
.process-step p { font-size: 0.95rem; color: #6B7280; line-height: 1.5; }

/* --- 用户评价区 --- */
.testimonial-section { background-color: white; }
.testimonial-card { height: 100%; border: 1px solid #e5e7eb; border-radius: 12px; background-color: white; position: relative; overflow: hidden; }
.testimonial-card .el-card__body { padding: 32px; display: flex; flex-direction: column; height: 100%; }
.quote-mark { position: absolute; top: 10px; left: 20px; font-size: 5rem; color: #F3F4F6; z-index: 1; }
.testimonial-quote { position: relative; z-index: 2; font-size: 1rem; color: #4B5563; line-height: 1.7; flex-grow: 1; margin-bottom: 24px; }
.testimonial-footer { display: flex; align-items: center; position: relative; z-index: 2; }
.user-info { margin-left: 12px; }
.user-name { display: block; font-weight: 600; color: #1F2937; }
.user-role { font-size: 0.85rem; color: #6B7280; }

/* --- 热门岗位区 --- */
.positions-list { display: flex; flex-wrap: wrap; justify-content: center; gap: 16px; }
.positions-list .el-tag { height: 40px; font-size: 1rem; background-color: #e5e7eb; color: #4b5563; border: none; }

/* --- 课程区 --- */
.course-section { padding: 80px 0; background-color: #111827; }
.course-card { border-radius: 12px; transition: all 0.3s ease; height: 100%; display: flex; flex-direction: column; border: 1px solid #374151; }
.course-card:hover { transform: translateY(-5px); box-shadow: 0 8px 25px rgba(0,0,0,0.2); border-color: #4b5563; }
.course-card :deep(.el-card__body) { flex-grow: 1; display: flex; flex-direction: column; }
.course-platform { font-size: 0.8rem; color: #000000; margin-bottom: 10px; }
.course-title { font-size: 1.2rem; margin-bottom: 10px; flex-grow: 1; color: #000000; }
.course-description { font-size: 0.9rem; color: #000000; min-height: 3.6em; overflow: hidden; margin-bottom: auto; }
.course-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; border-top: 1px solid #374151; padding-top: 10px; }

/* --- 页脚 --- */
.page-footer { background-color: #111827; color: #9CA3AF; padding-top: 60px; }
.footer-container { display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 40px; flex-wrap: wrap; gap: 20px; }
.footer-brand h3 { font-size: 1.5rem; color: white; margin-bottom: 8px; }
.footer-links { display: flex; gap: 24px; }
.footer-links a { color: #9CA3AF; text-decoration: none; transition: color 0.2s ease; }
.footer-links a:hover { color: white; }
.footer-bottom { border-top: 1px solid #374151; text-align: center; padding: 20px; font-size: 0.9rem; }
</style>