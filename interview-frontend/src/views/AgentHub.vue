<template>
  <div class="page-background">
    <!-- 装饰性的背景形状 -->
    <div class="shape shape-1"></div>
    <div class="shape shape-2"></div>
    <div class="grid-overlay"></div>

    <div class="agent-hub-container">
      <!-- 页面主标题 (玻璃拟态效果) -->
      <div class="hub-header">
        <h1>您的专属AI求职助手</h1>
        <p class="subtitle">选择一个智能体，开启您的求职加速之旅</p>
      </div>

      <!-- 智能体卡片网格 -->
      <div class="agent-grid">
        <div v-for="agent in agentList" :key="agent.name">
          <el-card 
            class="agent-card" 
            shadow="never"
            @mousemove="handleCardMouseMove"
            @click="navigateTo(agent.path)" 
          >
            <!-- 追光效果的伪元素 -->
            <div class="shine-effect"></div>
            <div class="card-content">
              <div 
                class="icon-wrapper" 
                :style="{ background: agent.color, boxShadow: `0 6px 15px ${agent.shadowColor}` }"
              >
                <el-icon :size="32"><component :is="agent.icon" /></el-icon>
              </div>
              <h3 class="agent-title">{{ agent.title }}</h3>
              <p class="agent-description">{{ agent.description }}</p>
            </div>
            <div class="card-action">
              <span>立即探索 <el-icon><Right /></el-icon></span>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  DocumentChecked,
  MagicStick,
  Compass,
  Right
} from '@element-plus/icons-vue';

const router = useRouter();

const agentList = ref([
  {
    name: 'ResumeAssessment',
    title: '简历智能评估',
    description: '上传简历，AI将进行多维度打分，评估与目标岗位的匹配度。',
    icon: DocumentChecked,
    path: '/resume-assessment',
    color: 'linear-gradient(135deg, #6b73ff 0%, #000dff 100%)', // 深邃科技蓝
    shadowColor: 'rgba(0, 13, 255, 0.3)',
  },
  {
    name: 'ResumeOptimizer',
    title: '简历智能优化',
    description: 'AI化身资深HR，逐条分析并提供具体优化建议，助您脱颖而出。',
    icon: MagicStick,
    path: '/resume-optimizer',
    color: 'linear-gradient(135deg, #3c8ce7 0%, #00eaff 100%)', // 清新天空蓝
    shadowColor: 'rgba(0, 234, 255, 0.3)',
  },
  {
    name: 'CareerPathPlanner',
    title: '岗位发展规划',
    description: '输入现状和目标，AI为您量身定制从现在到未来的清晰职业发展路径。',
    icon: Compass,
    path: '/career-planner',
    color: 'linear-gradient(135deg, #ff61d2 0%, #fe9090 100%)', // 活力珊瑚粉
    shadowColor: 'rgba(255, 97, 210, 0.3)',
  },
]);

const navigateTo = (path: string) => {
  router.push(path);
};

const handleCardMouseMove = (event: MouseEvent) => {
  const card = (event.currentTarget as HTMLElement);
  const rect = card.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;
  card.style.setProperty('--x', `${x}px`);
  card.style.setProperty('--y', `${y}px`);
};
</script>

<style scoped>

.page-background {
  background: #c6bcc1; 
  padding: 80px 24px;
  min-height: calc(100vh - 50px);
  position: relative;
  overflow: hidden;
}
.page-background::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(-45deg, #1e2a50, #2b2244, #4a2f3a, #1c3e3e);
  background-size: 400% 400%;
  animation: gradient-animation 20s ease infinite;
  opacity: 0.6;
}
@keyframes gradient-animation {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.grid-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.05) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.5;
}
.shape { position: absolute; border-radius: 50%; filter: blur(120px); }
.shape-1 { width: 400px; height: 400px; background: #3c8ce7; top: -150px; left: -150px; opacity: 0.3; }
.shape-2 { width: 500px; height: 500px; background: #ff61d2; bottom: -200px; right: -200px; opacity: 0.3; }

.agent-hub-container { max-width: 1200px; margin: 0 auto; position: relative; z-index: 1; }
.hub-header {
  text-align: center; margin-bottom: 80px; padding: 40px 20px;
  background: rgba(17, 24, 39, 0.3);
  border-radius: 20px;
  backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: fade-in-down 0.8s ease-out;
}
@keyframes fade-in-down { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
.hub-header h1 { font-size: 2.8rem; font-weight: 700; color: #fff; text-shadow: 0 2px 8px rgba(0,0,0,0.3); margin: 0; }
.hub-header .subtitle { font-size: 1.2rem; color: rgba(255, 255, 255, 0.7); margin-top: 16px; }


.agent-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 40px; }
.agent-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  cursor: pointer; overflow: hidden; position: relative;
  height: 100%; display: flex; flex-direction: column;
  border: 1px solid transparent; /* 占位边框 */
}


.agent-card::before {
  content: "";
  position: absolute;
  inset: -1px;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(255,255,255,0.3), rgba(255,255,255,0.1));
  z-index: -1;
  transition: all 0.4s ease;
}
.agent-card:hover::before {
  background: linear-gradient(135deg, #00eaff, #ff61d2);
}
.agent-card:hover {
  transform: translateY(-12px) scale(1.03);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}


.agent-card .shine-effect {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: radial-gradient(circle at var(--x, 50%) var(--y, 50%), rgba(255,255,255,0.8), transparent 40%);
  opacity: 0; transition: opacity 0.5s; pointer-events: none;
}
.agent-card:hover .shine-effect { opacity: 1; }

.card-content { padding: 40px; text-align: center; flex-grow: 1; }
.icon-wrapper {
  width: 80px; height: 80px; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; margin: 0 auto 28px;
  color: #fff; transition: all 0.4s ease;
}
.agent-card:hover .icon-wrapper { transform: translateY(-5px) scale(1.15) rotate(10deg); }
.agent-title { font-size: 1.6rem; font-weight: 600; margin: 0 0 16px; color: #1f2937; }
.agent-description {
  font-size: 1rem; color: #4b5563; line-height: 1.7; overflow: hidden;
  text-overflow: ellipsis; display: -webkit-box; -webkit-box-orient: vertical;
  -webkit-line-clamp: 3; line-clamp: 3;
}
.card-action {
  background-color: transparent; color: #6b7280; padding: 20px;
  text-align: center; font-weight: 500; transition: all 0.3s ease;
  border-top: 1px solid #e5e7eb;
}
.agent-card:hover .card-action {
  background: transparent;
  color: #3c8ce7;
  font-weight: 600;
}
.card-action span {
  display: flex; align-items: center; justify-content: center; gap: 8px;
}
</style>