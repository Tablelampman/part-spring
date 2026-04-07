<template>
  <div class="dashboard" v-loading="loading">
    <h2>Dashboard</h2>

    <template v-if="role === 'ADMIN'">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">Total Users</div>
            <div class="stat-value">{{ adminStats.totalUsers || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">Total Products</div>
            <div class="stat-value">{{ adminStats.totalProducts || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">Total Orders</div>
            <div class="stat-value">{{ adminStats.totalOrders || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">Today's Revenue</div>
            <div class="stat-value price">￥{{ adminStats.todayRevenue || 0 }}</div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <template v-if="role === 'FARMER'">
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="12">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">Total Sales Quantity</div>
            <div class="stat-value">{{ farmerStats.totalSalesQuantity || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">Total Revenue</div>
            <div class="stat-value price">￥{{ farmerStats.totalRevenue || 0 }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>Last 7 Days Sales Trend</span>
          </div>
        </template>
        <v-chart class="chart" :option="chartOption" autoresize />
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import request from '@/api/axios'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
} from 'echarts/components'

use([
  CanvasRenderer,
  LineChart,
  BarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
])

const userStore = useUserStore()
const role = computed(() => userStore.userInfo.role)
const loading = ref(false)

const adminStats = ref({})
const farmerStats = ref({})

const chartOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['Quantity', 'Revenue (￥)'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: [] },
  yAxis: [
    { type: 'value', name: 'Quantity' },
    { type: 'value', name: 'Revenue', axisLabel: { formatter: '￥{value}' } }
  ],
  series: [
    { name: 'Quantity', type: 'bar', data: [] },
    { name: 'Revenue (￥)', type: 'line', yAxisIndex: 1, data: [] }
  ]
})

const fetchAdminStats = async () => {
  const res = await request.get('/api/stats/admin')
  adminStats.value = res
}

const fetchFarmerStats = async () => {
  const res = await request.get('/api/stats/farmer')
  farmerStats.value = res

  if (res.recentSales && res.recentSales.length > 0) {
    const dates = res.recentSales.map(item => item.date)
    const quantities = res.recentSales.map(item => item.quantity)
    const revenues = res.recentSales.map(item => item.revenue)

    chartOption.value.xAxis.data = dates
    chartOption.value.series[0].data = quantities
    chartOption.value.series[1].data = revenues
  }
}

onMounted(async () => {
  loading.value = true
  try {
    if (role.value === 'ADMIN') {
      await fetchAdminStats()
    } else if (role.value === 'FARMER') {
      await fetchFarmerStats()
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard {
  padding: 10px;
}
.stat-card {
  text-align: center;
  padding: 20px 0;
}
.stat-title {
  color: #909399;
  font-size: 16px;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.price {
  color: #f56c6c;
}
.chart {
  height: 400px;
}
</style>
