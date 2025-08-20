<template>
  <div class="summary-container">
    <div class="summary-content">
      <!-- 总结标题 -->
      <div class="summary-header">
        <div class="summary-icon">
          <el-icon size="60" color="#409EFF">
            <DataAnalysis />
          </el-icon>
        </div>
        <h1 class="summary-title">练习总结</h1>
        <p class="summary-subtitle">本次算法练习的统计信息</p>
      </div>
      
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon attempted">
            <el-icon size="30"><EditPen /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.attempted }}</div>
            <div class="stat-label">尝试题目</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon submitted">
            <el-icon size="30"><Upload /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.submitted }}</div>
            <div class="stat-label">提交次数</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon passed">
            <el-icon size="30"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.passed }}</div>
            <div class="stat-label">通过题目</div>
          </div>
        </div>
      </div>
      
      <!-- 成功率展示 -->
      <div class="progress-section">
        <h3>通过率</h3>
        <el-progress 
          :percentage="successRate" 
          :stroke-width="20"
          :text-inside="true"
          :color="progressColor"
        />
        <p class="progress-text">
          <span v-if="successRate >= 80" class="excellent">优秀！继续保持！</span>
          <span v-else-if="successRate >= 60" class="good">表现不错，继续努力！</span>
          <span v-else class="needs-improvement">还有提升空间，加油！</span>
        </p>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-section">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { 
  DataAnalysis, 
  EditPen, 
  Upload, 
  CircleCheck, 
  HomeFilled 
} from '@element-plus/icons-vue';

const router = useRouter();

// 统计数据
const stats = ref({
  attempted: 0,
  submitted: 0,
  passed: 0
});

// 计算成功率
const successRate = computed(() => {
  if (stats.value.submitted === 0) return 0;
  return Math.round((stats.value.passed / stats.value.submitted) * 100);
});

// 进度条颜色
const progressColor = computed(() => {
  const rate = successRate.value;
  if (rate >= 80) return '#67C23A';
  if (rate >= 60) return '#E6A23C';
  return '#F56C6C';
});

// 操作函数
const goHome = () => {
  // 清除统计数据
  localStorage.removeItem('algorithmStats');
  router.push('/');
};

// 加载统计数据
const loadStats = () => {
  const savedStats = localStorage.getItem('algorithmStats');
  if (savedStats) {
    stats.value = JSON.parse(savedStats);
  }
};

onMounted(() => {
  loadStats();
});
</script>

<style scoped>
.summary-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  padding: 20px;
}

.summary-content {
  background: white;
  border-radius: 20px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
}

.summary-header {
  margin-bottom: 40px;
}

.summary-icon {
  margin-bottom: 20px;
}

.summary-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.summary-subtitle {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: #f8f9fa;
  border-radius: 15px;
  padding: 25px 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.attempted {
  background: linear-gradient(45deg, #74b9ff, #0984e3);
}

.stat-icon.submitted {
  background: linear-gradient(45deg, #fdcb6e, #e84393);
}

.stat-icon.passed {
  background: linear-gradient(45deg, #00b894, #55a3ff);
}

.stat-info {
  text-align: center;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.progress-section {
  margin-bottom: 40px;
  text-align: center;
}

.progress-section h3 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 20px;
}

.progress-text {
  margin-top: 15px;
  font-size: 16px;
  font-weight: 500;
}

.excellent {
  color: #67C23A;
}

.good {
  color: #E6A23C;
}

.needs-improvement {
  color: #F56C6C;
}

.action-section {
  text-align: center;
}

.action-section .el-button {
  min-width: 160px;
  border-radius: 25px;
  font-weight: 500;
  font-size: 16px;
  padding: 15px 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .summary-content {
    padding: 40px 20px;
  }
  
  .summary-title {
    font-size: 28px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .stat-card {
    flex-direction: row;
    justify-content: flex-start;
    text-align: left;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
  }
  
  .stat-number {
    font-size: 24px;
  }
}
</style> 