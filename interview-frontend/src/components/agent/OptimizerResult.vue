<template>
  <el-timeline>
    <el-timeline-item
      v-for="(item, index) in result"
      :key="index"
      :timestamp="item.category"
      placement="top"
      :type="item.type"
    >
      <el-card>
        <h4>{{ item.title }}</h4>
        <p>{{ item.content }}</p>
      </el-card>
    </el-timeline-item>
  </el-timeline>
</template>
<script setup lang="ts">
defineProps<{ result: any[] }>();
</script>
<style scoped>
h4 { margin: 0; }
p { margin: 8px 0 0; color: #606266; }
</style>