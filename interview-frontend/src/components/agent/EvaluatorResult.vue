<template>
  <div ref="radarChart" style="width: 100%; height: 350px;"></div>
  <el-descriptions :column="1" border>
    <el-descriptions-item label-class-name="my-label" label="综合得分">
      <el-tag size="large">{{ result.score }} / 100</el-tag>
    </el-descriptions-item>
    <el-descriptions-item label-class-name="my-label" label="核心优势">{{ result.strength }}</el-descriptions-item>
    <el-descriptions-item label-class-name="my-label" label="待优化点">{{ result.weakness }}</el-descriptions-item>
  </el-descriptions>
</template>
<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import * as echarts from 'echarts';

const props = defineProps<{ result: any }>();
const radarChart = ref<HTMLElement>();
let myChart: echarts.ECharts;

const setChartOption = (data: number[]) => {
  if (!myChart) myChart = echarts.init(radarChart.value!);
  myChart.setOption({
    tooltip: { trigger: 'item' },
    radar: { indicator: [{ name: '内容完整性', max: 100 }, { name: '关键词匹配', max: 100 }, { name: '成果量化', max: 100 }, { name: '格式排版', max: 100 }, { name: '专业技能', max: 100 }] },
    series: [{ name: '简历评估', type: 'radar', data: [{ value: data }] }],
  });
};

onMounted(() => { if (props.result) setChartOption(props.result.radarData); });
watch(() => props.result, (newVal) => { if (newVal) setChartOption(newVal.radarData); });
</script>
<style>
.my-label { width: 120px; }
</style>