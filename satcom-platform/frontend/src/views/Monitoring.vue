<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">状态监控</div>
        <div class="page-sub">信号质量与故障诊断</div>
      </div>
      <div>
        <el-button type="primary" @click="refresh">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover">
          <div class="metric-label">{{ card.label }}</div>
          <div class="metric-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="metric-desc">{{ card.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 质量分布图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>信号质量分布</span>
              <el-tag type="success">基于上报数据</el-tag>
            </div>
          </template>
          <div ref="signalChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>设备健康状态分布</span>
            </div>
          </template>
          <div ref="healthChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>信噪比分布</span>
          </template>
          <div ref="snrChartRef" class="chart-container-small"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>丢包率分布</span>
          </template>
          <div ref="packetLossChartRef" class="chart-container-small"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>延迟分布</span>
          </template>
          <div ref="latencyChartRef" class="chart-container-small"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 设备态势地图 -->
    <el-card class="map-card">
      <template #header>
        <div class="card-header">
          <span>设备态势地图</span>
          <div class="legend">
            <span class="legend-item"><i class="dot excellent"></i>优秀</span>
            <span class="legend-item"><i class="dot good"></i>良好</span>
            <span class="legend-item"><i class="dot fair"></i>一般</span>
            <span class="legend-item"><i class="dot poor"></i>较差</span>
            <span class="legend-item"><i class="dot critical"></i>危险</span>
            <el-button size="small" @click="toggleFullscreen">全屏</el-button>
          </div>
        </div>
      </template>
      <div ref="mapRef" class="map-container"></div>
    </el-card>

    <!-- 最新状态上报表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>最新状态上报</span>
          <el-button size="small" @click="showDataDialog = true">查看热量分布详情</el-button>
        </div>
      </template>
      <el-table :data="paginatedRows" style="width: 100%" v-loading="loading">
        <el-table-column prop="equipmentId" label="设备ID" width="100" />
        <el-table-column prop="signalQuality" label="信号质量" width="120">
          <template #default="scope">
            <el-tag :type="getSignalQualityType(scope.row.signalQuality)">
              {{ scope.row.signalQuality?.toFixed(2) || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="snr" label="信噪比(dB)" width="120">
          <template #default="scope">
            {{ scope.row.snr?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="power" label="功率(dBm)" width="120">
          <template #default="scope">
            {{ scope.row.power?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="packetLossRate" label="丢包率(%)" width="120">
          <template #default="scope">
            <span :style="{ color: scope.row.packetLossRate > 5 ? '#f56c6c' : '' }">
              {{ scope.row.packetLossRate?.toFixed(2) || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="latencyMs" label="延迟(ms)" width="100">
          <template #default="scope">
            <span :style="{ color: scope.row.latencyMs > 500 ? '#e6a23c' : '' }">
              {{ scope.row.latencyMs?.toFixed(0) || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="诊断结果" />
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 热量分布详情对话框 -->
    <el-dialog v-model="showDataDialog" title="设备质量热量分布详情" width="90%">
      <el-table :data="heatmapData" max-height="400">
        <el-table-column prop="equipmentId" label="设备ID" width="100" />
        <el-table-column prop="assetCode" label="资产编号" width="150" />
        <el-table-column prop="qualityScore" label="质量评分" width="120">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.qualityScore" 
              :color="getQualityColor(scope.row.qualityScore)"
              :status="scope.row.qualityScore >= 80 ? 'success' : ''"
            />
          </template>
        </el-table-column>
        <el-table-column prop="qualityLevel" label="质量等级" width="120">
          <template #default="scope">
            <el-tag :type="getQualityLevelType(scope.row.qualityLevel)">
              {{ getQualityLevelText(scope.row.qualityLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signalQuality" label="信号质量" width="100" />
        <el-table-column prop="snr" label="信噪比" width="100" />
        <el-table-column prop="power" label="功率" width="100" />
        <el-table-column prop="packetLossRate" label="丢包率" width="100" />
        <el-table-column prop="latencyMs" label="延迟" width="100" />
        <el-table-column prop="latitude" label="纬度" width="100" />
        <el-table-column prop="longitude" label="经度" width="100" />
      </el-table>
    </el-dialog>

    <!-- 健康状态设备列表对话框 -->
    <el-dialog 
      v-model="showHealthDeviceDialog" 
      :title="`${selectedHealthStatus} 设备列表`" 
      width="80%"
    >
      <div class="dialog-header-info">
        <el-tag type="info">共 {{ healthFilteredDevices.length }} 台设备</el-tag>
      </div>
      <el-table :data="healthFilteredDevices" max-height="400" v-loading="healthFilteredDevices.length === 0">
        <el-table-column prop="equipmentId" label="设备ID" width="100" />
        <el-table-column prop="assetCode" label="资产编号" width="150" />
        <el-table-column prop="qualityScore" label="质量评分" width="150">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.qualityScore || 0" 
              :color="getQualityColor(scope.row.qualityScore)"
              :status="scope.row.qualityScore >= 80 ? 'success' : ''"
            />
          </template>
        </el-table-column>
        <el-table-column prop="qualityLevel" label="质量等级" width="120">
          <template #default="scope">
            <el-tag :type="getQualityLevelType(scope.row.qualityLevel)">
              {{ getQualityLevelText(scope.row.qualityLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signalQuality" label="信号质量" width="100">
          <template #default="scope">
            {{ scope.row.signalQuality?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="snr" label="信噪比" width="100">
          <template #default="scope">
            {{ scope.row.snr?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="packetLossRate" label="丢包率(%)" width="100">
          <template #default="scope">
            <span :style="{ color: (scope.row.packetLossRate || 0) > 5 ? '#f56c6c' : '' }">
              {{ scope.row.packetLossRate?.toFixed(2) || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="latencyMs" label="延迟(ms)" width="100">
          <template #default="scope">
            <span :style="{ color: (scope.row.latencyMs || 0) > 500 ? '#e6a23c' : '' }">
              {{ scope.row.latencyMs?.toFixed(0) || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="latitude" label="纬度" width="100" />
        <el-table-column prop="longitude" label="经度" width="100" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import http from "../api/http";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import * as echarts from "echarts";

const rows = ref<any[]>([]);
const heatmapData = ref<any[]>([]);
const statistics = ref<any>(null);
const loading = ref(false);
const showDataDialog = ref(false);
const showHealthDeviceDialog = ref(false);
const selectedHealthStatus = ref("");
const healthFilteredDevices = ref<any[]>([]);

// 分页相关
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

const paginatedRows = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return rows.value.slice(start, end);
});

const mapRef = ref<HTMLDivElement | null>(null);
const signalChartRef = ref<HTMLDivElement | null>(null);
const healthChartRef = ref<HTMLDivElement | null>(null);
const snrChartRef = ref<HTMLDivElement | null>(null);
const packetLossChartRef = ref<HTMLDivElement | null>(null);
const latencyChartRef = ref<HTMLDivElement | null>(null);

let map: L.Map | null = null;
let markers: L.Marker[] = [];
let refreshTimer: number | null = null;

// ECharts instances
let signalChart: echarts.ECharts | null = null;
let healthChart: echarts.ECharts | null = null;
let snrChart: echarts.ECharts | null = null;
let packetLossChart: echarts.ECharts | null = null;
let latencyChart: echarts.ECharts | null = null;

const cards = computed(() => {
  const stats = statistics.value || {};
  return [
    { label: "设备总数", value: stats.totalDevices || 0, desc: "在线设备", color: "#409eff" },
    { label: "平均信号质量", value: (stats.signalQualityAvg || 0).toFixed(1), desc: "越高越好", color: getSignalColor(stats.signalQualityAvg) },
    { label: "平均信噪比", value: (stats.snrAvg || 0).toFixed(1), desc: "高于6为良好", color: getSnrColor(stats.snrAvg) },
    { label: "平均丢包率", value: (stats.packetLossRateAvg || 0).toFixed(2) + "%", desc: "低于5%为佳", color: getPacketLossColor(stats.packetLossRateAvg) }
  ];
});

const getSignalColor = (value: number) => {
  if (value >= 70) return "#67c23a";
  if (value >= 50) return "#e6a23c";
  return "#f56c6c";
};

const getSnrColor = (value: number) => {
  if (value >= 15) return "#67c23a";
  if (value >= 6) return "#e6a23c";
  return "#f56c6c";
};

const getPacketLossColor = (value: number) => {
  if (value <= 1) return "#67c23a";
  if (value <= 5) return "#e6a23c";
  return "#f56c6c";
};

const getSignalQualityType = (value: number) => {
  if (value >= 70) return "success";
  if (value >= 50) return "warning";
  return "danger";
};

const getQualityColor = (score: number) => {
  if (score >= 80) return "#67c23a";
  if (score >= 60) return "#409eff";
  if (score >= 40) return "#e6a23c";
  return "#f56c6c";
};

const getQualityLevelType = (level: string) => {
  const typeMap: Record<string, string> = {
    'EXCELLENT': 'success',
    'GOOD': 'primary',
    'FAIR': 'warning',
    'POOR': 'danger',
    'CRITICAL': 'danger'
  };
  return typeMap[level] || 'info';
};

const getQualityLevelText = (level: string) => {
  const textMap: Record<string, string> = {
    'EXCELLENT': '优秀',
    'GOOD': '良好',
    'FAIR': '一般',
    'POOR': '较差',
    'CRITICAL': '危险'
  };
  return textMap[level] || level;
};

// 初始化地图
const initMap = () => {
  if (!mapRef.value) return;
  map = L.map(mapRef.value, {
    center: [34.3, 108.9],
    zoom: 4,
    minZoom: 2,
    maxZoom: 18
  });

  L.tileLayer("http://localhost:8190/tiles/{z}/{x}/{y}.png", {
    maxZoom: 18,
    minZoom: 2
  }).addTo(map);
};

// 初始化图表
const initCharts = () => {
  if (signalChartRef.value) {
    signalChart = echarts.init(signalChartRef.value);
  }
  if (healthChartRef.value) {
    healthChart = echarts.init(healthChartRef.value);
  }
  if (snrChartRef.value) {
    snrChart = echarts.init(snrChartRef.value);
  }
  if (packetLossChartRef.value) {
    packetLossChart = echarts.init(packetLossChartRef.value);
  }
  if (latencyChartRef.value) {
    latencyChart = echarts.init(latencyChartRef.value);
  }
};

// 更新图表数据
const updateCharts = () => {
  const stats = statistics.value || {};
  
  // 信号质量分布图
  if (signalChart && stats.signalQualityDistribution) {
    const data = Object.entries(stats.signalQualityDistribution);
    signalChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(([k]) => k), axisLabel: { rotate: 45 } },
      yAxis: { type: 'value' },
      series: [{
        data: data.map(([, v]) => v),
        type: 'bar',
        itemStyle: { color: '#409eff' }
      }]
    });
  }

  // 健康状态饼图
  if (healthChart && stats.healthStats) {
    const data = Object.entries(stats.healthStats).map(([name, value]) => ({
      name: getQualityLevelText(name) || name,
      value: value as number,
      originalName: name
    }));
    healthChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        itemStyle: {
          color: (params: any) => {
            const colors: Record<string, string> = {
              '在线': '#67c23a',
              '告警': '#e6a23c',
              '异常': '#f56c6c',
              '离线': '#909399'
            };
            return colors[params.name] || '#409elf';
          }
        }
      }]
    });
    
    // 点击事件
    healthChart.off('click');
    healthChart.on('click', async (params: any) => {
      if (params.data && params.data.originalName) {
        const healthLevel = params.data.originalName;
        selectedHealthStatus.value = getQualityLevelText(healthLevel) || healthLevel;
        
        // 从后端获取对应健康状态的设备列表
        try {
          const res: any = await http.get("/monitoring/devices-by-health", {
            params: { healthLevel }
          });
          if (res.success) {
            healthFilteredDevices.value = res.data || [];
          }
        } catch (error) {
          console.error('获取设备列表失败:', error);
          healthFilteredDevices.value = [];
        }
        
        showHealthDeviceDialog.value = true;
      }
    });
  }

  // 信噪比分布
  if (snrChart && stats.snrDistribution) {
    const data = Object.entries(stats.snrDistribution);
    snrChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(([k]) => k), axisLabel: { rotate: 45 } },
      yAxis: { type: 'value' },
      series: [{
        data: data.map(([, v]) => v),
        type: 'bar',
        itemStyle: { color: '#67c23a' }
      }]
    });
  }

  // 丢包率分布
  if (packetLossChart && stats.packetLossRateDistribution) {
    const data = Object.entries(stats.packetLossRateDistribution);
    packetLossChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(([k]) => k), axisLabel: { rotate: 45 } },
      yAxis: { type: 'value' },
      series: [{
        data: data.map(([, v]) => v),
        type: 'bar',
        itemStyle: { color: '#e6a23c' }
      }]
    });
  }

  // 延迟分布
  if (latencyChart && stats.latencyDistribution) {
    const data = Object.entries(stats.latencyDistribution);
    latencyChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(([k]) => k), axisLabel: { rotate: 45 } },
      yAxis: { type: 'value' },
      series: [{
        data: data.map(([, v]) => v),
        type: 'bar',
        itemStyle: { color: '#909399' }
      }]
    });
  }
};

// 渲染地图标记
const renderMarkers = (data: any[]) => {
  if (!map) return;
  
  markers.forEach((marker) => marker.remove());
  markers = [];

  data.forEach((item) => {
    if (item.latitude == null || item.longitude == null) return;
    
    let color = "#67c23a";
    if (item.qualityLevel === "EXCELLENT") color = "#67c23a";
    else if (item.qualityLevel === "GOOD") color = "#409eff";
    else if (item.qualityLevel === "FAIR") color = "#e6a23c";
    else if (item.qualityLevel === "POOR") color = "#f56c6c";
    else if (item.qualityLevel === "CRITICAL") color = "#ff0000";
    else color = "#909399";

    const marker = L.marker([item.latitude, item.longitude], {
      icon: L.divIcon({
        className: "status-marker",
        html: `<div style="background:${color}" class="status-marker-dot"></div>`,
        iconSize: [14, 14]
      })
    });

    marker.bindPopup(`
      <b>设备ID：</b>${item.equipmentId}<br/>
      <b>资产编号：</b>${item.assetCode || '-'}<br/>
      <b>质量评分：</b>${item.qualityScore?.toFixed(1) || '-'}<br/>
      <b>质量等级：</b>${getQualityLevelText(item.qualityLevel) || '-'}<br/>
      <b>信号质量：</b>${item.signalQuality?.toFixed(2) || '-'}<br/>
      <b>信噪比：</b>${item.snr?.toFixed(2) || '-'}<br/>
      <b>丢包率：</b>${item.packetLossRate?.toFixed(2) || '-'}%<br/>
      <b>延迟：</b>${item.latencyMs?.toFixed(0) || '-'}ms
    `);
    
    markers.push(marker);
    marker.addTo(map!);
  });
};

// 获取热量分布数据
const fetchHeatmapData = async () => {
  try {
    const res: any = await http.get("/monitoring/heatmap");
    if (res.success) {
      heatmapData.value = res.data || [];
      renderMarkers(res.data);
    }
  } catch (error) {
    console.error('获取热量分布数据失败:', error);
  }
};

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res: any = await http.get("/monitoring/statistics/quality-distribution");
    if (res.success) {
      statistics.value = res.data;
      updateCharts();
    }
  } catch (error) {
    console.error('获取统计数据失败:', error);
  }
};

// 获取地图数据
const fetchMapData = async () => {
  const res: any = await http.get("/monitoring/map");
  rows.value = res.data?.filter((item: any) => 
    item.signalQuality != null || item.snr != null || item.packetLossRate != null
  ) || [];
  pagination.value.total = rows.value.length;
  pagination.value.currentPage = 1;
};

const fetchData = async () => {
  loading.value = true;
  try {
    await Promise.all([fetchMapData(), fetchStatistics(), fetchHeatmapData()]);
  } finally {
    loading.value = false;
  }
};

const refresh = () => {
  fetchData();
};

const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val;
  pagination.value.currentPage = 1;
};

const handleCurrentChange = (val: number) => {
  pagination.value.currentPage = val;
};

const startRefresh = () => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer);
  }
  refreshTimer = window.setInterval(() => {
    fetchMapData();
  }, 10000);
};

const resizeMap = () => {
  if (!map) return;
  setTimeout(() => {
    map?.invalidateSize();
  }, 200);
};

const toggleFullscreen = async () => {
  if (!mapRef.value) return;
  try {
    if (!document.fullscreenElement) {
      await mapRef.value.requestFullscreen();
    } else {
      await document.exitFullscreen();
    }
    resizeMap();
  } catch (error) {
    console.error('全屏切换失败', error);
  }
};

const handleResize = () => {
  signalChart?.resize();
  healthChart?.resize();
  snrChart?.resize();
  packetLossChart?.resize();
  latencyChart?.resize();
};

onMounted(() => {
  initMap();
  initCharts();
  fetchData();
  startRefresh();
  window.addEventListener('resize', handleResize);
  document.addEventListener('fullscreenchange', resizeMap);
});

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer);
  }
  window.removeEventListener('resize', handleResize);
  document.removeEventListener('fullscreenchange', resizeMap);
});
</script>

<style scoped>
.page {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

.page-sub {
  color: #8c8c9a;
  margin-top: 4px;
  font-size: 13px;
}

.page-header :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}

.page-header :deep(.el-button + .el-button) {
  margin-left: 8px;
}

.metric-label {
  color: #8c8c9a;
  font-size: 13px;
  font-weight: 500;
}

.metric-value {
  font-size: 26px;
  font-weight: 700;
  margin: 10px 0;
  color: #26263a;
}

.metric-desc {
  color: #bfbfbf;
  font-size: 12px;
}

.chart-row {
  margin-top: 16px;
}

.chart-container {
  height: 280px;
  width: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.chart-container-small {
  height: 220px;
  width: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.map-card {
  margin-top: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.map-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f5;
  background: #fafbfc;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.legend {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #606266;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.legend .excellent { background: #52c41a; }
.legend .good { background: #1890ff; }
.legend .fair { background: #fa8c16; }
.legend .poor { background: #f5222d; }
.legend .critical { background: #a8071a; }

.map-container {
  height: 400px;
  width: 100%;
  border-radius: 0 0 12px 12px;
  overflow: hidden;
}

:deep(.map-container:fullscreen) {
  width: 100%;
  height: 100%;
  border-radius: 0;
  background: #000;
}

:deep(.map-container:fullscreen .leaflet-container) {
  width: 100%;
  height: 100%;
}

:deep(.status-marker-dot) {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.3);
}

.table-card {
  margin-top: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.dialog-header-info {
  margin-bottom: 16px;
}

:deep(.el-card) {
  border-radius: 12px;
  border: none;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f5;
}
</style>
