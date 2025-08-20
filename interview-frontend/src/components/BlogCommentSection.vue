<template>
  <div class="comment-section">
    <h2 class="section-title">评论区 ({{ commentCount }})</h2>
    
    <div class="comment-form">
      <el-input
        v-model="newCommentText"
        type="textarea"
        :rows="3"
        placeholder="发表你的看法..."
        maxlength="500"
        show-word-limit
      />
      <div class="form-actions">
        <el-button type="primary" @click="submitComment" :disabled="!newCommentText.trim()" :loading="isSubmitting">
          发表评论
        </el-button>
      </div>
    </div>
    
    <div v-loading="isLoading">
      <div v-if="comments.length > 0" class="comment-list">
  
        <CommentItem 
          v-for="comment in comments" 
          :key="comment.id" 
          :comment="comment" 
          :post-id="postId" 
          @reply-submitted="fetchComments" 
        />
      </div>
      <el-empty v-else description="暂无评论，快来发表第一条评论吧！" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineProps, computed } from 'vue';
import api from '@/services/api';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store/user';
import CommentItem from './CommentItem.vue';

const props = defineProps<{
  postId: number;
}>();

const userStore = useUserStore();
const comments = ref<any[]>([]);
const newCommentText = ref('');
const isLoading = ref(false);
const isSubmitting = ref(false);

const commentCount = computed(() => {
    let count = 0;
    const countReplies = (comment: any) => {
        count++;
        if (comment.replies) {
            for (const reply of comment.replies) {
                countReplies(reply);
            }
        }
    };
    for (const comment of comments.value) {
        countReplies(comment);
    }
    return count;
});

const fetchComments = async () => {
  if (!props.postId) return;
  isLoading.value = true;
  try {
    const response = await api.get(`/posts/${props.postId}/comments`);
    comments.value = response.data;
  } catch (error) {
    console.error('获取评论列表失败:', error);
    ElMessage.error('无法加载评论');
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchComments();
});

const submitComment = async () => {
  if (!userStore.isLoggedIn) {
      return ElMessage.warning('请先登录再发表评论');
  }
  if (!newCommentText.value.trim()) {
      return ElMessage.warning('评论内容不能为空');
  }

  isSubmitting.value = true;
  try {
    const payload = { content: newCommentText.value };
    await api.post(`/posts/${props.postId}/comments`, payload);
    ElMessage.success('评论发表成功！');
    newCommentText.value = '';
    await fetchComments();
  } catch (error) {
    console.error('发表评论失败:', error);
    ElMessage.error('评论失败，请稍后重试');
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>

.comment-section { margin-top: 40px; }
.section-title { font-size: 1.2rem; font-weight: 600; margin: 0 0 20px; padding-left: 10px; border-left: 4px solid #409EFC; }
.comment-form { display: flex; flex-direction: column; gap: 10px; margin-bottom: 30px; }
.form-actions { display: flex; justify-content: flex-end; }
.comment-list { display: flex; flex-direction: column; gap: 10px; }
</style>