<template>
  <div class="page-container">
    <div class="background-grid"></div>
    <div class="title-section">
      <h1 class="main-title">选择面试岗位</h1>
      <p class="subtitle">请选择您的目标岗位，开启与专属AI面试官的对话</p>
    </div>

    <transition-group
      name="card-fade"
      tag="el-row"
      :gutter="32"
      justify="center"
      appear
      class="card-row"
    >
      <el-col
        v-for="(position, index) in positions"
        :key="position.id"
        :xs="24" :sm="12" :md="8" :lg="6"
        class="position-col"
        :style="{ 'transition-delay': index * 80 + 'ms' }"
      >
        <div 
          class="position-card" 
          @click="selectPosition(position.id, position.name)"
        >
          <!-- 新增的卡片内部辉光效果 -->
          <div class="card-glow"></div>
          <div class="card-content">
            <!-- 新增的图标独立容器 -->
            <div class="icon-container">
              <el-icon class="card-icon" :size="40">
                <component :is="position.icon" />
              </el-icon>
            </div>
            <h2 class="card-title">{{ position.name }}</h2>
            <p class="card-description">{{ position.description }}</p>
          </div>
        </div>
      </el-col>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
// 导入 Compass 图标用于算法岗
import { Cpu, Odometer, Monitor, Finished, DataLine, Tools, Notebook, Compass } from '@element-plus/icons-vue';

const router = useRouter();

// 在列表中新增“算法工程师”
const positions = ref([
  { id: 'frontend-developer', name: '前端开发工程师', description: '负责网站和应用用户界面的设计、开发与优化。', icon: Monitor },
  { id: 'backend-developer', name: '后端开发工程师', description: '负责服务器端程序、数据库和API的设计与开发。', icon: Odometer },
  { id: 'algorithm-engineer', name: '算法工程师', description: '专注于设计、实现和优化解决特定问题的算法模型。', icon: Compass },
  { id: 'ai-engineer', name: '人工智能工程师', description: '负责机器学习模型的设计、训练和部署。', icon: Cpu },
  { id: 'qa-engineer', name: '测试工程师', description: '负责产品的功能、性能和安全测试，保障质量。', icon: Finished },
  { id: 'big-data-engineer', name: '大数据工程师', description: '负责大规模数据的采集、处理、分析和存储。', icon: DataLine },
  { id: 'devops-engineer', name: '运维工程师', description: '负责系统的部署、监控、维护和自动化。', icon: Tools },
  { id: 'product-manager', name: '产品经理', description: '负责产品规划、需求分析和项目推进。', icon: Notebook }
]);

const selectPosition = (positionId: string, positionName: string) => {
  ElMessage.success(`您已选择【${positionName}】，即将进入面试间...`);
  setTimeout(() => {
    router.push({ name: 'interviewSession', params: { positionId } });
  }, 1000);
};

// 由于新的设计不再需要实时追踪鼠标，原有的 handleMouseMove 函数已被移除
</script>

<style scoped>
:root {
  --bg-color: #121212;
  /* 卡片背景色调整为更有层次的径向渐变 */
  --card-bg-color: radial-gradient(circle at 50% 0%, #282828, #1d1d1d);
  --grid-color: rgba(0, 246, 255, 0.05); /* 网格颜色与主题色统一 */
  --text-primary: #f0f0f0;
  --text-secondary: #a0a0a0;
  --accent-color: #00f6ff;
  --card-border-color: rgba(255, 255, 255, 0.1);
  --card-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  --card-hover-shadow: 0 8px 24px rgba(0, 246, 255, 0.1);
}

.page-container {
  position: relative;
  overflow: hidden;
  padding: 60px 20px;
  min-height: 100vh;
  background-color: var(--bg-color);
  color: var(--text-primary);
  z-index: 1;
}

/* 动态网格背景 */
@keyframes pan-grid {
  0% { background-position: 0% 0%; }
  100% { background-position: 50px 50px; }
}

.background-grid {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background-image: linear-gradient(var(--grid-color) 1px, transparent 1px),
                    linear-gradient(90deg, var(--grid-color) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: pan-grid 20s linear infinite;
  z-index: -1;
  opacity: 0.5;
}

.title-section {
  text-align: center;
  margin-bottom: 70px;
}

.main-title {
  font-size: 3.2rem;
  font-weight: 800;
  letter-spacing: -1px;
  color: #000000;
  margin-bottom: 15px;
}

.subtitle {
  font-size: 1.25rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
}

.card-row {
  display: flex;
  flex-wrap: wrap;
}

.position-col {
  margin-bottom: 32px;
}

/* --- 卡片样式美化核心 --- */
.position-card {
  position: relative;
  width: 100%;
  height: 100%;
  background: var(--card-bg-color);
  border-radius: 16px; /* 更圆润的边角 */
  cursor: pointer;
  overflow: hidden;
  border: 1px solid var(--card-border-color);
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  box-shadow: var(--card-shadow);
}

.position-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--card-hover-shadow);
  border-color: var(--accent-color);
}

/* 卡片内部辉光效果 */
.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 50% 50%, transparent, var(--accent-color));
  transform: scale(0);
  opacity: 0;
  transition: transform 0.5s ease, opacity 0.5s ease;
  pointer-events: none;
  z-index: 1;
}

.position-card:hover .card-glow {
  transform: scale(4);
  opacity: 0.08; /* 非常 subtle 的辉光 */
}

.card-content {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 30px 25px 40px; /* 调整内边距 */
  height: 100%;
  z-index: 2; /* 确保内容在辉光之上 */
}

/* 图标容器样式 */
.icon-container {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  margin-bottom: 30px;
  border: 1px solid transparent;
  transition: all 0.3s ease;
}

.position-card:hover .icon-container {
  background-color: rgba(0, 246, 255, 0.1);
  border-color: rgba(0, 246, 255, 0.3);
}

.card-icon {
  color: var(--text-secondary);
  transition: color 0.3s ease, transform 0.3s ease;
}

.position-card:hover .card-icon {
  color: var(--accent-color);
  transform: scale(1.1);
}

.card-title {
  margin: 0 0 10px 0; /* 调整外边距 */
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.position-card:hover .card-title {
  color: #ffffff;
}

.card-description {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.6;
  min-height: 4.8em;
}

/* 入场动画 */
.card-fade-enter-active {
  transition: all 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.card-fade-enter-from {
  opacity: 0;
  transform: translateY(50px);
}
</style>