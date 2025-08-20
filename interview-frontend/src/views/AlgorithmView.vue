<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>算法测试</span>
          <span class="header-extra">共 {{ pagination.total }} 道算法题</span>
        </div>
      </template>
      <div class="filter-bar">
        <el-input v-model="filters.keyword" placeholder="按题目标题搜索" clearable @keyup.enter="handleSearch"
          size="large">
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>

      <el-table :data="filteredQuestions" v-loading="loading" style="width: 100%" stripe
        :row-key="(row: any) => row.id">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="题目" min-width="300">
          <template #default="{ row }">
            <div class="question-title">{{ row.title }}</div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120" align="center">
          <template #default>
            <el-tag type="info">算法题</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120" align="center">
          <template #default>
            <el-tag type="warning">牛客网</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="startSolving(row.id)">开始答题</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && filteredQuestions.length === 0" description="未找到相关算法题" />

      <el-pagination v-if="!loading && pagination.total > 0" class="pagination" background
        layout="total, prev, pager, next, jumper" :total="pagination.total" :page-size="pagination.size"
        :current-page="pagination.page + 1" @current-change="handlePageChange" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Search } from '@element-plus/icons-vue';

interface AlgorithmQuestion {
  id: number;
  title: string;
  url: string;
  description: string;
}

const router = useRouter();
const loading = ref(true);
const questions = ref<AlgorithmQuestion[]>([]);
const filters = reactive({ keyword: '' });
const pagination = reactive({ page: 0, size: 15, total: 0 });


const loadAlgorithmQuestions = async () => {
  loading.value = true;
  try {

    const response = await fetch('/nowcoder_problems(1).json');
    const data = await response.json();
    questions.value = data;
    pagination.total = data.length;
  } catch (error) {
    console.error("加载算法题目失败:", error);
   
    questions.value = [
      { id: 1, title: "反转链表", url: "", description: "给定一个单链表的头结点，反转该链表后，返回新链表的表头。" },
      { id: 2, title: "链表内指定区间反转", url: "", description: "将一个节点数为 size 链表 m 位置到 n 位置之间的区间反转。" },
      { id: 3, title: "合并两个排序的链表", url: "", description: "输入两个递增的链表，合并这两个链表并使新链表中的节点仍然是递增排序的。" }
    ];
    pagination.total = questions.value.length;
  }
  finally {
    loading.value = false;
  }
};


const filteredQuestions = computed(() => {
  let result = questions.value;
  
  if (filters.keyword) {
    result = result.filter(q => 
      q.title.toLowerCase().includes(filters.keyword.toLowerCase()) ||
      q.id.toString().includes(filters.keyword)
    );
  }
  
  const start = pagination.page * pagination.size;
  const end = start + pagination.size;
  return result.slice(start, end);
});

onMounted(loadAlgorithmQuestions);

const handleSearch = () => {
  pagination.page = 0;
};

const handlePageChange = (newPage: number) => {
  pagination.page = newPage - 1;
};

const startSolving = (questionId: number) => {
  router.push(`/algorithm/${questionId}`);
};
</script>

<style scoped>
.page-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.header-extra {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}

.filter-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 16px;
}

.question-title {
  font-weight: 500;
  cursor: pointer;
}

.question-title:hover {
  color: var(--el-color-primary);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 