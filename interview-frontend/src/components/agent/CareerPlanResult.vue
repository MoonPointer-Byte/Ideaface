<template>
  <h4>推荐发展方向</h4>
  <div class="role-tags">
    <el-tag v-for="role in result.roles" :key="role" type="success" effect="dark" size="large">{{ role }}</el-tag>
  </div>
  <el-divider />
  <h4>建议学习路线</h4>
  <el-steps direction="vertical" :active="result.steps.length">
    <el-step v-for="step in result.steps" :key="step.title" :title="step.title" :description="step.desc" />
  </el-steps>
</template>
<script setup lang="ts">
defineProps<{ result: any }>();
</script>
<style scoped>
h4 { margin-bottom: 15px; }
.role-tags .el-tag { margin: 0 10px 10px 0; }
.el-divider { margin: 24px 0; }
</style>