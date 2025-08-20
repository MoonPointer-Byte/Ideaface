<template>
  <el-input v-model="content" type="textarea" :rows="10" placeholder="请在此处输入内容..."/>
  <el-button type="primary" :loading="isLoading" @click="submit" class="submit-btn">
    {{ isLoading ? '分析中...' : '提交分析' }}
  </el-button>
</template>

<script setup lang="ts">
import { ref } from 'vue';
defineProps<{ isLoading: boolean }>();
const emit = defineEmits(['submit']);
const content = ref('');
const submit = () => emit('submit', content.value);
</script>

<style scoped>
.submit-btn { width: 100%; margin-top: 15px; padding: 18px 0; font-size: 1rem; }
</style>