<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">主站站点状态监控</div>
        <div class="page-sub">实时监控主站设备状态与性能指标</div>
      </div>
      <div>
        <el-button type="primary" @click="refresh">刷新</el-button>
      </div>
    </div>

    <!-- 设备概览栏 -->
    <el-card class="overview-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-label">设备名称</div>
            <div class="overview-value">{{ stationData.deviceName }}</div>
            <div class="overview-sub">{{ stationData.model }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="overview-item">
            <div class="overview-label">在线状态</div>
            <div class="overview-value">
              <el-tag :type="stationData.online ? 'success' : 'danger'" size="large">
                {{ stationData.online ? '在线' : '离线' }}
              </el-tag>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="overview-item">
            <div class="overview-label">网络连接</div>
            <div class="overview-value">{{ stationData.beam }} 波束</div>
            <div class="overview-sub">{{ stationData.networkType }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="overview-item">
            <div class="overview-label">端站位置</div>
            <div class="overview-value">{{ stationData.longitude }}°E</div>
            <div class="overview-sub">{{ stationData.latitude }}°N</div>
          </div>
        </el-col>
        <el-col :span="3">
          <div class="overview-item">
            <div class="overview-label">主站天气</div>
            <div class="overview-value">{{ stationData.masterWeather }}</div>
            <div class="overview-sub">{{ stationData.masterRain }}</div>
          </div>
        </el-col>
        <el-col :span="3">
          <div class="overview-item">
            <div class="overview-label">端站天气</div>
            <div class="overview-value">{{ stationData.slaveWeather }}</div>
            <div class="overview-sub">{{ stationData.slaveCloud }}</div>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 10px;">
        <el-col :span="12">
          <div class="overview-time">端站本地时间：{{ stationData.localTime }}</div>
        </el-col>
        <el-col :span="12">
          <div class="overview-time">状态最后更新时间：{{ stationData.updateTime }}</div>
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
      <el-row :gutter="20">
        <el-col :span="4">
          <div class="metric-box">
            <div class="metric-label">上传速度</div>
            <div class="metric-value green">{{ realtimeData.uploadSpeed }} <span class="metric-unit">Kbps</span></div>
            <div class="metric-time">采集于 {{ realtimeData.uploadTime }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="metric-box">
            <div class="metric-label">下载速度</div>
            <div class="metric-value green">{{ realtimeData.downloadSpeed }} <span class="metric-unit">Kbps</span></div>
            <div class="metric-time">采集于 {{ realtimeData.downloadTime }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="metric-box">
            <div class="metric-label">总速率</div>
            <div class="metric-value">{{ realtimeData.totalRate }} <span class="metric-unit">Kbps</span></div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="metric-box">
            <div class="metric-label">时延</div>
            <div class="metric-value">{{ realtimeData.delay }} <span class="metric-unit">ms</span></div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="metric-box">
            <div class="metric-label">丢包率</div>
            <div class="metric-value">{{ realtimeData.packetLoss }} <span class="metric-unit">%</span></div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="metric-box">
            <div class="metric-label">抖动</div>
            <div class="metric-value">{{ realtimeData.jitter }} <span class="metric-unit">ms</span></div>
            <div class="metric-time">采集于 {{ realtimeData.jitterTime }}</div>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <div class="cn-box">
            <div class="cn-label">发送端 C/N</div>
            <div class="cn-value">{{ realtimeData.cnTx }} dB</div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="cn-box">
            <div class="cn-label">接收端 C/N</div>
            <div class="cn-value">{{ realtimeData.cnRx }} dB</div>
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
              <el-option label="发送C/N" value="cn_tx" />
              <el-option label="接收C/N" value="cn_rx" />
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
            <el-select v-model="queryForm.granularity" placeholder="请选择" style="width: 120px;">
              <el-option label="全量" value="all" />
              <el-option label="2小时" value="2h" />
              <el-option label="1天" value="1d" />
              <el-option label="1周" value="1w" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="queryHistoryData">查询</el-button>
          </el-form-item>
        </el-form>
        
        <!-- 快速选择按钮 -->
        <div class="quick-select">
          <el-radio-group v-model="queryForm.quickRange" @change="handleQuickSelect">
            <el-radio-button label="2h">2小时</el-radio-button>
            <el-radio-button label="1d">1天</el-radio-button>
            <el-radio-button label="1w">1周</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 数据可视化图表 -->
      <div class="chart-container" v-loading="chartLoading">
        <div class="chart-header">
          <span>{{ metricLabel }}趋势</span>
          <span class="data-count">共 {{ chartData.length }} 个数据点</span>
        </div>
        <div ref="chartRef" class="chart"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import * as echarts from 'echarts';
import http from '../api/http';

const stationData = ref({
  deviceName: '凯睿星通-KeyCsatPS035-TDA200-250250',
  model: 'KRXT-PS035-TDA200-250250',
  online: true,
  beam: '9b（西安）',
  networkType: 'ORCA',
  longitude: 119.549,
  latitude: 25.705,
  masterWeather: '小雨',
  masterRain: '降雨量: 5mm',
  slaveWeather: '晴',
  slaveCloud: '少云',
  localTime: '2026-03-17 14:00',
  updateTime: '2026-03-17 14:00:18'
});

const realtimeData = ref({
  uploadSpeed: 0.00,
  downloadSpeed: 0.00,
  totalRate: 3.30,
  uploadTime: '14:00:16',
  downloadTime: '14:00:16',
  delay: 5.91,
  packetLoss: 0.00,
  jitter: 0.00,
  jitterTime: '14:00:21',
  cnTx: 3.30,
  cnRx: 3.30
});

const queryForm = ref({
  metric: 'network_rate',
  dateRange: [],
  granularity: '2h',
  quickRange: '2h'
});

const chartRef = ref<HTMLElement>();
const chartLoading = ref(false);
const chartData = ref<any[]>([]);

const metricLabel = computed(() => {
  const labels: Record<string, string> = {
    network_rate: '网络速率',
    online_status: '在线情况',
    cn_tx: '发送C/N',
    cn_rx: '接收C/N',
    delay: '时延',
    packet_loss: '丢包率'
  };
  return labels[queryForm.value.metric] || '网络速率';
});

let chartInstance: echarts.ECharts | null = null;

const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value);
    updateChart();
  }
};

const updateChart = () => {
  if (!chartInstance) return;
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const time = params[0]?.axisValue || '';
        let result = `${time}<br/>`;
        params.forEach((item: any) => {
          result += `${item.marker} ${item.seriesName}: ${item.value} Kbps<br/>`;
        });
        return result;
      }
    },
    legend: {
      data: ['上传速度', '下载速度', '总速率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: chartData.value.map(item => item.time)
    },
    yAxis: {
      type: 'value',
      name: 'Kbps'
    },
    series: [
      {
        name: '上传速度',
        type: 'line',
        data: chartData.value.map(item => item.upload),
        smooth: true,
        itemStyle: { color: '#409eff' }
      },
      {
        name: '下载速度',
        type: 'line',
        data: chartData.value.map(item => item.download),
        smooth: true,
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '总速率',
        type: 'line',
        data: chartData.value.map(item => item.total),
        smooth: true,
        itemStyle: { color: '#e6a23c' }
      }
    ]
  };
  
  chartInstance.setOption(option);
};

const fetchStationData = async () => {
  try {
    const res: any = await http.get('/station/monitoring');
    if (res.success && res.data) {
      stationData.value = { ...stationData.value, ...res.data.station };
      realtimeData.value = { ...realtimeData.value, ...res.data.realtime };
    }
  } catch (error) {
    console.error('获取主站数据失败:', error);
  }
};

const queryHistoryData = async () => {
  chartLoading.value = true;
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
    console.error('查询历史数据失败:', error);
  } finally {
    chartLoading.value = false;
  }
};

const handleQuickSelect = (value: string) => {
  queryForm.value.granularity = value;
  const now = new Date();
  let startTime = new Date();
  
  if (value === '2h') {
    startTime = new Date(now.getTime() - 2 * 60 * 60 * 1000);
  } else if (value === '1d') {
    startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000);
  } else if (value === '1w') {
    startTime = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
  }
  
  queryForm.value.dateRange = [
    startTime.toISOString().replace('T', ' ').substring(0, 19),
    now.toISOString().replace('T', ' ').substring(0, 19)
  ];
  
  queryHistoryData();
};

const refresh = () => {
  fetchStationData();
  queryHistoryData();
};

onMounted(() => {
  fetchStationData();
  handleQuickSelect('2h');
  setTimeout(initChart, 100);
  
  window.addEventListener('resize', () => {
    chartInstance?.resize();
  });
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
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  letter-spacing: -0.5px;
}

.page-sub {
  font-size: 13px;
  color: #8c8c9a;
  margin-top: 6px;
}

.overview-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.overview-card :deep(.el-card__body) {
  padding: 20px 24px;
}

.overview-item {
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fc 0%, #f0f2f5 100%);
  border-radius: 10px;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.overview-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.overview-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.overview-value {
  font-size: 15px;
  font-weight: 600;
  color: #26263a;
  line-height: 1.4;
}

.overview-sub {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
}

.overview-time {
  font-size: 13px;
  color: #8c8c9a;
  padding-left: 4px;
  border-left: 3px solid #409eff;
}

.realtime-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.realtime-card :deep(.el-card__header) {
  padding: 16px 24px;
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
  text-align: center;
  padding: 20px 12px;
  background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
  border-radius: 12px;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.metric-box:hover {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.12);
}

.metric-label {
  font-size: 13px;
  color: #8c8c9a;
  margin-bottom: 12px;
  font-weight: 500;
}

.metric-value {
  font-size: 26px;
  font-weight: 700;
  color: #26263a;
  letter-spacing: -0.5px;
}

.metric-value.green {
  color: #52c41a;
}

.metric-value.orange {
  color: #fa8c16;
}

.metric-value.blue {
  color: #1890ff;
}

.metric-unit {
  font-size: 13px;
  font-weight: 500;
  color: #8c8c9a;
}

.metric-time {
  font-size: 11px;
  color: #bfbfbf;
  margin-top: 8px;
}

.cn-box {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  border-radius: 12px;
  border: 1px solid #91d5ff;
}

.cn-label {
  font-size: 13px;
  color: #1890ff;
  margin-bottom: 10px;
  font-weight: 500;
}

.cn-value {
  font-size: 22px;
  font-weight: 700;
  color: #0050b3;
}

.history-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.history-card :deep(.el-card__header) {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f5;
  background: #fafbfc;
  border-radius: 12px 12px 0 0;
}

.query-panel {
  margin-bottom: 20px;
  padding: 20px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #f0f0f5;
}

.query-panel :deep(.el-form-item) {
  margin-bottom: 0;
}

.query-panel :deep(.el-select),
.query-panel :deep(.el-date-editor) {
  width: 100%;
}

.quick-select {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e8e8e8;
}

.quick-select :deep(.el-radio-button__inner) {
  font-size: 13px;
}

.chart-container {
  min-height: 380px;
  padding: 20px;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #f0f0f5;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
  color: #26263a;
}

.data-count {
  font-size: 12px;
  color: #8c8c9a;
  font-weight: 400;
}

.chart {
  width: 100%;
  height: 320px;
}
</style>
