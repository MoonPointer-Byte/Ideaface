<template>
  <el-form :model="form" label-position="top">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="当前或期望职位">
          <el-input v-model="form.currentRole" placeholder="例如：前端开发工程师"/>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="掌握的技能">
           <el-select v-model="form.skills" multiple filterable allow-create default-first-option placeholder="输入技能后按回车创建">
           </el-select>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
  <el-button type="primary" :loading="isLoading" @click="submit" class="submit-btn">
    {{ isLoading ? '生成中...' : '获取发展建议' }}
  </el-button>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
defineProps<{ isLoading: boolean }>();
const emit = defineEmits(['submit']);
const form = reactive({ currentRole: '前端开发工程师', skills: ['Vue', 'TypeScript'] });
const submit = () => emit('submit', form);
</script>

<style scoped>
.submit-btn { width: 100%; margin-top: 15px; padding: 18px 0; font-size: 1rem; }
.el-select { width: 100%; }
</style>