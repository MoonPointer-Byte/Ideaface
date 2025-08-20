<template>
  <div class="comment-item">
    <el-avatar :size="40" :src="`/api/user/avatar/${comment.author.username}.svg`" class="comment-avatar" />
    <div class="comment-content">
      <div class="comment-header">
        <span class="author-name">{{ comment.author.username }}</span>
  
        <span class="create-time">{{ formatTime(comment.createTime) }}</span>
      </div>
      <p class="comment-body">{{ comment.content }}</p>
      <div class="comment-actions">
        <el-button type="primary" link @click="showReply = !showReply">回复</el-button>
      </div>

      <div v-if="showReply" class="reply-form">
        <el-input v-model="replyText" :placeholder="`回复 @${comment.author.username}`" size="small" />
        <el-button type="primary" size="small" @click="submitReply" :loading="isSubmitting" :disabled="!replyText.trim()">提交</el-button>
      </div>

      <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
        <CommentItem 
          v-for="reply in comment.replies" 
          :key="reply.id" 
          :comment="reply" 
          :post-id="postId" 
          @reply-submitted="onChildReplySubmitted" 
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits } from 'vue';
import api from '@/services/api';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store/user';
import CommentItem from './CommentItem.vue';

const props = defineProps<{
  comment: any;
  postId: number;
}>();

const emit = defineEmits(['reply-submitted']);
const userStore = useUserStore();
const showReply = ref(false);
const replyText = ref('');
const isSubmitting = ref(false);

const formatTime = (timeData: any): string => {
  if (!timeData) return 'N/A';
  if (Array.isArray(timeData)) {
    const [year, month, day, hour, minute] = timeData;
    const date = new Date(year, month - 1, day, hour, minute);
    return isNaN(date.getTime()) ? 'Invalid Date' : date.toLocaleString();
  } else if (typeof timeData === 'string') {
    const date = new Date(timeData.replace('T', ' '));
    return isNaN(date.getTime()) ? 'Invalid Date' : date.toLocaleString();
  }
  return 'Unknown Format';
};

const submitReply = async () => {
  if (!userStore.isLoggedIn) {
      return ElMessage.warning('请先登录再回复');
  }
  isSubmitting.value = true;
  try {
    const payload = { content: replyText.value, parentId: props.comment.id };
    await api.post(`/posts/${props.postId}/comments`, payload);
    ElMessage.success('回复成功！');
    replyText.value = '';
    showReply.value = false;
    emit('reply-submitted');
  } catch (error) {
    ElMessage.error('回复失败');
  } finally {
    isSubmitting.value = false;
  }
};

const onChildReplySubmitted = () => {
    emit('reply-submitted');
}
</script>

<style scoped>

.comment-item { display: flex; gap: 15px; margin-top: 15px; }
.comment-avatar { flex-shrink: 0; }
.comment-content { flex-grow: 1; }
.comment-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 5px; }
.author-name { font-weight: 600; }
.create-time { font-size: 0.8rem; color: #909399; }
.comment-body { margin: 5px 0; white-space: pre-wrap; }
.comment-actions { text-align: right; margin-top: 5px; }
.reply-form { display: flex; gap: 10px; margin-top: 10px; }
.replies-list { border-left: 2px solid #eef0f4; padding-left: 20px; margin-top: 15px; }
</style>