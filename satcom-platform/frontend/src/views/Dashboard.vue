<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">态势总览</div>
        <div class="page-sub">关键指标与异常告警概览</div>
      </div>
      <el-button type="primary" @click="generateReport">生成日报</el-button>
    </div>

    <!-- KPI 指标卡片 -->
    <el-row :gutter="20" class="kpi">
      <el-col :span="6">
        <el-card class="kpi-card">
          <div class="kpi-icon online">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-title">在线终端</div>
            <div class="kpi-value">{{ statistics.onlineCount || 0 }}</div>
            <div class="kpi-desc">
              <span class="trend up">↑ {{ statistics.onlineChange || 0 }}</span>
              本周新增
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi-card">
          <div class="kpi-icon warning">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-title">异常告警</div>
            <div class="kpi-value">{{ statistics.alertCount || 0 }}</div>
            <div class="kpi-desc">
              <span class="processing">{{ statistics.processingCount || 0 }}</span>
              处理中
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi-card">
          <div class="kpi-icon dispatch">
            <el-icon><Guide /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-title">在途调度</div>
            <div class="kpi-value">{{ statistics.dispatchCount || 0 }}</div>
            <div class="kpi-desc">
              <span class="urgent">{{ statistics.urgentCount || 0 }}</span>
              紧急
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi-card">
          <div class="kpi-icon balance">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-title">余额预警</div>
            <div class="kpi-value">{{ statistics.balanceAlertCount || 0 }}</div>
            <div class="kpi-desc">
              <span class="balance-alert">需续费</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表和告警区域 -->
    <el-row :gutter="20" class="panels">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>信号质量趋势</span>
              <el-radio-group v-model="chartPeriod" size="small" @change="fetchChartData">
                <el-radio-button label="24h">24小时</el-radio-button>
                <el-radio-button label="7d">7天</el-radio-button>
                <el-radio-button label="30d">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="signalChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新告警</span>
              <el-button type="text" size="small" @click="viewAllAlerts">查看全部</el-button>
            </div>
          </template>
          <el-scrollbar height="280px">
            <el-timeline v-if="alerts.length > 0">
              <el-timeline-item
                v-for="alert in alerts"
                :key="alert.id"
                :timestamp="formatTime(alert.timestamp)"
                :type="getAlertType(alert.level)"
                :hollow="alert.level === 'INFO'"
              >
                <div class="alert-item">
                  <div class="alert-title">{{ alert.title }}</div>
                  <div class="alert-desc">{{ alert.description }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无告警" :image-size="60" />
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>

    <!-- 设备状态分布 -->
    <el-row :gutter="20" class="panels">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">设备健康状态分布</div>
          </template>
          <div ref="healthChartRef" class="chart-container-small"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">信号质量分布</div>
          </template>
          <div ref="qualityChartRef" class="chart-container-small"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { Monitor, Warning, Guide, Wallet } from '@element-plus/icons-vue';
import http from '../api/http';

const chartPeriod = ref('24h');
const signalChartRef = ref<HTMLElement>();
const healthChartRef = ref<HTMLElement>();
const qualityChartRef = ref<HTMLElement>();

let signalChart: echarts.ECharts | null = null;
let healthChart: echarts.ECharts | null = null;
let qualityChart: echarts.ECharts | null = null;

const statistics = ref({
  onlineCount: 0,
  onlineChange: 0,
  alertCount: 0,
  processingCount: 0,
  dispatchCount: 0,
  urgentCount: 0,
  balanceAlertCount: 0
});

const alerts = ref<any[]>([]);

const getAlertType = (level: string) => {
  const typeMap: Record<string, string> = {
    'DANGER': 'danger',
    'WARNING': 'warning',
    'INFO': 'info'
  };
  return typeMap[level] || 'info';
};

const formatTime = (time: string) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

const fetchStatistics = async () => {
  try {
    const res: any = await http.get('/monitoring/statistics/quality-distribution');
    if (res.success && res.data) {
      statistics.value = {
        onlineCount: res.data.onlineCount || 0,
        onlineChange: res.data.onlineChange || 0,
        alertCount: res.data.alertCount || 0,
        processingCount: res.data.processingCount || 0,
        dispatchCount: res.data.dispatchCount || 0,
        urgentCount: res.data.urgentCount || 0,
        balanceAlertCount: res.data.balanceAlertCount || 0
      };
    }
  } catch (error) {
    console.error('获取统计数据失败:', error);
  }
};

const fetchAlerts = async () => {
  try {
    const res: any = await http.get('/monitoring/devices-by-health', {
      params: { healthLevel: 'ALERT' }
    });
    if (res.success && res.data) {
      alerts.value = (res.data as any[]).slice(0, 5).map((item: any, index: number) => ({
        id: index,
        title: item.assetCode || `设备-${item.equipmentId}`,
        description: item.diagnosis || '设备异常',
        level: 'WARNING',
        timestamp: item.collectedAt || new Date().toISOString()
      }));
    }
  } catch (error) {
    console.error('获取告警失败:', error);
  }
};

const initCharts = () => {
  if (signalChartRef.value) {
    signalChart = echarts.init(signalChartRef.value);
  }
  if (healthChartRef.value) {
    healthChart = echarts.init(healthChartRef.value);
  }
  if (qualityChartRef.value) {
    qualityChart = echarts.init(qualityChartRef.value);
  }
};

const fetchChartData = async () => {
  try {
    const res: any = await http.get('/monitoring/heatmap');
    if (res.success && res.data) {
      updateSignalChart(res.data);
      updateHealthChart(res.data);
      updateQualityChart(res.data);
    }
  } catch (error) {
    console.error('获取图表数据失败:', error);
    generateMockChartData();
  }
};

const generateMockChartData = () => {
  const now = Date.now();
  const data: any[] = [];
  for (let i = 24; i >= 0; i--) {
    data.push({
      time: new Date(now - i * 3600000).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      quality: 70 + Math.random() * 25,
      snr: 10 + Math.random() * 15,
      latency: 50 + Math.random() * 100
    });
  }
  updateSignalChart(data);
  updateHealthChart(data);
  updateQualityChart(data);
};

const updateSignalChart = (data: any[]) => {
  if (!signalChart) return;
  
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['信号质量', '信噪比'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map((d: any) => d.time || d.collectedAt?.split('T')[1]?.substring(0, 5) || ''),
      axisLabel: { color: '#8c8c9a' }
    },
    yAxis: {
      type: 'value',
      name: 'dB',
      axisLabel: { color: '#8c8c9a' }
    },
    series: [
      {
        name: '信号质量',
        type: 'line',
        data: data.map((d: any) => d.quality || d.signalQuality || 0),
        smooth: true,
        itemStyle: { color: '#409eff' },
        areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
      },
      {
        name: '信噪比',
        type: 'line',
        data: data.map((d: any) => d.snr || d.snrValue || 0),
        smooth: true,
        itemStyle: { color: '#67c23a' }
      }
    ]
  };
  
  signalChart.setOption(option);
};

const updateHealthChart = (data: any[]) => {
  if (!healthChart) return;
  
  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        name: '健康状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}: {c} ({d}%)' },
        data: [
          { value: 45, name: '优秀', itemStyle: { color: '#52c41a' } },
          { value: 30, name: '良好', itemStyle: { color: '#1890ff' } },
          { value: 15, name: '一般', itemStyle: { color: '#fa8c16' } },
          { value: 10, name: '异常', itemStyle: { color: '#f5222d' } }
        ]
      }
    ]
  };
  
  healthChart.setOption(option);
};

const updateQualityChart = (data: any[]) => {
  if (!qualityChart) return;
  
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['丢包率', '延迟'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['优', '良', '中', '差'],
      axisLabel: { color: '#8c8c9a' }
    },
    yAxis: { type: 'value', axisLabel: { color: '#8c8c9a' } },
    series: [
      {
        name: '丢包率',
        type: 'bar',
        data: [5, 15, 25, 55],
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '延迟',
        type: 'bar',
        data: [10, 20, 30, 40],
        itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] }
      }
    ]
  };
  
  qualityChart.setOption(option);
};

const handleResize = () => {
  signalChart?.resize();
  healthChart?.resize();
  qualityChart?.resize();
};

const generateReport = () => {
  ElMessage.success('日报生成中，请稍候...');
};

const viewAllAlerts = () => {
  ElMessage.info('跳转到告警列表');
};

onMounted(() => {
  fetchStatistics();
  fetchAlerts();
  initCharts();
  fetchChartData();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  signalChart?.dispose();
  healthChart?.dispose();
  qualityChart?.dispose();
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

.kpi {
  margin-bottom: 16px;
}

.kpi-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: none;
  transition: all 0.3s ease;
}

.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.kpi-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 20px;
}

.kpi-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
}

.kpi-icon.online {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  color: #1890ff;
}

.kpi-icon.warning {
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  color: #fa8c16;
}

.kpi-icon.dispatch {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #52c41a;
}

.kpi-icon.balance {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  color: #f5222d;
}

.kpi-content {
  flex: 1;
}

.kpi-title {
  color: #8c8c9a;
  font-size: 13px;
  font-weight: 500;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  margin: 8px 0 4px;
  color: #26263a;
}

.kpi-desc {
  color: #8c8c9a;
  font-size: 12px;
}

.kpi-desc .trend.up {
  color: #52c41a;
  margin-right: 4px;
}

.kpi-desc .processing {
  color: #fa8c16;
  margin-right: 4px;
}

.kpi-desc .urgent {
  color: #f5222d;
  margin-right: 4px;
}

.kpi-desc .balance-alert {
  color: #f5222d;
}

.panels {
  margin-top: 16px;
}

.panels .el-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: none;
}

.panels :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f5;
  background: #fafbfc;
  border-radius: 12px 12px 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: #26263a;
}

.chart-container {
  height: 280px;
  width: 100%;
}

.chart-container-small {
  height: 220px;
  width: 100%;
}

.alert-item {
  padding: 4px 0;
}

.alert-title {
  font-weight: 500;
  color: #26263a;
  font-size: 13px;
}

.alert-desc {
  color: #8c8c9a;
  font-size: 12px;
  margin-top: 4px;
}

:deep(.el-timeline-item__content) {
  padding-bottom: 8px;
}
</style>
