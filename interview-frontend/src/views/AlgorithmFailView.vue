<template>
  <div class="fail-container">
    <div class="fail-content">
      <!-- 失败图标和标题 -->
      <div class="fail-header">
        <div class="fail-icon">
          <el-icon size="80" color="#F56C6C">
            <CircleClose />
          </el-icon>
        </div>
        <h1 class="fail-title">解答错误</h1>
        <p class="fail-subtitle">请检查您的代码并重新尝试</p>
      </div>
      
      <!-- 错误信息 -->
      <div class="error-section">
        <div class="error-item">
          <div class="error-label">错误类型</div>
          <div class="error-value">{{ errorType }}</div>
        </div>
        <div class="error-item">
          <div class="error-label">通过测试</div>
          <div class="error-value">{{ passedCases }}/{{ totalCases }}</div>
        </div>
        <div class="error-item">
          <div class="error-label">最后执行的输入</div>
          <div class="error-value">{{ lastInput }}</div>
        </div>
      </div>
      
      <!-- 错误详情 -->
      <div v-if="errorMessage" class="error-details">
        <h3>错误详情</h3>
        <pre class="error-message">{{ errorMessage }}</pre>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button size="large" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回修改
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
import { CircleClose, ArrowLeft, DocumentRemove } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();

const errorType = ref(route.query.errorType || '答案错误');
const passedCases = ref(route.query.passedCases || '2');
const totalCases = ref(route.query.totalCases || '3');
const lastInput = ref(route.query.lastInput || '{1,2,3}');
const errorMessage = ref(route.query.errorMessage || '');
const currentQuestionId = ref(parseInt(route.params.id as string) || 1);


const goBack = () => {
  router.push(`/algorithm/${currentQuestionId.value}`);
};

const exitToSummary = () => {

  updateSummaryStats();
  router.push('/algorithm/summary');
};

const updateSummaryStats = () => {

  const stats = JSON.parse(localStorage.getItem('algorithmStats') || '{"attempted": 0, "submitted": 0, "passed": 0}');
  stats.submitted += 1;
  localStorage.setItem('algorithmStats', JSON.stringify(stats));
};

onMounted(() => {
 
});
</script>

<style scoped>
.fail-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff7b7b 0%, #d63384 100%);
  padding: 20px;
}

.fail-content {
  background: white;
  border-radius: 20px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
}

.fail-header {
  margin-bottom: 40px;
}

.fail-icon {
  margin-bottom: 20px;
}

.fail-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.fail-subtitle {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.error-section {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #fef0f0;
  border-radius: 12px;
  border: 1px solid #fbc4c4;
}

.error-item {
  text-align: center;
}

.error-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.error-value {
  font-size: 16px;
  font-weight: 600;
  color: #F56C6C;
}

.error-details {
  margin-bottom: 40px;
  text-align: left;
}

.error-details h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 18px;
}

.error-message {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #e74c3c;
  overflow-x: auto;
  white-space: pre-wrap;
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


@media (max-width: 768px) {
  .fail-content {
    padding: 40px 20px;
  }
  
  .fail-title {
    font-size: 28px;
  }
  
  .error-section {
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