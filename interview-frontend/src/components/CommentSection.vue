<template>
  <div class="comment-section">
    <h2 class="section-title">讨论区 ({{ comments.length }})</h2>
    
    <div v-if="userStore.isLoggedIn" class="comment-form">
      <el-input 
        v-model="newComment" 
        type="textarea" 
        :rows="3" 
        placeholder="分享你的见解和思路..." 
        maxlength="500"
        show-word-limit
      />
      <div class="form-actions">
        <el-button type="primary" @click="postComment" :loading="postingComment">
          发表评论
        </el-button>
      </div>
    </div>
    <el-alert v-else title="请登录后发表评论" type="info" show-icon :closable="false" style="margin-bottom: 20px;" />
    
    <ul class="comment-list" v-loading="loadingComments">
      <li v-for="comment in comments" :key="comment.id" class="comment-item">
      
        <el-avatar class="comment-avatar" :src="`https://api.multiavatar.com/${comment.username}.svg`" />
        <div class="comment-body">
          <div class="comment-meta">
            <span class="comment-author">{{ comment.username }}</span>
            <span class="comment-time">{{ new Date(comment.createTime).toLocaleString() }}</span>
          </div>
          <p class="comment-text">{{ comment.content }}</p>
        </div>
      </li>
    </ul>
    <el-empty v-if="!loadingComments && comments.length === 0" description="暂无评论，快来抢占第一个沙发吧！" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/store/user';
import api from '@/services/api';
import { ElMessage } from 'element-plus';

interface Comment {
  id: number;
  content: string;
  createTime: string;
  username: string;
  userId: number;
  questionId: number;
}
const props = defineProps<{
  questionId: number;
}>();

const userStore = useUserStore();
const comments = ref<Comment[]>([]);
const newComment = ref('');
const loadingComments = ref(true);
const postingComment = ref(false);

const fetchComments = async () => {
  if (!props.questionId) return;
  loadingComments.value = true;
  try {
    const response = await api.get(`/comments/question/${props.questionId}`);
    comments.value = response.data;
  } catch (error) { 
    console.error("获取评论失败:", error);
  } finally { 
    loadingComments.value = false; 
  }
};

const postComment = async () => {
  if (!newComment.value.trim()) {
    return ElMessage.warning("评论内容不能为空");
  }
  
  if (!userStore.currentUser) {
    return ElMessage.error("无法获取当前用户信息，请重新登录。");
  }
  
  postingComment.value = true;
  try {
    const response = await api.post('/comments', {
      content: newComment.value,
      questionId: props.questionId,
  
    });
  
    comments.value.unshift(response.data); 
    newComment.value = '';
    ElMessage.success("评论发表成功！");
  } catch (error) { 
    console.error("发表评论失败:", error);
  } finally { 
    postingComment.value = false; 
  }
};

onMounted(() => {
  fetchComments();
});
</script>

<style scoped>
.section-title { font-size: 1.2rem; font-weight: 600; margin-bottom: 20px; }
.comment-form { margin-bottom: 30px; }
.form-actions { text-align: right; margin-top: 10px; }
.comment-list { list-style: none; padding: 0; }
.comment-item { display: flex; gap: 15px; padding: 15px 0; border-top: 1px solid var(--app-border-color); }
.comment-item:first-child { border-top: none; padding-top: 0; }
.comment-body { flex-grow: 1; }
.comment-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 5px; }
.comment-author { font-weight: bold; color: var(--app-text-color); }
.comment-time { font-size: 0.8rem; color: #999; }
.comment-text { margin: 5px 0 0; line-height: 1.6; white-space: pre-wrap; color: var(--app-secondary-text-color); }
</style>