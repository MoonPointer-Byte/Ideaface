<template>
  <div class="agent-detail-wrapper">
    <!-- 1. 顶部导航和智能体标识 -->
    <el-page-header class="page-header" @back="goBack">
      <template #content>
        <div class="agent-identity">
          <div class="icon-bg" :style="{ backgroundColor: agent.color }">
            <el-icon :size="22" color="#fff"><component :is="agent.icon" /></el-icon>
          </div>
          <span class="agent-name">{{ agent.name }}</span>
        </div>
      </template>
    </el-page-header>

    <div class="content-center">
      <!-- 2. 交互核心区：动态加载不同智能体的输入组件 -->
      <div class="interaction-zone">
        <p class="prompt-guide">{{ agent.prompt }}</p>
        
        <!-- 使用动态组件加载对应智能体的输入界面 -->
        <component 
          :is="agent.inputComponent" 
          @submit="runAnalysis" 
          :is-loading="isLoading"
        />
      </div>

      <!-- 3. 结果展示区 -->
      <div class="results-wrapper">
        <!-- 加载状态 -->
        <div v-if="isLoading" class="loading-state">
          <el-icon class="is-loading" :size="30"><Loading /></el-icon>
          <p>正在为您生成报告，请稍候...</p>
        </div>

        <!-- 结果展示 -->
        <template v-else-if="analysisResult">
           <el-divider><el-icon><MagicStick /></el-icon></el-divider>
           <!-- 使用动态组件加载对应智能体的结果界面 -->
           <component :is="agent.resultComponent" :result="analysisResult" />
        </template>
        
        <!-- 初始空状态 -->
        <div v-else class="empty-state">
          <el-icon :size="60"><Document /></el-icon>
          <p>分析报告将在此处生成</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, defineAsyncComponent, markRaw } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Edit, DataAnalysis, TrendCharts, Loading, MagicStick, Document,
} from '@element-plus/icons-vue';

// --- 组件定义 ---
// 使用 markRaw 包装异步组件，避免被 Vue 代理，提升性能
const ResumeInput = markRaw(defineAsyncComponent(() => import('@/components/agent/ResumeInput.vue')));
const CareerForm = markRaw(defineAsyncComponent(() => import('@/components/agent/CareerForm.vue')));
const OptimizerResult = markRaw(defineAsyncComponent(() => import('@/components/agent/OptimizerResult.vue')));
const EvaluatorResult = markRaw(defineAsyncComponent(() => import('@/components/agent/EvaluatorResult.vue')));
const CareerPlanResult = markRaw(defineAsyncComponent(() => import('@/components/agent/CareerPlanResult.vue')));


// --- 路由和状态管理 ---
const route = useRoute();
const router = useRouter();
const agentId = computed(() => route.path.split('/').pop() || '');

const isLoading = ref(false);
const analysisResult = ref<any>(null);


// --- 智能体数据库 ---
// 将每个智能体的配置信息、专属组件和提示语整合在一起
const agentDatabase: Record<string, any> = {
  'resume-optimizer': {
    name: '简历优化智能体',
    icon: Edit,
    color: '#67C23A',
    prompt: '粘贴您的简历内容，我将为您提供具体的优化建议。',
    inputComponent: ResumeInput,
    resultComponent: OptimizerResult,
  },
  'resume-evaluator': {
    name: '简历评估智能体',
    icon: DataAnalysis,
    color: '#409EFF',
    prompt: '粘贴您的简历内容，我将为您生成一份多维度评估报告。',
    inputComponent: ResumeInput,
    resultComponent: EvaluatorResult,
  },
  'career-development': {
    name: '岗位发展智能体',
    icon: TrendCharts,
    color: '#E6A23C',
    prompt: '请填写您的技能和目标，我将为您规划职业发展路线。',
    inputComponent: CareerForm,
    resultComponent: CareerPlanResult,
  },
};

const agent = computed(() => agentDatabase[agentId.value] || { name: '未知' });

// --- 方法 ---
const goBack = () => router.push('/agent');

const runAnalysis = (payload: any) => {
  if (!payload) {
    ElMessage.warning('请输入有效内容！');
    return;
  }
  isLoading.value = true;
  analysisResult.value = null; // 清空旧结果
  
  // 模拟API调用
  setTimeout(() => {
    // 这里可以根据 agentId.value 调用不同的后端API
    // 我们用一个函数来模拟不同类型的返回结果
    analysisResult.value = generateMockResult(agentId.value, payload);
    isLoading.value = false;
  }, 2000);
};

// 模拟后端数据生成器
const generateMockResult = (id: string, payload: any) => {
  if (id === 'resume-optimizer') {
    return [
      { category: '关键动词', title: '增强动作表达', content: '建议将“负责开发”替换为“主导开发”、“实现”或“构建”，以突出您的主观能动性。', type: 'primary' },
      { category: '量化成果', title: '具体化项目影响', content: '在项目描述中，将“提升了性能”具体化为“通过代码重构，将页面加载速度提升了约30%”。', type: 'success' },
      { category: '技能匹配', title: '对齐岗位要求', content: '您的技能清单缺少目标岗位要求的“Webpack性能优化”经验，建议补充相关项目或知识点。', type: 'warning' },
    ];
  }
  if (id === 'resume-evaluator') {
    const scores = [Math.floor(Math.random()*20+80), Math.floor(Math.random()*20+75), Math.floor(Math.random()*20+65), Math.floor(Math.random()*20+70), Math.floor(Math.random()*20+80)];
    return {
      score: Math.round(scores.reduce((a, b) => a + b, 0) / scores.length),
      strength: '技能掌握扎实，项目经验丰富。',
      weakness: '项目成果量化不足，缺乏数据支撑。',
      radarData: scores,
    };
  }
  if (id === 'career-development') {
    return {
      roles: ['高级前端工程师', '前端架构师', '全栈开发工程师 (Node.js)'],
      steps: [
        { title: '深化现有技能', desc: `基于您掌握的 ${payload.skills.join(', ')}，建议深入学习Vue3响应式原理。` },
        { title: '拓宽技术视野', desc: '学习Webpack/Vite等构建工具的底层原理和性能优化。' },
        { title: '软技能提升', desc: '参与至少一个开源项目，锻炼跨团队协作和沟通能力。' },
      ],
    };
  }
  return null;
};
</script>

<style scoped>
.agent-detail-wrapper {
  background-color: #f7f8fc;
  min-height: calc(100vh - 60px);
  padding: 20px;
}

.page-header {
  max-width: 900px;
  margin: 0 auto;
}

.agent-identity {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-bg {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  box-shadow: 0 4px 8px -2px rgba(0,0,0,0.1);
}

.agent-name {
  font-size: 1.3rem;
  font-weight: 600;
  color: #303133;
}

.content-center {
  max-width: 900px;
  margin: 20px auto 0;
}

.interaction-zone {
  background-color: #fff;
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 8px 24px rgba(0,0,0,0.03);
}

.prompt-guide {
  margin: 0 0 20px;
  color: #606266;
  font-size: 1rem;
  text-align: center;
}

.results-wrapper {
  margin-top: 20px;
  min-height: 200px;
  position: relative;
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  padding: 40px 0;
  text-align: center;
  transition: opacity 0.3s ease;
}

.loading-state p, .empty-state p {
  margin-top: 15px;
  font-size: 1rem;
}

.el-divider {
  margin-bottom: 25px;
}
</style>