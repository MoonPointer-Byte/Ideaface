<template>
  <div class="page-background">
    <div class="career-planner-container">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <el-icon><Compass /></el-icon>
            <span>岗位发展路径智能规划</span>
          </div>
        </template>

        <div class="form-section">
          <div class="form-grid">
            <!-- 当前状态 -->
            <div class="form-item">
              <label class="form-label">1. 我的当前状态</label>
              <el-input
                v-model="currentSkills"
                :rows="10"
                type="textarea"
                placeholder="请简述您当前具备的核心技能、项目经验、技术栈等。您可以从业余项目、工作经历、或学习课程中总结..."
                show-word-limit
                maxlength="2000"
              />
            </div>

            <!-- 目标岗位 -->
            <div class="form-item">
              <label class="form-label">2. 我的发展目标</label>
              <el-select
                v-model="targetPositionId"
                placeholder="请选择您的目标岗位"
                size="large"
                style="width: 100%;"
              >
                <el-option
                  v-for="item in positions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-button">
            <el-button 
              type="primary" 
              size="large" 
              @click="handleGeneratePlan" 
              :loading="isLoading" 
              :disabled="!isFormValid"
              round
            >
              <el-icon style="margin-right: 8px;"><MagicStick /></el-icon>
              {{ isLoading ? '正在规划您的未来...' : '生成我的发展路径' }}
            </el-button>
          </div>
        </div>

        <!-- 发展路径报告展示 -->
        <transition name="fade-slide">
          <div v-if="careerPlan || isLoading" class="result-section">
            <el-divider>
              <span class="divider-text">AI生成的职业发展规划</span>
            </el-divider>
            <el-card shadow="never" class="result-card">
              <!-- 骨架屏加载状态 -->
              <div v-if="isLoading && !careerPlan" class="skeleton-loader">
                <div class="skeleton-line title"></div>
                <div class="skeleton-line" style="width: 80%;"></div>
                <div class="skeleton-line title" style="margin-top: 30px;"></div>
                <div class="skeleton-line" style="width: 70%;"></div>
                <div class="skeleton-line" style="width: 85%;"></div>
                <div class="skeleton-line" style="width: 75%;"></div>
                 <div class="skeleton-line title" style="margin-top: 30px;"></div>
                <div class="skeleton-line" style="width: 90%;"></div>
              </div>
              <!-- 实际报告内容 -->
              <div v-else-if="careerPlan" class="markdown-body" v-html="formattedPlan"></div>
            </el-card>
          </div>
        </transition>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Compass, MagicStick } from '@element-plus/icons-vue';
import api from '@/services/api'; // 假设这是您封装的axios实例
import { marked } from 'marked'; 
import DOMPurify from 'dompurify';
import axios from 'axios'; // 引入axios以获取AxiosError类型

// --- State ---
const currentSkills = ref('');
const targetPositionId = ref<string | null>(null);
const isLoading = ref(false);
const careerPlan = ref('');

// --- Static Data (Reused) ---
const positions = [
  { value: 'ai-engineer', label: '人工智能工程师' },
  { value: 'backend-developer', label: '后端开发工程师' },
  { value: 'frontend-developer', label: '前端开发工程师' },
  { value: 'product-manager', label: '产品经理' },
  { value: 'algorithm-engineer', label: '算法工程师' },
  { value: 'qa-engineer', label: '测试工程师' },
  { value: 'big-data-engineer', label: '大数据开发工程师' },
  { value: 'devops-engineer', label: '运维工程师' },
];

// --- Computed Properties ---
const isFormValid = computed(() => {
  return targetPositionId.value && currentSkills.value.trim().length > 20;
});

const formattedPlan = computed(() => {
  if (careerPlan.value) {
    const rawHtml = marked(careerPlan.value);
    return DOMPurify.sanitize(rawHtml as string);
  }
  return '';
});

// --- Methods ---
const handleGeneratePlan = async () => {
  if (!isFormValid.value) {
    ElMessage.warning('请选择目标岗位并描述您当前的技能情况。');
    return;
  }
  isLoading.value = true;
  careerPlan.value = '';

  try {
    const response = await api.post('/career/plan', {
      currentSkills: currentSkills.value,
      targetPositionId: targetPositionId.value,
    }, {
      timeout: 120000 
    });
    
   
    if (response.data && response.data.plan) {
      careerPlan.value = response.data.plan;
      ElMessage.success('您的专属发展路径已生成！');
    } else {
     
      throw new Error("后端返回的数据格式不正确。");
    }
  } catch (error: unknown) {
    console.error("生成发展路径失败:", error);

    let errorMessage = '请求失败，请稍后重试。'; 

 
    if (axios.isAxiosError(error)) {
        
        if (error.response) {
           
            const serverMessage = error.response.data?.message || error.response.data?.error;
            if (serverMessage) {
                errorMessage = `请求失败: ${serverMessage}`;
            } else {
                errorMessage = `服务器错误，状态码: ${error.response.status}`;
            }
        } else if (error.code === 'ECONNABORTED') {
         
            errorMessage = '请求超时，AI正在深度思考，请稍后再试。';
        } else if (error.request) {
          
            errorMessage = '网络连接错误，请检查您的网络。';
        }
    } else if (error instanceof Error) {
        
        errorMessage = error.message;
    }

    ElMessage.error(errorMessage);
    
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>

.page-background {
  background-color: #f7f8fc;
  padding: 40px 24px;
  min-height: calc(100vh - 50px);
}
.career-planner-container {
  max-width: 1200px;
  margin: 0 auto;
}
.box-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  padding: 16px;
}
.card-header {
  display: flex;
  align-items: center;
  font-size: 1.3rem;
  font-weight: 600;
  gap: 10px;
  color: #303133;
}
.form-section {
  display: flex;
  flex-direction: column;
  gap: 32px;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 40px;
}
.form-item .form-label {
  display: block;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
  font-size: 1rem;
  font-weight: 500;
}
.action-button {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
.action-button .el-button {
  padding: 0 40px;
  height: 48px;
  font-size: 1.1rem;
  transition: all 0.3s ease;
}
.action-button .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}
.result-section {
  margin-top: 48px;
}
.divider-text {
  font-size: 1.2rem;
  color: var(--el-text-color-secondary);
  font-weight: 300;
}
.result-card {
  border: 1px solid var(--el-border-color-light);
  background-color: #fafcfe;
}
.skeleton-loader { padding: 20px 25px; }
.skeleton-line { height: 18px; margin-bottom: 15px; border-radius: 4px; background: linear-gradient(90deg, #f0f2f5 25%, #e6e8eb 37%, #f0f2f5 63%); background-size: 400% 100%; animation: skeleton-pulse 1.5s ease-in-out infinite; }
.skeleton-line.title { height: 24px; width: 40%; margin-bottom: 25px; }
@keyframes skeleton-pulse { 0% { background-position: 100% 50%; } 100% { background-position: 0 50%; } }
.markdown-body { padding: 15px 30px; line-height: 1.9; color: #333; font-size: 16px; }
:deep(.markdown-body h3) { font-size: 1.3em; font-weight: 600; color: #2c3e50; border-bottom: 2px solid var(--el-color-primary-light-7); padding-bottom: 0.5em; margin-top: 32px; margin-bottom: 20px; }
:deep(.markdown-body h4) { font-size: 1.1em; font-weight: 600; color: #34495e; margin-top: 24px; margin-bottom: 12px; }
:deep(.markdown-body ul) { padding-left: 25px; list-style-type: disc; }
:deep(.markdown-body li) { margin-bottom: 12px; }
:deep(.markdown-body strong) { color: var(--el-color-primary); font-weight: 600; }
:deep(.markdown-body a) { color: var(--el-color-primary); text-decoration: none; border-bottom: 1px solid var(--el-color-primary-light-5); transition: all 0.2s; }
:deep(.markdown-body a:hover) { background-color: var(--el-color-primary-light-9); }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.5s ease-out; }
.fade-slide-enter-from, .fade-slide-leave-to { opacity: 0; transform: translateY(20px); }
@media (max-width: 768px) { .form-grid { grid-template-columns: 1fr; } .page-background { padding: 20px 12px; } }
</style>