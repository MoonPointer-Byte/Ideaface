<!-- src/views/agent/index.vue -->

<template>
  <div class="agent-center-container">
    <div class="page-header">
      <h1 class="page-title">智能体中心</h1>
      <p class="page-description">
        选择一个AI智能体开始您的求职提升之旅。
      </p>
    </div>

    <div class="agent-list">
      <el-row :gutter="24">
        <el-col
          v-for="agent in agentList"
          :key="agent.id"
          :xs="24"
          :sm="12"
          :lg="8"
        >
          <el-card 
            class="agent-card" 
            shadow="never"
            :style="{ '--card-theme-color': agent.color }"
          >
            <div class="card-header">
              <div class="icon-wrapper">
                <el-icon :size="26" color="#fff">
                  <component :is="agent.icon" />
                </el-icon>
              </div>
              <h3 class="agent-name">{{ agent.name }}</h3>
            </div>
            
            <p class="agent-description">{{ agent.description }}</p>

            <div class="card-footer">
              <el-button
                class="start-button"
                :icon="Promotion"
                @click="startAgent(agent)"
              >
                立即开始
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
// 【【【修改】】】: 更新导入的图标
import { Edit, DataAnalysis, TrendCharts, Promotion } from '@element-plus/icons-vue';
//import { ElMessage } from 'element-plus';

const router = useRouter();

// 【【【修改】】】: 更新智能体数据列表以匹配新需求
const agentList = ref([
  {
    id: 'resume-optimizer',
    name: '简历优化智能体',
    icon: Edit,
    description: '上传您的简历，AI将根据目标岗位进行分析，并提供逐条详细的优化建议。',
    color: '#67C23A' // 成功绿
  },
  {
    id: 'resume-evaluator',
    name: '简历评估智能体',
    icon:  DataAnalysis,
    description: '获取对您简历的综合评分和深度分析报告，全面评估内容、格式和关键词匹配度。',
    color: '#409EFF' // 主题蓝
  },
  {
    id: 'career-development',
    name: '岗位发展智能体',
    icon: TrendCharts,
    description: '分析您的技能和目标，推荐匹配岗位，并为您规划清晰的职业发展路线图。',
    color: '#E6A23C' // 警告黄
  }
]);

const startAgent = (agent: any) => {
  // 不再只显示消息，而是直接跳转
  router.push(`/agent/${agent.id}`);
};
</script>

<style scoped>
/* 整体布局 */
.agent-center-container {
  padding: 30px 20px;
  background-color: #f7f8fc;
  min-height: calc(100vh - 60px); /* 视口高度减去导航栏高度 */
}

/* 页面头部 */
.page-header {
  max-width: 1200px;
  margin: 0 auto 40px auto;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.page-title {
  font-size: 2.2rem;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.page-description {
  font-size: 1rem;
  color: #909399;
}

/* 卡片列表 */
.agent-list {
  max-width: 1200px;
  margin: 0 auto;
}

/* 卡片样式 */
.agent-card {
  --card-theme-color: #409EFF; /* CSS变量，用于主题色 */
  
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  height: calc(100% - 24px); /* 减去 margin-bottom 的高度 */
}

.agent-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.1);
}

:deep(.el-card__body) {
  padding: 24px;
  display: flex;
  flex-direction: column;
  flex-grow: 1; /* 让body填满整个卡片 */
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
}

.icon-wrapper {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  margin-right: 16px;
  background-color: var(--card-theme-color);
  flex-shrink: 0; 
}

.agent-name {
  font-size: 1.3rem;
  font-weight: 600;
  color: #303133;
}


.agent-description {
  font-size: 0.95rem;
  color: #606266;
  line-height: 1.7;
  flex-grow: 1; 
}


.card-footer {
  margin-top: 24px; 
  padding-top: 20px;
  border-top: 1px solid #f2f3f5;
}

.start-button {
  width: 100%;
  font-size: 1rem;
  padding: 18px 0;
  border-radius: 8px;
  

  background-color: transparent;
  border-color: var(--card-theme-color);
  color: var(--card-theme-color);
  
  transition: all 0.2s ease-in-out;
}

.start-button:hover,
.start-button:focus {
  
  background-color: var(--card-theme-color);
  border-color: var(--card-theme-color);
  color: #fff;
  transform: scale(1.02);
}
</style>