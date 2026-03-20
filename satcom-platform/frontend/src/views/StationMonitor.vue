<template>
  <div class="page">
    <!-- 设备概览栏 -->
    <el-card class="overview-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <div class="overview-main">
            <div class="device-name">{{ stationInfo.name }}</div>
            <div class="device-model">型号：{{ stationInfo.model }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="overview-item">
            <div class="overview-label">在线状态</div>
            <div class="overview-value">
              <el-tag :type="stationInfo.online ? 'success' : 'danger'" size="large">
                {{ stationInfo.online ? '在线' : '离线' }}
              </el-tag>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="overview-item">
            <div class="overview-label">网络连接</div>
            <div class="overview-value">{{ stationInfo.beam }} · {{ stationInfo.networkType }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="overview-item">
            <div class="overview-label">端站位置</div>
            <div class="overview-value">{{ stationInfo.longitude }}°E / {{ stationInfo.latitude }}°N</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="overview-item">
            <div class="overview-label">天气</div>
            <div class="overview-value weather">
              <span>主站({{ stationInfo.masterWeather }})</span>
              <span>端站({{ stationInfo.terminalWeather }})</span>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" align="middle" style="margin-top: 16px;">
        <el-col :span="8">
          <div class="overview-item">
            <div class="overview-label">端站本地时间</div>
            <div class="overview-value time">{{ stationInfo.terminalTime }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="overview-item">
            <div class="overview-label">状态最后更新时间</div>
            <div class="overview-value time">{{ stationInfo.lastUpdate }}</div>
          </div>
        </el-col>
        <el-col :span="8" style="text-align: right;">
          <el-link type="primary" :underline="true" class="archive-link">
            打开端站档案
          </el-link>
        </el-col>
      </el-row>
    </el-card>

    <!-- 实时指标数据面板 -->
    <el-card class="realtime-card">
      <template #header>
        <div class="card-header">
          <span>实时指标数据</span>
        </div>
      </template>
      
      <!-- 核心速率指标 -->
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="metric-box speed">
            <div class="metric-title">上传速度</div>
            <div class="metric-value green">{{ realtimeData.uploadSpeed.toFixed(2) }}</div>
            <div class="metric-unit">Kbps</div>
            <div class="metric-time">采集于 {{ realtimeData.uploadTime }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="metric-box speed">
            <div class="metric-title">下载速度</div>
            <div class="metric-value green">{{ realtimeData.downloadSpeed.toFixed(2) }}</div>
            <div class="metric-unit">Kbps</div>
            <div class="metric-time">采集于 {{ realtimeData.downloadTime }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="metric-box speed total">
            <div class="metric-title">总速率</div>
            <div class="metric-value green">{{ realtimeData.totalRate.toFixed(2) }}</div>
            <div class="metric-unit">Kbps</div>
          </div>
        </el-col>
      </el-row>

      <!-- 网络质量参数 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="6">
          <div class="metric-box-small">
            <div class="metric-title">时延</div>
            <div class="metric-value">{{ realtimeData.delay.toFixed(2) }}</div>
            <div class="metric-unit">ms</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-box-small">
            <div class="metric-title">丢包率</div>
            <div class="metric-value" :class="{ 'warning': realtimeData.packetLoss > 0 }">
              {{ realtimeData.packetLoss.toFixed(2) }}
            </div>
            <div class="metric-unit">%</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-box-small">
            <div class="metric-title">发送C/N</div>
            <div class="metric-value">{{ realtimeData.txCN.toFixed(2) }}</div>
            <div class="metric-unit">dB</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-box-small">
            <div class="metric-title">抖动</div>
            <div class="metric-value">{{ realtimeData.jitter.toFixed(2) }}</div>
            <div class="metric-unit">ms</div>
            <div class="metric-time">采集于 {{ realtimeData.jitterTime }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 历史指标数据查询与图表区 -->
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span>历史指标数据</span>
        </div>
      </template>
      
      <!-- 查询控制面板 -->
      <div class="query-panel">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="指标选择">
            <el-select v-model="queryForm.metric" placeholder="请选择指标" style="width: 150px;">
              <el-option label="网络速率" value="network_rate" />
              <el-option label="在线情况" value="online_status" />
              <el-option label="发送C/N" value="tx_cn" />
              <el-option label="接收C/N" value="rx_cn" />
              <el-option label="时延" value="delay" />
              <el-option label="丢包率" value="packet_loss" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="queryForm.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="数据粒度">
            <el-radio-group v-model="queryForm.granularity">
              <el-radio-button label="all">全量</el-radio-button>
              <el-radio-button label="2h">2小时</el-radio-button>
              <el-radio-button label="1d">1天</el-radio-button>
              <el-radio-button label="1w">1周</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="queryHistoryData">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据可视化图表 -->
      <div class="chart-wrapper">
        <div class="chart-container" ref="chartRef"></div>
        <div class="chart-zoom-hint">
          <el-icon><i-ep-zoom-in /></el-icon>
          可缩放查看详细数据
        </div>
      </div>
      
      <div class="chart-footer">
        <span class="data-points">共 {{ chartData.length }} 个数据点</span>
        <span class="time-range" v-if="queryForm.granularity !== 'all'">
          当前视图：{{ queryForm.granularity === '2h' ? '2小时' : queryForm.granularity === '1d' ? '1天' : '1周' }}
        </span>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';
import http from '../api/http';

const chartRef = ref<HTMLElement>();
let chartInstance: echarts.ECharts | null = null;

// 设备信息
const stationInfo = ref({
  name: '凯睿星通-KeyCsatPS035-TDA200-250250',
  model: 'KRXT-PS035-TDA200-250250',
  online: true,
  beam: '9b（西安）',
  networkType: 'ORCA',
  longitude: 119.549,
  latitude: 25.705,
  masterWeather: '西安：小雨',
  terminalWeather: '晴，少云',
  terminalTime: '2026-03-17 14:00',
  lastUpdate: '2026-03-17 14:00:18'
});

// 实时数据
const realtimeData = ref({
  uploadSpeed: 0,
  uploadTime: '14:00:16',
  downloadSpeed: 0,
  downloadTime: '14:00:16',
  totalRate: 3.30,
  delay: 5.91,
  packetLoss: 0,
  txCN: 3.30,
  rxCN: 3.30,
  jitter: 0,
  jitterTime: '14:00:21'
});

// 查询表单
const queryForm = ref({
  metric: 'network_rate',
  dateRange: [] as string[],
  granularity: '2h'
});

// 图表数据
const chartData = ref<any[]>([]);

// 当前指标对应的Y轴名称
const metricYAxisName: Record<string, string> = {
  network_rate: 'Kbps',
  tx_cn: 'dB',
  rx_cn: 'dB',
  delay: 'ms',
  packet_loss: '%',
  online_status: ''
};

// 当前指标对应的系列数据
const getSeriesData = (metric: string) => {
  switch (metric) {
    case 'network_rate':
      return [
        { name: '上传速度', key: 'uploadSpeed', color: '#409eff' },
        { name: '下载速度', key: 'downloadSpeed', color: '#67c23a' },
        { name: '总速率', key: 'totalRate', color: '#e6a23c' }
      ];
    case 'tx_cn':
      return [{ name: '发送C/N', key: 'txCN', color: '#409eff' }];
    case 'rx_cn':
      return [{ name: '接收C/N', key: 'rxCN', color: '#67c23a' }];
    case 'delay':
      return [{ name: '时延', key: 'delay', color: '#909399' }];
    case 'packet_loss':
      return [{ name: '丢包率', key: 'packetLoss', color: '#f56c6c' }];
    case 'online_status':
      return [{ name: '在线状态', key: 'online', color: '#67c23a' }];
    default:
      return [];
  }
};

const initChart = () => {
  if (!chartRef.value) return;
  
  chartInstance = echarts.init(chartRef.value);
  updateChart();
};

const updateChart = () => {
  if (!chartInstance) return;

  const seriesConfig = getSeriesData(queryForm.value.metric);
  const yAxisName = metricYAxisName[queryForm.value.metric] || 'Kbps';

  const series = seriesConfig.map(config => ({
    name: config.name,
    type: 'line',
    smooth: true,
    data: chartData.value.map(item => [item.time, item[config.key]]),
    itemStyle: { color: config.color },
    areaStyle: config.color.includes('409') || config.color.includes('67c') ? 
      { color: `${config.color}1A` } : undefined,
    connectNulls: true
  }));

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const time = params[0]?.axisValue || '';
        let result = `<div style="font-weight: 500;">时间: ${time}</div>`;
        params.forEach((item: any) => {
          const value = item.value?.[1];
          const unit = yAxisName;
          result += `<div>${item.seriesName}: ${value !== undefined && value !== null ? Number(value).toFixed(2) : '-'} ${unit}</div>`;
        });
        return result;
      }
    },
    legend: {
      data: seriesConfig.map(s => s.name),
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'time',
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: yAxisName,
      min: 0,
      nameTextStyle: {
        padding: [0, 0, 0, 40]
      }
    },
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        type: 'slider',
        start: 0,
        end: 100,
        height: 20,
        bottom: 40
      }
    ],
    series
  };

  chartInstance.setOption(option, true);
};

const fetchRealtimeData = async () => {
  try {
    const res: any = await http.get('/station/realtime');
    if (res.success) {
      realtimeData.value = res.data;
    }
  } catch (error) {
    console.error('获取实时数据失败:', error);
  }
};

const fetchStationInfo = async () => {
  try {
    const res: any = await http.get('/station/info');
    if (res.success) {
      stationInfo.value = res.data;
    }
  } catch (error) {
    console.error('获取设备信息失败:', error);
  }
};

const queryHistoryData = async () => {
  try {
    const params = {
      metric: queryForm.value.metric,
      startTime: queryForm.value.dateRange?.[0],
      endTime: queryForm.value.dateRange?.[1],
      granularity: queryForm.value.granularity
    };
    const res: any = await http.get('/station/history', { params });
    if (res.success) {
      chartData.value = res.data || [];
      updateChart();
    }
  } catch (error) {
    ElMessage.error('查询历史数据失败');
  }
};

const handleResize = () => {
  chartInstance?.resize();
};

onMounted(() => {
  nextTick(() => {
    initChart();
  });
  fetchStationInfo();
  fetchRealtimeData();
  queryHistoryData();
  
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});

watch(() => queryForm.value.metric, () => {
  queryHistoryData();
});

watch(() => queryForm.value.granularity, () => {
  queryHistoryData();
});
</script>

<style scoped>
.page {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.overview-card {
  margin-bottom: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.overview-main {
  padding: 8px 0;
}

.device-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.device-model {
  font-size: 13px;
  color: #8c8c9a;
  margin-top: 4px;
}

.overview-item {
  padding: 12px 16px;
  background: #fafbfc;
  border-radius: 10px;
}

.overview-label {
  font-size: 12px;
  color: #8c8c9a;
  margin-bottom: 6px;
  font-weight: 500;
}

.overview-value {
  font-size: 14px;
  font-weight: 500;
  color: #26263a;
}

.overview-value.time {
  font-family: 'Monaco', 'Consolas', monospace;
}

.overview-value.weather {
  display: flex;
  flex-direction: column;
  font-size: 12px;
}

.archive-link {
  font-size: 14px;
  font-weight: 500;
}

.realtime-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.realtime-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f5;
  background: #fafbfc;
  border-radius: 12px 12px 0 0;
}

.card-header {
  font-size: 15px;
  font-weight: 600;
  color: #26263a;
}

.metric-box {
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  color: #fff;
  transition: all 0.3s ease;
}

.metric-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.metric-box.speed {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.metric-box.speed.total {
  background: linear-gradient(135deg, #0f3460 0%, #16213e 100%);
}

.metric-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
}

.metric-value.green {
  color: #52c41a;
}

.metric-unit {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.metric-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 8px;
}

.metric-box-small {
  padding: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
  border-radius: 12px;
  text-align: center;
  border: 1px solid #f0f0f5;
  transition: all 0.3s ease;
}

.metric-box-small:hover {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.1);
}

.metric-box-small .metric-title {
  color: #8c8c9a;
}

.metric-box-small .metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #26263a;
}

.metric-box-small .metric-value.warning {
  color: #f5222d;
}

.metric-box-small .metric-unit {
  color: #8c8c9a;
  font-size: 12px;
}

.metric-box-small .metric-time {
  color: #bfbfbf;
  font-size: 11px;
  margin-top: 4px;
}

.history-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.history-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f5;
  background: #fafbfc;
  border-radius: 12px 12px 0 0;
}

.query-panel {
  margin-bottom: 16px;
  padding: 20px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #f0f0f5;
}

.query-panel :deep(.el-form-item) {
  margin-bottom: 0;
}

.query-panel :deep(.el-button) {
  border-radius: 8px;
}

.chart-wrapper {
  position: relative;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.chart-zoom-hint {
  position: absolute;
  top: 10px;
  right: 30px;
  color: #8c8c9a;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.chart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding: 0 10px;
}

.data-points {
  font-size: 13px;
  color: #8c8c9a;
}

.time-range {
  font-size: 13px;
  color: #1890ff;
  font-weight: 500;
}

:deep(.el-card) {
  border-radius: 12px;
  border: none;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-button) {
  border-radius: 8px;
}
</style>
