<template>
  <div class="report-page-bg-light">
    <el-row justify="center">
      <el-col :span="22" :lg="20" :xl="16">
        
        <div v-if="isLoading" class="loading-container">
          <el-icon class="is-loading" :size="50"><Loading /></el-icon>
          <p class="loading-text">{{ loadingText }}</p>
        </div>

        <el-card v-if="!isLoading && reportData && reportData.status === 'COMPLETED'" class="report-card" shadow="always">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon :size="28" color="#409EFC"><Document /></el-icon>
                <span>面试评估报告</span>
              </div>
              <div class="header-info">
                <span>面试用户: <strong>{{ reportData.username || 'N/A' }}</strong></span>
                <span>面试时间: <strong>{{ interviewTime }}</strong></span>
              </div>
            </div>
          </template>

          <div class="report-content">
            <el-descriptions :column="1" border title="综合评语" class="overview-desc">
              <el-descriptions-item>
                <template #label><el-icon><ChatDotRound /></el-icon> 核心评价</template>
                <span class="comment-text">{{ overallComment }}</span>
              </el-descriptions-item>
            </el-descriptions>

            <h2 class="section-title">能力维度分析</h2>
            <el-row :gutter="40" align="middle">
              <el-col :span="24" :md="14">
                <div ref="radarChartEl" class="chart-container"></div>
              </el-col>
              <el-col :span="24" :md="10">
                <div v-for="item in scoreTableData" :key="item.dimension" class="score-item">
                  <span class="score-label">{{ item.dimension }}</span>
                  <el-progress :percentage="item.score * 20" :color="getScoreColor(item.score)"
                    :format="() => `${item.score.toFixed(1)} 分`" />
                </div>
              </el-col>
            </el-row>

            <h2 class="section-title">亮点表现</h2>
            <div v-if="highlights && highlights.length > 0" class="suggestion-list">
              <el-alert v-for="(item, index) in highlights" :key="index"
                :title="item.highlight" type="success" :closable="false" show-icon class="suggestion-item"
              />
            </div>
             <el-empty v-else description="本次面试未发现明显的亮点表现" :image-size="80"></el-empty>

            <h2 class="section-title">AI 改进建议</h2>
            <div class="suggestion-list">
               <el-alert v-for="(item, index) in improvementSuggestions" :key="index"
                :title="item.dimension" type="warning" :closable="false" show-icon class="suggestion-item"
              >
                {{ item.suggestion }}
              </el-alert>
            </div>

            <h2 class="section-title">面试记录回顾</h2>
            <el-timeline class="dialogue-timeline">
              <el-timeline-item v-for="(item, index) in dialogueHistory" :key="index"
                :timestamp="`问题 ${index + 1}`" placement="top" type="primary" hollow>
                <el-card class="dialogue-card">
                  <h4>{{ item.question }}</h4>
                  <p class="answer-text"><strong>你的回答:</strong> {{ item.answer }}</p>
                  <p class="eval-text"><strong>AI 单项点评:</strong> {{ item.evaluation }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
        
        <div v-if="!isLoading && (!reportData || reportData.status !== 'COMPLETED')" class="result-container">
           <el-empty :description="emptyDescription">
              <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
            </el-empty>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import api from '@/services/api';
import * as echarts from 'echarts/core';
import { RadarChart } from 'echarts/charts';
import { Loading, Document, ChatDotRound } from '@element-plus/icons-vue';
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { ElMessage } from 'element-plus';

echarts.use([TitleComponent, TooltipComponent, LegendComponent, GridComponent, RadarChart, CanvasRenderer]);

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const isLoading = ref(true);
const loadingText = ref('报告正在生成中，AI大脑正在高速运转...');
const reportData = ref<any>(null);
const capabilityAnalysis = ref<any>(null);
const improvementSuggestions = ref<any[]>([]);
const highlights = ref<any[]>([]);
const dialogueHistory = ref<any[]>([]);
const radarChartEl = ref<HTMLElement | null>(null);
let pollingInterval: number | null = null;
let chartInstance: echarts.ECharts | null = null;

const interviewTime = computed(() => {
    const timeData = reportData.value?.createTime;
    if (!timeData) return 'N/A';

    if (Array.isArray(timeData)) {
        const [year, month, day, hour, minute] = timeData;
        const date = new Date(year, month - 1, day, hour, minute);
        if (isNaN(date.getTime())) return 'Invalid Date Array';
        return date.toLocaleString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    } else if (typeof timeData === 'string') {
        const date = new Date(timeData.replace('T', ' '));
        if (isNaN(date.getTime())) return 'Invalid Date String';
        return date.toLocaleString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    }
    return 'Unknown Date Format';
});
const overallComment = computed(() => reportData.value?.overallComment || 'AI正在总结您的表现...');

const scoreTableData = computed(() => {
  if (!capabilityAnalysis.value) return [];
  return Object.entries(capabilityAnalysis.value).map(([dimension, score]) => ({
      dimension: mapDimensionToChinese(dimension as string),
      score: score as number,
  }));
});

const emptyDescription = computed(() => {
    if (reportData.value?.status === 'FAILED') return "抱歉，报告生成失败";
    if (['ANALYZING', 'ONGOING', 'PROCESSING'].includes(reportData.value?.status)) return "报告仍在分析中，请稍后再试";
    return "找不到该面试报告";
});

const getScoreColor = (score: number) => {
  if (score < 3) return '#F56C6C';
  if (score < 4) return '#E6A23C';
  return '#67C23A';
};

const mapDimensionToChinese = (key: string): string => {
    const map: Record<string, string> = {
        professionalKnowledge: '专业知识', skillMatch: '技能匹配度', expressionAbility: '沟通表达',
        logicalThinking: '逻辑思维', stressResistance: '抗压抗挫',
        '求职动机': '求职动机', '项目经验': '项目经验',
    };
    return map[key] || key;
};

onMounted(() => {
    startPollingForReport();
    window.addEventListener('resize', resizeChart);
});

onUnmounted(() => {
    if (pollingInterval) clearInterval(pollingInterval);
    window.removeEventListener('resize', resizeChart);
    if (chartInstance) chartInstance.dispose();
});

function resizeChart() {
    if(chartInstance) chartInstance.resize();
}

function startPollingForReport() {
    const sessionId = route.params.sessionId as string;
    if (!sessionId) {
        isLoading.value = false;
        ElMessage.error("无效的会话ID！");
        router.push('/');
        return;
    }

    fetchReport(sessionId);
    pollingInterval = window.setInterval(() => {
        if (!isLoading.value || (reportData.value && !['ANALYZING', 'ONGOING', 'PROCESSING'].includes(reportData.value.status))) {
            if (pollingInterval) clearInterval(pollingInterval);
            return;
        }
        fetchReport(sessionId);
    }, 3000);
}

async function fetchReport(sessionId: string) {
    try {
        const response = await api.get(`/reports/${sessionId}`);
        const data = response.data;
        
        if (!data.username && data.userId) {
          if (String(userStore.currentUser?.id) === data.userId) {
           data.username = userStore.currentUser?.username ?? '未知用户';
          }
        }

        if (data.status === 'COMPLETED') {
            if (pollingInterval) clearInterval(pollingInterval);
            reportData.value = data;
            isLoading.value = false;
            ElMessage.success("您的专属报告已生成！");

            capabilityAnalysis.value = JSON.parse(data.capabilityAnalysisJson);
            improvementSuggestions.value = JSON.parse(data.improvementSuggestionsJson);
            highlights.value = data.highlightsJson ? JSON.parse(data.highlightsJson) : [];
            dialogueHistory.value = data.dialogueHistoryJson ? JSON.parse(data.dialogueHistoryJson) : [];
            
            await nextTick();
            initRadarChart();
        } else if (['ANALYZING', 'ONGOING', 'PROCESSING'].includes(data.status)) {
            reportData.value = data;
            loadingText.value = `报告状态: ${data.status}, 请稍候...`;
        } else {
            if (pollingInterval) clearInterval(pollingInterval);
            reportData.value = data;
            isLoading.value = false;
        }
    } catch (error: any) {
        if (error.response?.status !== 404) {
            if (pollingInterval) clearInterval(pollingInterval);
            isLoading.value = false;
            console.error("获取报告API请求失败:", error);
            ElMessage.error("网络错误，无法获取报告。");
        } else {
          loadingText.value = '等待报告创建中...';
        }
    }
}

function initRadarChart() {
  if (!radarChartEl.value || !capabilityAnalysis.value) return;
  if (chartInstance) chartInstance.dispose();
  chartInstance = echarts.init(radarChartEl.value);
  const indicatorData = scoreTableData.value.map(item => ({ name: item.dimension, max: 5 }));
  const seriesData = scoreTableData.value.map(item => item.score);

  const option = {
    title: { text: '', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { bottom: 5, data: ['能力评估'], textStyle: { color: '#606266' } },
    radar: {
      indicator: indicatorData,
      radius: '65%', center: ['50%', '55%'],
      axisName: { color: '#666', fontSize: 14 },
      splitArea: { show: true, areaStyle: { color: ['rgba(250,250,250,0.3)', 'rgba(245,245,245,0.3)'] } },
      axisLine: { lineStyle: { color: 'rgba(190,190,190,0.5)' } },
      splitLine: { lineStyle: { color: 'rgba(200,200,200,0.5)' } }
    },
    series: [{
      name: '能力评估', type: 'radar',
      data: [{
        value: seriesData, name: '能力评估', symbol: 'circle', symbolSize: 6,
        lineStyle: { width: 2, color: '#409EFF' }, itemStyle: { color: '#409EFF' },
        areaStyle: { color: 'rgba(64, 158, 255, 0.4)' }
      }]
    }]
  };
  chartInstance.setOption(option);
}
</script>

<style scoped>
.report-page-bg-light { padding: 40px 20px; min-height: 100vh; background-color: #f0f2f5; }
.loading-container { text-align: center; padding-top: 20vh; color: #606266; }
.loading-text { margin-top: 20px; font-size: 1.2rem; }
.result-container { padding-top: 20vh; }

.report-card { border-radius: 12px; border: none; margin-bottom: 40px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 15px; color: #303133; font-size: 1.5rem; font-weight: bold;}
.header-info { font-size: 0.9rem; color: #909399; }
.header-info span { margin-left: 20px; }
.header-info strong { color: #606266; }

.report-content { padding: 10px 20px; }
.overview-desc { margin-bottom: 20px; }
.overview-desc :deep(.el-descriptions__label) { width: 120px; }
.comment-text { font-style: italic; color: #606266; line-height: 1.7; }
.section-title { font-size: 1.2rem; font-weight: 600; margin: 40px 0 20px; padding-left: 10px; border-left: 4px solid #409EFC; }
.chart-container { width: 100%; height: 400px; }

.score-item { display: flex; align-items: center; margin-bottom: 15px; }
.score-label { width: 120px; flex-shrink: 0; color: #909399; }
.el-progress { width: 100%; }

.suggestion-list { display: flex; flex-direction: column; gap: 15px; }
.suggestion-item { padding: 10px 15px; }

.dialogue-timeline { margin-top: 20px; }
.dialogue-card { border-radius: 6px; }
.dialogue-card h4 { margin: 0 0 10px 0; color: #303133; }
.answer-text { color: #606266; margin: 0 0 10px 0; }
.eval-text { color: #909399; font-style: italic; margin: 0; border-top: 1px dashed #e4e7ed; padding-top: 10px; font-size: 0.9rem;}
</style>