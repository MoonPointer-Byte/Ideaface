<template>
  <div class="interview-page-bg">
    <el-row :gutter="24" justify="center" class="interview-container">
      <!-- 左侧栏: 仪表盘 -->
      <el-col :xs="24" :md="8" class="panel">
        <div class="side-dashboard">
          <!-- 用户影像卡片 -->
          <div class="glass-card self-view-card">
            <div class="card-header">
              <span><el-icon><User /></el-icon> 我的影像</span>
              <el-tag :type="faceStatus.type" size="small" effect="dark" round>
                <el-icon class="is-loading" style="vertical-align: middle;" v-if="faceStatus.type === 'info'"><Loading /></el-icon>
                {{ faceStatus.text }}
              </el-tag>
            </div>
            <div class="user-video-container">
              <video ref="userVideoPlayer" class="user-video" autoplay muted playsinline></video>
              <div class="face-scan-anim" :class="{ 'no-face': !faceDetected }">
                <div class="scan-corner top-left"></div>
                <div class="scan-corner top-right"></div>
                <div class="scan-corner bottom-left"></div>
                <div class="scan-corner bottom-right"></div>
                <div class="scan-line" v-if="faceDetected"></div>
              </div>
            </div>
             <!-- 【关键修改】: 新增一个卡片来显示扩展的分析信息 -->
            <div class="behavior-analysis-card">
              <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label-class-name="behavior-label" label="年龄/性别">{{ ageGenderText }}</el-descriptions-item>
                <el-descriptions-item label-class-name="behavior-label" label="视线方向">{{ gazeDirectionText }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
          <!-- 状态与操作卡片 -->
          <div class="glass-card status-card">
            <div class="card-header"><span><el-icon><Compass /></el-icon> 面试仪表盘</span></div>
            <el-descriptions :column="1" border>
              <el-descriptions-item label-class-name="status-label" label="面试岗位">{{ positionTitle }}</el-descriptions-item>
              <el-descriptions-item label-class-name="status-label" label="当前状态">
                <el-tag :type="statusTagType" effect="dark" size="small">{{ interviewStatusText }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label-class-name="status-label" label="面试进度">
                <el-progress :percentage="progressPercentage" :stroke-width="16" striped striped-flow />
              </el-descriptions-item>
            </el-descriptions>
            <el-divider content-position="left" style="margin: 20px 0;"><el-icon><FolderChecked /></el-icon> 您的简历</el-divider>
            <div class="resume-upload-section">
              <div v-if="isResumeUploaded" class="upload-success-tip">
                <el-icon color="#67C23A" :size="24"><CircleCheckFilled /></el-icon>
                <div class="success-text">
                  <span>{{ uploadedFileName }}</span>
                  <small>简历已上传，AI将结合您的经历提问。</small>
                </div>
              </div>
              <div v-else>
                <el-upload
                  ref="uploadRef" drag action="#" :auto-upload="false" :show-file-list="false"
                  :on-change="handleFileChange" :limit="1" :on-exceed="handleExceed" class="resume-uploader"
                >
                  <div v-if="selectedFile" class="file-preview-wrapper" @click.stop>
                    <el-icon class="file-icon" :size="48"><component :is="fileIconComponent" /></el-icon>
                    <div class="file-details">
                      <span class="file-name" :title="selectedFile.name">{{ selectedFile.name }}</span>
                      <span class="file-size">{{ selectedFile.size ? (selectedFile.size / 1024 / 1024).toFixed(2) : '0.00' }} MB</span>
                    </div>
                    <div class="file-actions">
                      <el-button type="danger" :icon="Delete" circle plain @click="handleFileRemove" :disabled="isUploading" />
                    </div>
                    <el-progress v-if="isUploading" :percentage="uploadProgress" :stroke-width="4" striped striped-flow :duration="10" class="upload-progress-bar" />
                  </div>
                  <div v-else class="upload-placeholder">
                    <el-icon :size="40" class="upload-icon"><UploadFilled /></el-icon>
                    <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                  </div>
                </el-upload>
                <el-button v-if="selectedFile" type="success" @click="submitResumeAndStart" :disabled="isUploading" :loading="isUploading" class="submit-resume-btn">
                  {{ isUploading ? '正在分析...' : '上传简历，开启面试' }}
                </el-button>
              </div>
            </div>
            <el-divider content-position="left" style="margin: 20px 0;"><el-icon><Bell /></el-icon> 面试注意事项</el-divider>
            <div class="interview-tips-section">
              <dl class="tips-list">
                <dt>环境与设备</dt>
                <dd>确保网络通畅，选择光线充足、背景整洁的安静环境。</dd>
                <dt>沟通与表达</dt>
                <dd>请正视摄像头，保持声音清晰、语速适中，自信交流。</dd>
                <dt>着装与态度</dt>
                <dd>请穿着得体、整洁，展现出专业的精神面貌。</dd>
              </dl>
            </div>
          </div>
        </div>
      </el-col>
      <!-- 右侧栏 -->
      <el-col :xs="24" :md="16" class="panel">
        <div class="glass-card main-cockpit">
          <div class="interviewer-zone">
            <div class="question-display">
              <el-tabs v-model="activeTab" class="question-tabs" @tab-click="onTabClick">
                <el-tab-pane name="question">
                  <template #label><el-icon><ChatDotRound /></el-icon> 当前问题</template>
                  <div class="question-content" v-loading="isLoading" :element-loading-text="loadingText">
                    <p>{{ currentQuestion }}</p>
                  </div>
                </el-tab-pane>
                <el-tab-pane name="feedback" :disabled="!lastEvaluation">
                  <template #label>
                    <span><el-icon><DataAnalysis /></el-icon> AI 简评<el-badge is-dot class="feedback-dot" :hidden="!newFeedback" /></span>
                  </template>
                  <div class="feedback-content">
                    <el-alert type="info" :closable="false" show-icon><p>{{ lastEvaluation }}</p></el-alert>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
            <div class="avatar-container"><VirtualAvatar /></div>
          </div>
          <div class="action-zone" v-if="isInterviewing || (interviewStatus === 'pending' && !isResumeUploaded)">
            <el-input v-model="userAnswerText" type="textarea" :placeholder="isInterviewing ? '请在这里输入您的回答...' : '请先上传简历以开始面试'"
              maxlength="2000" show-word-limit resize="none" :disabled="isLoading || !isInterviewing" class="answer-textarea" />
            <div class="controls-footer">
              <div class="voice-controls">
                <el-button @click="toggleVoiceInput" :type="isVoiceRecording ? 'danger' : 'primary'" plain round :disabled="!isInterviewing">
                  <el-icon style="margin-right: 8px;"><component :is="isVoiceRecording ? VideoPause : Microphone" /></el-icon>
                  {{ isVoiceRecording ? '停止语音' : '开始语音' }}
                </el-button>
                <div class="voice-wave-container" :class="{ 'is-active': isVoiceRecording }">
                  <span v-for="i in 5" :key="i" class="wave-bar"></span>
                </div>
              </div>
              <el-button type="primary" @click="submitFinalAnswer" :disabled="isLoading || userAnswerText.trim() === '' || !isInterviewing" round class="submit-final-btn" size="large">
                <el-icon><Promotion /></el-icon> 提交最终回答
              </el-button>
            </div>
          </div>
          <div v-else-if="interviewStatus === 'finished'" class="finished-box">
            <el-result icon="success" title="面试已完成！" sub-title="您的专属评估报告已生成，祝您求职顺利！">
              <template #extra><el-button type="primary" size="large" @click="goToReport">查看我的面试报告</el-button></template>
            </el-result>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, watch } from 'vue';
import { ElMessage, type UploadProps, type UploadFile, type UploadRawFile, type UploadInstance, type TabsPaneContext } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import api from '@/services/api';
import VirtualAvatar from '@/components/VirtualAvatar.vue';
import * as faceapi from 'face-api.js';
import {
  User, Compass, ChatDotRound, DataAnalysis, Promotion, Loading, Microphone,
  VideoPause, Document, UploadFilled, Delete, FolderChecked, Tickets, Bell, CircleCheckFilled
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();


const faceApiModelsLoaded = ref(false);
const faceAnalysisInterval = ref<number | null>(null);
const faceDetected = ref(false);
const faceStatus = ref<{ text: string; type: 'info' | 'success' | 'warning' | 'primary' }>({ text: '加载模型...', type: 'info' });
const emotionHistory = ref<faceapi.FaceExpressions[]>([]);
const ageGenderHistory = ref<{ age: number; gender: string; }[]>([]);
const gazeHistory = ref<{ direction: string; tilt: number; }[]>([]);
let recognition: any = null;
type InterviewStatus = 'pending' | 'ongoing' | 'finished' | 'error';
const interviewStatus = ref<InterviewStatus>('pending');
const isLoading = ref<boolean>(false);
const loadingText = ref('AI正在分析...');
const sessionId = ref<string>('');
const currentQuestion = ref<string>('请上传您的简历以开启AI面试');
const currentQuestionIndex = ref<number>(0);
const totalQuestions = ref<number>(8);
const userAnswerText = ref<string>('');
const lastEvaluation = ref<string>('');
const activeTab = ref('question');
const newFeedback = ref(false);
const userVideoPlayer = ref<HTMLVideoElement | null>(null);
let mediaStream: MediaStream | null = null;
const isVoiceRecording = ref(false);
const uploadRef = ref<UploadInstance>();
const selectedFile = ref<UploadFile | null>(null);
const isUploading = ref(false);
const uploadProgress = ref(0);
let progressInterval: number | null = null;
const isResumeUploaded = ref(false);
const uploadedFileName = ref('');


const ageGenderText = computed(() => {
    if (ageGenderHistory.value.length === 0) return '分析中...';
    const lastReading = ageGenderHistory.value[ageGenderHistory.value.length - 1];
    const genderText = lastReading.gender === 'male' ? '男' : '女';
    return `${Math.round(lastReading.age)}岁 / ${genderText}`;
});
const gazeDirectionText = computed(() => {
    if (gazeHistory.value.length === 0) return '分析中...';
    return gazeHistory.value[gazeHistory.value.length - 1].direction;
});
const positionTitle = computed(() => {
    const positionMap: { [key: string]: string } = { 'ai-engineer': '人工智能工程师', 'backend-developer': '后端开发工程师', 'product-manager': '产品经理' };
    return positionMap[route.params.positionId as string] || '模拟岗位';
});
const isInterviewing = computed(() => interviewStatus.value === 'ongoing');
const progressPercentage = computed(() => {
  if (interviewStatus.value === 'finished') return 100;
  if (totalQuestions.value === 0) return 0;
  return Math.round((currentQuestionIndex.value / totalQuestions.value) * 100);
});
const interviewStatusText = computed(() => {
  if (isLoading.value) return loadingText.value;
  const statusMap: Record<InterviewStatus, string> = {
    pending: isResumeUploaded.value ? '待开始' : '待上传简历',
    ongoing: `第 ${currentQuestionIndex.value + 1} / ${totalQuestions.value} 题`,
    finished: '已完成',
    error: '出现错误'
  };
  return statusMap[interviewStatus.value];
});
const statusTagType = computed(() => ({ pending: 'info' as const, ongoing: 'primary' as const, finished: 'success' as const, error: 'danger' as const }[interviewStatus.value]));
const fileIconComponent = computed(() => {
  if (!selectedFile.value || !selectedFile.value.name) return Tickets;
  const extension = selectedFile.value.name.split('.').pop()?.toLowerCase();
  if (extension === 'pdf') return Tickets;
  if (extension === 'docx' || extension === 'doc') return Document;
  return Document;
});

onMounted(async () => {
  sessionId.value = `${route.params.positionId || 'mock'}_${Date.now()}`;
  setupSpeechRecognition();
  setupFaceAPI();
  await setupMedia();
});

onUnmounted(() => {
  if (mediaStream) mediaStream.getTracks().forEach(track => track.stop());
  if (recognition) recognition.stop();
  if(progressInterval) clearInterval(progressInterval);
  if (faceAnalysisInterval.value) clearInterval(faceAnalysisInterval.value);
});


async function startInterview() {
  isLoading.value = true;
  loadingText.value = 'AI正在为您生成专属面试题...';
  clearAllHistories();
  try {
    const response = await api.post('/interactive-interview/start', { sessionId: sessionId.value, positionId: route.params.positionId as string });
    if (response.data && response.data.question) {
      currentQuestion.value = response.data.question;
      currentQuestionIndex.value = 0;
      interviewStatus.value = 'ongoing';
    } else { throw new Error('后端返回数据格式不正确'); }
  } catch (error: any) {
    console.error("开始面试失败:", error);
    const backendError = error.response?.data?.error || "获取面试题失败，请检查网络。";
    currentQuestion.value = backendError;
    interviewStatus.value = 'error';
    ElMessage.error(backendError);
  } finally {
    isLoading.value = false;
  }
}

const submitResumeAndStart = async () => {
  if (!selectedFile.value) return ElMessage.warning('请先选择要上传的文件。');
  isUploading.value = true;
  isLoading.value = true;
  loadingText.value = '正在上传和分析简历...';
  simulateProgress();
  const formData = new FormData();
  formData.append('file', selectedFile.value.raw as Blob);
  formData.append('sessionId', sessionId.value);
  formData.append('positionId', route.params.positionId as string);
  try {
    await api.post('/interactive-interview/upload-resume', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
    if (progressInterval) clearInterval(progressInterval);
    uploadProgress.value = 100;
    ElMessage.success('简历解析成功！');
    isResumeUploaded.value = true;
    uploadedFileName.value = selectedFile.value.name;
    selectedFile.value = null;
    await startInterview();
  } catch (error: any) {
    if (progressInterval) clearInterval(progressInterval);
    uploadProgress.value = 0;
    console.error('上传简历或开始面试失败:', error);
    const backendError = error.response?.data?.error || "处理失败，请刷新页面后重试。";
    ElMessage.error(backendError);
    interviewStatus.value = 'error';
  } finally {
    isUploading.value = false;
  }
};


async function submitFinalAnswer() {
  if (userAnswerText.value.trim() === '') return ElMessage.warning("回答不能为空！");
  isLoading.value = true;
  loadingText.value = 'AI正在分析您的回答...';
  if (isVoiceRecording.value) toggleVoiceInput();

  lastEvaluation.value = '';
  newFeedback.value = false;

  const emotionData = calculateEmotionSummary(emotionHistory.value);
  const behavioralData = calculateBehavioralSummary(ageGenderHistory.value, gazeHistory.value);

  try {
    const response = await api.post('/interactive-interview/answer', {
      sessionId: sessionId.value,
      answerText: userAnswerText.value,
      emotionData: emotionData,
      behavioralData: behavioralData
    });
    lastEvaluation.value = response.data.evaluation || 'AI没有提供评价。';
    newFeedback.value = true;
    activeTab.value = 'feedback';
    userAnswerText.value = '';
    clearAllHistories();

    if (response.data.question) {

      currentQuestion.value = response.data.question;
      currentQuestionIndex.value++;
    } else {
  
      currentQuestionIndex.value = totalQuestions.value;
      interviewStatus.value = 'finished';
      currentQuestion.value = "所有问题已回答完毕！";
      ElMessage.success({ message: '所有问题已回答完毕！报告即将生成。', duration: 5000 });
    }
  } catch (error: any) {
    console.error("提交回答失败:", error);
    const backendError = error.response?.data?.error || "提交回答失败，请稍后重试。";
    ElMessage.error(backendError);
    interviewStatus.value = 'error';
  } finally {
    isLoading.value = false;
  }
}


async function endInterviewAndGoToReport() {
    isLoading.value = true;
    loadingText.value = '正在为您生成最终报告，请稍候...';
    try {
        // 调用我们新的后端接口来触发异步报告生成
        await api.post(`/interviews/${sessionId.value}/end`);
        ElMessage.success("报告生成任务已启动！");
        // 直接导航到报告页，报告页将负责轮询获取结果
        router.push({ name: 'report', params: { sessionId: sessionId.value }});
    } catch (error: any) {
        console.error("结束面试并生成报告失败:", error);
        const backendError = error.response?.data?.error || "生成报告请求失败，请稍后重试。";
        ElMessage.error(backendError);
    } finally {
        isLoading.value = false;
    }
}


function goToReport() {
    // 无论何时点击，都调用新的结束流程
    endInterviewAndGoToReport();
}

async function setupFaceAPI() {
  try {
    faceStatus.value = { text: '加载识别模型...', type: 'info' };
    await faceapi.nets.tinyFaceDetector.loadFromUri('/models');
    await faceapi.nets.faceLandmark68Net.loadFromUri('/models');
    await faceapi.nets.faceExpressionNet.loadFromUri('/models');
    await faceapi.nets.ageGenderNet.loadFromUri('/models');
    faceApiModelsLoaded.value = true;
    faceStatus.value = { text: '准备就绪', type: 'primary' };
  } catch (error) {
    console.error('FaceAPI 模型加载失败:', error);
    faceStatus.value = { text: '模型加载失败', type: 'warning' };
    ElMessage.error('人脸识别模型加载失败，请检查网络或刷新页面。');
  }
}
function startFaceAnalysis() {
  if (!userVideoPlayer.value || !faceApiModelsLoaded.value) return;
  faceAnalysisInterval.value = window.setInterval(async () => {
    if (!userVideoPlayer.value || userVideoPlayer.value.paused || userVideoPlayer.value.ended) return;
    const detections = await faceapi.detectSingleFace(userVideoPlayer.value, new faceapi.TinyFaceDetectorOptions())
                                  .withFaceLandmarks()
                                  .withFaceExpressions()
                                  .withAgeAndGender();
    if (detections) {
      faceDetected.value = true;
      emotionHistory.value.push(detections.expressions);
      const primaryEmotion = Object.keys(detections.expressions).reduce((a, b) => (detections.expressions as any)[a] > (detections.expressions as any)[b] ? a : b);
      const emotionMap: { [key: string]: string } = { neutral: '中性', happy: '开心', sad: '悲伤', angry: '生气', fearful: '害怕', disgusted: '厌恶', surprised: '惊讶' };
      faceStatus.value = { text: `情绪: ${emotionMap[primaryEmotion] || '分析中'}`, type: 'success' };
      ageGenderHistory.value.push({ age: detections.age, gender: detections.gender });
      const landmarks = detections.landmarks;
      const nose = landmarks.getNose()[3];
      const jaw = landmarks.getJawOutline()[8];
      const leftCheek = landmarks.getJawOutline()[0];
      const rightCheek = landmarks.getJawOutline()[16];
      const horizontalCenter = (leftCheek.x + rightCheek.x) / 2;
      const horizontalOffset = nose.x - horizontalCenter;
      let direction = "正视前方";
      if (horizontalOffset > 15) direction = "看向右侧";
      else if (horizontalOffset < -15) direction = "看向左侧";
      else if (nose.y > jaw.y - 65) direction = "低头向下";
      else if (nose.y < jaw.y - 85) direction = "抬头向上";
      const tilt = Math.atan2(rightCheek.y - leftCheek.y, rightCheek.x - leftCheek.x) * (180 / Math.PI);
      gazeHistory.value.push({ direction, tilt });
    } else {
      faceDetected.value = false;
      faceStatus.value = { text: '未检测到人脸', type: 'warning' };
    }
  }, 2500);
}
function calculateEmotionSummary(history: faceapi.FaceExpressions[]): { [key: string]: number } {
  const summary = { neutral: 0, happy: 0, sad: 0, angry: 0, fearful: 0, disgusted: 0, surprised: 0 };
  if (history.length === 0) return summary;
  for (const expressions of history) {
    for (const key in summary) { (summary as any)[key] += (expressions as any)[key]; }
  }
  for (const key in summary) { (summary as any)[key] /= history.length; }
  return summary;
}
function calculateBehavioralSummary(ageHistory: any[], gazeHistory: any[]) {
    if (ageHistory.length === 0 || gazeHistory.length === 0) return null;
    const lastAgeGender = ageHistory[ageHistory.length - 1];
    const gazeDistribution = gazeHistory.reduce((acc, curr) => {
        acc[curr.direction] = (acc[curr.direction] || 0) + 1;
        return acc;
    }, {} as Record<string, number>);
    const primaryGaze = Object.keys(gazeDistribution).reduce((a, b) => gazeDistribution[a] > gazeDistribution[b] ? a : b, "分析中");
    const averageTilt = gazeHistory.reduce((acc, curr) => acc + curr.tilt, 0) / gazeHistory.length;
    return {
        estimatedAge: Math.round(lastAgeGender.age).toString(),
        estimatedGender: lastAgeGender.gender,
        gazeDirection: primaryGaze,
        headTilt: averageTilt
    };
}
function setupSpeechRecognition() {
  const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
  if (SpeechRecognition) {
    recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.lang = 'zh-CN';
    recognition.interimResults = true;
    recognition.onresult = (event: any) => {
      let finalTranscript = '';
      for (let i = event.resultIndex; i < event.results.length; ++i) {
        if (event.results[i].isFinal) finalTranscript += event.results[i][0].transcript;
      }
      if (finalTranscript) userAnswerText.value += finalTranscript;
    };
    recognition.onerror = (event: any) => {
      ElMessage.error(`语音识别错误: ${event.error}`);
      if (isVoiceRecording.value) toggleVoiceInput();
    };
    recognition.onend = () => {
      if (isVoiceRecording.value) { isVoiceRecording.value = false; ElMessage.info('语音识别已停止。'); }
    };
  } else {
    console.warn("您的浏览器不支持语音识别功能。");
  }
}
async function setupMedia() {
  try {
    if (!navigator.mediaDevices?.getUserMedia) throw new Error('浏览器不支持媒体设备API');
    mediaStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    if (userVideoPlayer.value) {
      userVideoPlayer.value.srcObject = mediaStream;
      userVideoPlayer.value.onloadedmetadata = () => {
        if (faceApiModelsLoaded.value) {
          startFaceAnalysis();
        } else {
          const stopWatch = watch(faceApiModelsLoaded, (loaded) => {
            if(loaded) { startFaceAnalysis(); stopWatch(); }
          });
        }
      };
    }
  } catch (err) {
    ElMessage.error('无法访问摄像头或麦克风，请检查权限！');
    interviewStatus.value = 'error';
    faceStatus.value = { text: '设备访问失败', type: 'warning' };
  }
}
function toggleVoiceInput() {
  if (!recognition) return ElMessage.error('您的浏览器不支持语音识别功能。');
  isVoiceRecording.value = !isVoiceRecording.value;
  if (isVoiceRecording.value) { recognition.start(); ElMessage.success('语音识别已开始...'); }
  else { recognition.stop(); }
}
function clearAllHistories() {
    emotionHistory.value = [];
    ageGenderHistory.value = [];
    gazeHistory.value = [];
}
const handleFileChange = (file: UploadFile) => {
  const rawFile = file.raw as UploadRawFile;
  const allowedTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
  if (!allowedTypes.includes(rawFile.type)) return ElMessage.error('请上传PDF或DOCX格式的文件！');
  if (rawFile.size / 1024 / 1024 > 5) return ElMessage.error('文件大小不能超过5MB！');
  selectedFile.value = file;
};
const handleFileRemove = () => {
  selectedFile.value = null;
  uploadRef.value!.clearFiles();
  uploadProgress.value = 0;
  if (progressInterval) clearInterval(progressInterval);
};
const handleExceed: UploadProps['onExceed'] = (files) => {
  uploadRef.value!.clearFiles();
  const file = files[0] as UploadRawFile;
  uploadRef.value!.handleStart(file);
};
const simulateProgress = () => {
  uploadProgress.value = 0;
  if (progressInterval) clearInterval(progressInterval);
  progressInterval = window.setInterval(() => {
    if (uploadProgress.value < 90) uploadProgress.value += Math.round(Math.random() * 10);
    else if (progressInterval) clearInterval(progressInterval);
  }, 300);
};
function onTabClick(tab: TabsPaneContext) {
  if (tab.paneName === 'feedback') newFeedback.value = false;
}

</script>

<style scoped>

.interview-page-bg { width: 100%; min-height: 100vh; padding: 24px; box-sizing: border-box; background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%); display: flex; align-items: center; }
.interview-container { width: 100%; max-width: 1800px; margin: 0 auto; height: calc(100vh - 48px); }
.panel { height: 100%; }
.glass-card { height: 100%; border-radius: 16px; background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.25); box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15); padding: 24px; box-sizing: border-box; display: flex; flex-direction: column; overflow: hidden;}
.status-card { overflow-y: auto; }

.side-dashboard { display: flex; flex-direction: column; gap: 24px; height: 100%; }
.card-header { font-size: 1.1rem; font-weight: 600; display: flex; justify-content: space-between; align-items: center; padding-bottom: 16px; color: #303133; }
.card-header .el-icon { margin-right: 8px; vertical-align: middle; }
.self-view-card { height: auto; flex-shrink: 0; }
.user-video-container { width: 100%; aspect-ratio: 4 / 3; background-color: #000; border-radius: 8px; position: relative; overflow: hidden; margin-bottom: 12px;}
.user-video { width: 100%; height: 100%; object-fit: cover; transform: scaleX(-1); }
.status-card :deep(.status-label) { font-weight: 500; width: 100px; }


.behavior-analysis-card { background-color: rgba(255, 255, 255, 0.5); border-radius: 8px; }
.behavior-analysis-card :deep(.el-descriptions__cell) { background-color: transparent !important; }
.behavior-analysis-card :deep(.behavior-label) { font-weight: 500; }


.resume-upload-section { display: flex; flex-direction: column; }
.resume-uploader :deep(.el-upload) { width: 100%; }
.resume-uploader :deep(.el-upload-dragger) { padding: 0; width: 100%; height: 100px; display: flex; align-items: center; justify-content: center; border: 2px dashed #d9d9d9; border-radius: 10px; transition: border-color 0.3s, background-color 0.3s; }
.resume-uploader :deep(.el-upload-dragger:hover) { border-color: var(--el-color-primary); }
.resume-uploader :deep(.is-dragover) { background-color: rgba(64, 158, 255, 0.05); border-color: var(--el-color-primary); border-style: solid; }
.upload-placeholder { text-align: center; color: var(--el-text-color-regular); transition: all 0.3s; }
.upload-placeholder .upload-icon { transition: all 0.3s; margin-bottom: 8px;}
.upload-placeholder .el-upload__text { font-size: 1rem; line-height: 1.5; }
.file-preview-wrapper { display: flex; align-items: center; width: 100%; height: 100%; padding: 12px; box-sizing: border-box; text-align: left; cursor: default; position: relative; overflow: hidden; background-color: rgba(245, 247, 250, 0.8); }
.file-icon { flex-shrink: 0; width: 40px; height: 40px; color: var(--el-color-primary); }
.file-details { flex-grow: 1; margin-left: 12px; overflow: hidden; display: flex; flex-direction: column; justify-content: center;}
.file-name { font-weight: 500; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.file-size { font-size: 0.8rem; color: #909399; }
.file-actions { flex-shrink: 0; margin-left: 12px; }
.upload-progress-bar { position: absolute; bottom: 0; left: 0; right: 0; }
.upload-progress-bar :deep(.el-progress-bar__outer) { background-color: transparent; }
.submit-resume-btn { width: 100%; margin-top: 12px; font-weight: 600; }
.upload-success-tip { display: flex; align-items: center; padding: 16px; border-radius: 10px; background-color: var(--el-color-success-light-9); border: 1px solid var(--el-color-success-light-5); }
.success-text { margin-left: 12px; display: flex; flex-direction: column; }
.success-text span { font-weight: 500; color: var(--el-color-success-dark-2); }
.success-text small { font-size: 0.8rem; color: #909399; margin-top: 2px; }


.interview-tips-section { font-size: 0.9rem; color: #606266; }
.tips-list { margin: 0; padding: 0; }
.tips-list dt { font-weight: 600; color: #303133; margin-top: 10px; }
.tips-list dt:first-child { margin-top: 0; }
.tips-list dd { margin-left: 0; line-height: 1.5; margin-top: 4px; }

.main-cockpit { gap: 16px; overflow: hidden;}
.interviewer-zone { display: flex; gap: 24px; flex-shrink: 0; margin-bottom: 16px; }
.question-display { flex-grow: 1; height: auto; display: flex; flex-direction: column; }
.avatar-container { width: 320px; height: 240px; background-color: #1a1a1a; border-radius: 12px; flex-shrink: 0; overflow: hidden;}
.question-tabs { height: 100%; display: flex; flex-direction: column; }
.question-tabs :deep(.el-tabs__content) { flex-grow: 1; }
.question-tabs :deep(.el-tab-pane) { height: 100%; display: flex; align-items: center; justify-content: center; }
.question-content, .feedback-content { font-size: 1.5rem; line-height: 1.7; text-align: center; padding: 20px; color: #303133; }
.feedback-content { font-size: 1.1rem; text-align: left; }
.feedback-content p { margin: 0; }
.feedback-dot { margin-left: 5px; }
.action-zone { flex-grow: 1; display: flex; flex-direction: column; }
.answer-textarea { flex-grow: 1; margin-bottom: 16px; }
.answer-textarea :deep(textarea) { height: 100% !important; font-size: 1.1rem; line-height: 1.6; }
.submit-final-btn { font-weight: bold; min-width: 180px; flex-shrink: 0; }
.finished-box { flex-grow: 1; display: flex; align-items: center; justify-content: center; }
.controls-footer { display: flex; justify-content: space-between; align-items: center; flex-shrink: 0; }
.voice-controls { display: flex; align-items: center; gap: 16px; }


.face-scan-anim { position: absolute; top: 10%; left: 15%; width: 70%; height: 80%; pointer-events: none; transition: all 0.3s ease; }
.scan-corner { position: absolute; width: 25px; height: 25px; border-style: solid; border-color: rgba(0, 255, 132, 0.7); transition: border-color 0.3s ease; }
.top-left { top: 0; left: 0; border-width: 4px 0 0 4px; }
.top-right { top: 0; right: 0; border-width: 4px 4px 0 0; }
.bottom-left { bottom: 0; left: 0; border-width: 0 0 4px 4px; }
.bottom-right { bottom: 0; right: 0; border-width: 0 4px 4px 0; }
.scan-line { position: absolute; top: 0; left: 0; width: 100%; height: 3px; background: linear-gradient(to right, transparent, rgba(0, 255, 132, 0.8), transparent); animation: scan-anim 3s infinite linear; box-shadow: 0 0 10px rgba(0, 255, 132, 0.8); }
.face-scan-anim.no-face .scan-corner { border-color: rgba(245, 108, 108, 0.8); }
.face-scan-anim.no-face .scan-line { display: none; }
@keyframes scan-anim { from { top: 0; } to { top: 100%; } }
.voice-wave-container { display: flex; justify-content: center; align-items: center; height: 40px; width: 80px; }
.wave-bar { display: inline-block; width: 5px; height: 4px; margin: 0 3px; background-color: #409EFF; border-radius: 3px; transition: height 0.3s ease-in-out; }
.voice-wave-container.is-active .wave-bar { animation: wave-anim 1.2s infinite ease-in-out; }
.voice-wave-container.is-active .wave-bar:nth-child(2) { animation-delay: -1.1s; }
.voice-wave-container.is-active .wave-bar:nth-child(3) { animation-delay: -1.0s; }
.voice-wave-container.is-active .wave-bar:nth-child(4) { animation-delay: -0.9s; }
.voice-wave-container.is-active .wave-bar:nth-child(5) { animation-delay: -0.8s; }
@keyframes wave-anim { 0%, 40%, 100% { height: 4px; opacity: 0.7; } 20% { height: 28px; opacity: 1; } }
</style>