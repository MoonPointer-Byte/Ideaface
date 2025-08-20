<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>题库中心</span>
          <span class="header-extra">共 {{ pagination.total }} 道题目</span>
        </div>
      </template>
      <div class="filter-bar">
        <el-select v-model="filters.category" placeholder="按岗位类别筛选" clearable @change="handleSearch"
          style="width: 240px;" size="large">
          <el-option label="人工智能工程师" value="ai-engineer"></el-option>
          <el-option label="后端开发工程师" value="backend-developer"></el-option>
          <el-option label="产品经理" value="product-manager"></el-option>
        </el-select>
        <el-input v-model="filters.keyword" placeholder="按题目关键词或ID搜索" clearable @keyup.enter="handleSearch"
          size="large">
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>

      <el-table :data="questions" v-loading="loading" style="width: 100%" stripe
        :row-key="(row: any) => row.id">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="题目" min-width="300" />
        <el-table-column prop="category" label="类别" width="180" align="center">
          <template #default="{ row }">{{ categoryMap[row.category as keyof typeof categoryMap] }}</template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="difficultyMap[row.difficulty as keyof typeof difficultyMap]?.type">{{ row.difficulty
              }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && questions.length === 0" description="未找到相关题目" />

      <el-pagination v-if="!loading && pagination.total > 0" class="pagination" background
        layout="total, prev, pager, next, jumper" :total="pagination.total" :page-size="pagination.size"
        :current-page="pagination.page + 1" @current-change="handlePageChange" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import { Search } from '@element-plus/icons-vue';

interface Question { id: number; title: string; category: string; difficulty: string; }

const router = useRouter();
const loading = ref(true);
const questions = ref<Question[]>([]);
const filters = reactive({ category: '', keyword: '' }); // title -> keyword
const pagination = reactive({ page: 0, size: 10, total: 0 });

const categoryMap = { 'ai-engineer': '人工智能', 'backend-developer': '后端开发', 'product-manager': '产品' };
const difficultyMap = { '简单': { type: 'success' as const }, '中等': { type: 'warning' as const }, '困难': { type: 'danger' as const } };

const fetchQuestions = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      category: filters.category,
      keyword: filters.keyword,
    };
    const response = await api.get('/content/questions', { params });
    if (response.data && response.data.content) {
      questions.value = response.data.content;
      pagination.total = response.data.totalElements;
    }
  } catch (error) { console.error("获取题目列表失败:", error); }
  finally { loading.value = false; }
};

onMounted(fetchQuestions);

const handleSearch = () => {
  pagination.page = 0;
  fetchQuestions();
};

const handlePageChange = (newPage: number) => {
  pagination.page = newPage - 1;
  fetchQuestions();
};

const viewDetail = (id: number) => router.push({ name: 'questionDetail', params: { id } });
</script>

<style scoped>
.box-card {
  min-height: calc(100vh - 105px);
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1.2rem;
  font-weight: 600;
}

.header-extra {
  font-size: 0.9rem;
  font-weight: normal;
  color: var(--app-secondary-text-color);
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.el-table {
  flex-grow: 1;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>