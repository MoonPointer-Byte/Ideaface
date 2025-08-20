<template>
  <div class="page-container">
    <el-skeleton :rows="10" animated v-if="loading" />
    <el-card v-if="!loading && question" class="question-detail-card">
      <template #header>
        <div class="question-header">
          <div class="header-left">
            <el-tag :type="difficultyMap[question.difficulty as keyof typeof difficultyMap]?.type" size="large">{{
              question.difficulty }}</el-tag>
            <h1>{{ question.title }}</h1>
          </div>
          <el-tag effect="plain">{{ categoryMap[question.category as keyof typeof categoryMap] }}</el-tag>
        </div>
      </template>
      <div class="question-content" v-html="question.content.replace(/\n/g, '<br />')"></div>

      <el-divider />

      <CommentSection :question-id="questionId" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/services/api';
import CommentSection from '@/components/CommentSection.vue';

interface Question { id: number; title: string; content: string; category: string; difficulty: string; }

const route = useRoute();
const question = ref<Question | null>(null);
const loading = ref(true);
const questionId = Number(route.params.id);

const categoryMap = { 'ai-engineer': '人工智能', 'backend-developer': '后端开发', 'product-manager': '产品' };
const difficultyMap = { '简单': { type: 'success' as const }, '中等': { type: 'warning' as const }, '困难': { type: 'danger' as const } };

onMounted(async () => {
  try {
    const response = await api.get(`/content/questions/${questionId}`);
    question.value = response.data;
  } catch (error) { console.error("获取题目详情失败:", error); }
  finally { loading.value = false; }
});
</script>


<style scoped>
.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-left h1 {
  font-size: 1.5rem;
  margin: 0;
}

.question-content {
  padding: 20px;
  font-size: 1rem;
  line-height: 1.8;
  color: var(--app-secondary-text-color);
}

.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 20px;
}

.comment-form {
  margin-bottom: 30px;
}

.form-actions {
  text-align: right;
  margin-top: 10px;
}

.comment-list {
  list-style: none;
  padding: 0;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-top: 1px solid var(--app-border-color);
}

.comment-item:first-child {
  border-top: none;
  padding-top: 0;
}

.comment-body {
  flex-grow: 1;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.comment-author {
  font-weight: bold;
  color: var(--app-text-color);
}

.comment-time {
  font-size: 0.8rem;
  color: #999;
}

.comment-text {
  margin: 5px 0 0;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>