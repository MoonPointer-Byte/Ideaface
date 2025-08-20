<template>
  <div class="pass-container">
    <div class="pass-content">
      <!-- 通过图标和标题 -->
      <div class="pass-header">
        <div class="success-icon">
          <el-icon size="80" color="#67C23A">
            <CircleCheck />
          </el-icon>
        </div>
        <h1 class="pass-title">恭喜通过！</h1>
        <p class="pass-subtitle">您已成功解决了这道算法题目</p>
      </div>
      
      <!-- 统计信息 -->
      <div class="stats-section">
        <div class="stat-item">
          <div class="stat-label">执行用时</div>
          <div class="stat-value">{{ executionTime }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">内存消耗</div>
          <div class="stat-value">{{ memoryUsage }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">击败</div>
          <div class="stat-value">{{ beatPercentage }}%</div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button size="large" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-button type="primary" size="large" @click="nextQuestion">
          下一题
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button size="large" @click="exitToSummary">
          <el-icon><DocumentRemove /></el-icon>
          退出
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { CircleCheck, ArrowLeft, ArrowRight, DocumentRemove } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();


const executionTime = ref(route.query.executionTime || '120ms');
const memoryUsage = ref(route.query.memoryUsage || '15.2MB');
const beatPercentage = ref(route.query.beatPercentage || '85.6');
const currentQuestionId = ref(parseInt(route.params.id as string) || 1);


const goBack = () => {
  router.push(`/algorithm/${currentQuestionId.value}`);
};

const nextQuestion = () => {
  const nextId = currentQuestionId.value + 1;
  router.push(`/algorithm/${nextId}`);
};

const exitToSummary = () => {

  updateSummaryStats();
  router.push('/algorithm/summary');
};

const updateSummaryStats = () => {

  const stats = JSON.parse(localStorage.getItem('algorithmStats') || '{"attempted": 0, "submitted": 0, "passed": 0}');
  stats.submitted += 1;
  stats.passed += 1;
  localStorage.setItem('algorithmStats', JSON.stringify(stats));
};

onMounted(() => {
});
</script>

<style scoped>
.pass-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.pass-content {
  background: white;
  border-radius: 20px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.pass-header {
  margin-bottom: 40px;
}

.success-icon {
  margin-bottom: 20px;
}

.pass-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.pass-subtitle {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.stats-section {
  display: flex;
  justify-content: space-around;
  margin-bottom: 40px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 12px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  min-width: 120px;
  border-radius: 25px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pass-content {
    padding: 40px 20px;
  }
  
  .pass-title {
    font-size: 28px;
  }
  
  .stats-section {
    flex-direction: column;
    gap: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .action-buttons .el-button {
    width: 100%;
    max-width: 200px;
  }
}
</style> 