<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span><el-icon><Memo /></el-icon> 我的面试历史</span>
        </div>
      </template>

      <el-table :data="history" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="sessionId" label="面试ID" min-width="250" show-overflow-tooltip />
        <el-table-column prop="positionId" label="面试岗位" width="180">
          
            <template #default="{ row }">{{ categoryMap[row.positionId as keyof typeof categoryMap] || row.positionId }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="面试时间" width="200" sortable>
            <template #default="{ row }">{{ new Date(row.createTime).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="status" label="分析状态" width="120" align="center">
          <template #default="{ row }">
         
            <el-tag :type="statusMap[row.status as keyof typeof statusMap]?.type" effect="dark" size="small">
              {{ statusMap[row.status as keyof typeof statusMap]?.text || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="viewReport(row.sessionId)"
              :disabled="row.status !== 'COMPLETED'"
            >
              <el-icon><Document /></el-icon>
              查看报告
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && history.length === 0" description="您还没有任何面试记录，快去开始一场面试吧！">
        <el-button type="primary" @click="$router.push('/interview')">开始第一次面试</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import { Memo, Document } from '@element-plus/icons-vue';

type InterviewStatus = 'COMPLETED' | 'PROCESSING' | 'FAILED';
interface HistoryItem {
  sessionId: string;
  positionId: string;
  positionTitle: string | null;
  createTime: string;
  status: InterviewStatus;
}

const router = useRouter();
const loading = ref(true);
const history = ref<HistoryItem[]>([]);

const categoryMap: Record<string, string> = { 
    'ai-engineer': '人工智能工程师', 
    'backend-developer': '后端开发工程师', 
    'product-manager': '产品经理' 
};

const statusMap: Record<InterviewStatus, { text: string; type: 'success' | 'primary' | 'danger' }> = {
    'COMPLETED': { text: '已完成', type: 'success' },
    'PROCESSING': { text: '分析中', type: 'primary' },
    'FAILED': { text: '分析失败', type: 'danger' },
};

onMounted(async () => {
  try {
    const response = await api.get('/user/history');
    history.value = response.data;
  } catch (error) {
    console.error("获取面试历史失败:", error);
  } finally {
    loading.value = false;
  }
});

const viewReport = (sessionId: string) => {
  router.push({ name: 'report', params: { sessionId } });
};
</script>

<style scoped>
.card-header { 
  font-size: 1.2rem; 
  font-weight: bold; 
  display: flex;
  align-items: center;
  gap: 10px;
}
.el-table .el-button .el-icon {
  margin-right: 5px;
}
</style>