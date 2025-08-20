<template>
  <div class="page-background">
    <div class="resume-assessment-container">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <el-icon><DocumentChecked /></el-icon>
            <span>简历智能评估</span>
          </div>
        </template>

        <div class="form-section">
          <div class="form-grid">
            <!-- 岗位选择 -->
            <div class="form-item">
              <label class="form-label">1. 选择目标岗位</label>
              <el-select
                v-model="positionId"
                placeholder="请选择您要评估的目标岗位"
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

            <!-- 简历文件上传 (升级为拖拽) -->
            <div class="form-item">
              <label class="form-label">2. 上传简历文件</label>
              <el-upload
                ref="uploadRef"
                class="resume-uploader"
                drag
                :action="''" 
                :auto-upload="false"
                :limit="1"
                :on-exceed="handleExceed"
                :on-change="handleFileChange"
                :accept="'.pdf,.doc,.docx,.md,.txt'"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  将文件拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 pdf / doc / docx / md / txt 格式，大小不超过 10MB
                  </div>
                </template>
              </el-upload>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-button">
            <el-button 
              type="primary" 
              size="large" 
              @click="handleAssess" 
              :loading="isLoading" 
              :disabled="!isFormValid"
              round
            >
              {{ isLoading ? '正在智能分析中...' : '开始评估' }}
            </el-button>
          </div>
        </div>

        <!-- 评估报告展示 (带过渡动画) -->
        <transition name="fade-slide">
          <div v-if="assessmentResult || isLoading" class="result-section">
            <el-divider>
              <span class="divider-text">评估报告</span>
            </el-divider>
            <el-card shadow="never" class="result-card">
              <!-- 骨架屏加载状态 -->
              <div v-if="isLoading && !assessmentResult" class="skeleton-loader">
                <div class="skeleton-line title"></div>
                <div class="skeleton-line" style="width: 80%;"></div>
                <div class="skeleton-line" style="width: 90%;"></div>
                <div class="skeleton-line title" style="margin-top: 30px;"></div>
                <div class="skeleton-line" style="width: 70%;"></div>
                <div class="skeleton-line" style="width: 85%;"></div>
              </div>
              <!-- 实际报告内容 -->
              <div v-else-if="assessmentResult" class="markdown-body" v-html="formattedResult"></div>
            </el-card>
          </div>
        </transition>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage, genFileId } from 'element-plus';
import type { UploadInstance, UploadProps, UploadRawFile, UploadFile } from 'element-plus'
import { DocumentChecked, UploadFilled } from '@element-plus/icons-vue';
import api from '@/services/api';
import { marked } from 'marked'; 
import DOMPurify from 'dompurify';

const positionId = ref<string | null>(null);
const isLoading = ref(false);
const assessmentResult = ref('');
const uploadRef = ref<UploadInstance>()
const uploadedFile = ref<UploadFile | null>(null);

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

const isFormValid = computed(() => positionId.value && uploadedFile.value);

const formattedResult = computed(() => {
  if (assessmentResult.value) {
    const rawHtml = marked(assessmentResult.value);
    return DOMPurify.sanitize(rawHtml as string);
  }
  return '';
});

const handleFileChange: UploadProps['onChange'] = (uploadFile) => {
  const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/markdown', 'text/plain'];
  const isTypeValid = allowedTypes.includes(uploadFile.raw?.type || '');
  const isSizeValid = (uploadFile.size || 0) / 1024 / 1024 < 10;

  if (!isTypeValid) {
    ElMessage.error('不支持的文件格式！');
    uploadRef.value?.clearFiles();
    uploadedFile.value = null;
    return;
  }
  if (!isSizeValid) {
    ElMessage.error('文件大小不能超过 10MB！');
    uploadRef.value?.clearFiles();
    uploadedFile.value = null;
    return;
  }
  uploadedFile.value = uploadFile;
};

const handleExceed: UploadProps['onExceed'] = (files) => {
  uploadRef.value!.clearFiles();
  const file = files[0] as UploadRawFile;
  file.uid = genFileId();
  uploadRef.value!.handleStart(file);
};

const handleAssess = async () => {
  if (!isFormValid.value || !uploadedFile.value?.raw) {
    ElMessage.warning('请选择岗位并上传简历。');
    return;
  }
  isLoading.value = true;
  assessmentResult.value = '';
  const formData = new FormData();
  formData.append('positionId', positionId.value!);
  formData.append('resumeFile', uploadedFile.value.raw);
  try {
    const response = await api.post('/resume/assess-file', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    });
    assessmentResult.value = response.data.assessment;
    ElMessage.success('简历评估完成！');
  } catch (error) {
    console.error("简历评估失败:", error);
    ElMessage.error('评估请求失败，请稍后重试。');
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.page-background {
  background-color: #f7f8fc;
  padding: 40px 24px;
  min-height: calc(100vh - 50px); /* 假设顶部导航栏高50px */
}

.resume-assessment-container {
  max-width: 900px;
  margin: 0 auto;
}

.box-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px exertions(0, 0, 0, 0.08);
  overflow: visible; /* 允许阴影显示 */
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
  gap: 32px;
}

.form-item .form-label {
  display: block;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
  font-size: 1rem;
  font-weight: 500;
}

.resume-uploader :deep(.el-upload-dragger) {
  padding: 30px;
  border-radius: 8px;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.resume-uploader :deep(.el-upload-dragger:hover) {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.1);
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

/* 骨架屏样式 */
.skeleton-loader {
  padding: 20px 25px;
}
.skeleton-line {
  height: 18px;
  margin-bottom: 15px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f2f5 25%, #e6e8eb 37%, #f0f2f5 63%);
  background-size: 400% 100%;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}
.skeleton-line.title {
  height: 24px;
  width: 40%;
  margin-bottom: 25px;
}
@keyframes skeleton-pulse {
  0% { background-position: 100% 50%; }
  100% { background-position: 0 50%; }
}


/* 报告内容样式 */
.markdown-body {
  padding: 15px 30px;
  line-height: 1.9;
  color: #333;
  font-size: 16px;
}

:deep(.markdown-body h3) {
  font-size: 1.3em;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 2px solid var(--el-color-primary-light-7);
  padding-bottom: 0.5em;
  margin-top: 32px;
  margin-bottom: 20px;
}
:deep(.markdown-body ul) {
  padding-left: 25px;
  list-style-type: disc;
}
:deep(.markdown-body li) {
  margin-bottom: 12px;
}
:deep(.markdown-body strong) {
  color: var(--el-color-primary);
  font-weight: 600;
}

/* 过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.5s ease-out;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  .page-background {
    padding: 20px 12px;
  }
}
</style>