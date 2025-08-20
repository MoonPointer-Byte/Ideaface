<template>
  <div class="search-input-container">
    <el-input
      v-model="keyword"
      placeholder="搜索文章..."
      :prefix-icon="Search"
      clearable
      @keyup.enter="handleSearch"
      @clear="handleSearch"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Search } from '@element-plus/icons-vue';

const router = useRouter();
const keyword = ref('');

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ name: 'searchResult', query: { q: keyword.value } });
  }
};
</script>

<style scoped>
.search-input-container {
  width: 220px;
  margin: 0 20px;
}
</style>