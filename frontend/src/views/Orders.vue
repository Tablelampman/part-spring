<template>
  <div class="orders">
    <h2>{{ isFarmer ? $t('order.farmerOrders') : $t('order.myOrders') }}</h2>

    <div v-if="!isFarmer">
      <el-card v-for="order in consumerOrders" :key="order.id" style="margin-bottom: 20px;">
        <template #header>
          <div class="card-header" style="display: flex; justify-content: space-between;">
            <span>{{ $t('order.orderId', { id: order.id }) }}</span>
            <span>{{ $t('order.total') }}: ￥{{ order.totalAmount }} - <el-tag>{{ order.status }}</el-tag></span>
          </div>
        </template>
        <div v-for="item in order.items" :key="item.id" style="display: flex; align-items: center; margin-bottom: 10px;">
          <img :src="item.product.imageUrl" style="width: 50px; height: 50px; object-fit: cover; margin-right: 10px;" v-if="item.product"/>
          <div>
            <div>{{ item.product ? item.product.name : 'Unknown Product' }}</div>
            <div style="color: #999; font-size: 12px;">{{ $t('order.qty') }}: {{ item.quantity }} × ￥{{ item.unitPrice }}</div>
          </div>
        </div>
        <div style="margin-top: 15px; text-align: right;">
          <el-button type="success" @click="simulatePayment(order.id)" v-if="order.status === 'PENDING_PAYMENT'">{{ $t('order.simulatePayment') }}</el-button>
          <el-button type="danger" @click="deleteOrder(order.id)">{{ $t('order.deleteOrder') }}</el-button>
        </div>
      </el-card>
    </div>

    <div v-else>
      <el-table :data="farmerOrderItems" style="width: 100%">
        <el-table-column prop="orderId" :label="$t('order.orderId', { id: '' })" width="100" />
        <el-table-column :label="$t('cart.product')">
          <template #default="scope">
            {{ scope.row.product.name }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" :label="$t('order.qty')" width="80" />
        <el-table-column :label="$t('order.total')">
          <template #default="scope">
            ￥{{ (scope.row.quantity * scope.row.unitPrice).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/api/axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const userStore = useUserStore()
const consumerOrders = ref([])
const farmerOrderItems = ref([])

const isFarmer = computed(() => userStore.userInfo.role === 'FARMER')

const fetchOrders = async () => {
  if (isFarmer.value) {
    farmerOrderItems.value = await request.get('/api/orders/farmer')
  } else {
    consumerOrders.value = await request.get('/api/orders/my')
  }
}

const simulatePayment = async (orderId) => {
  await request.post(`/api/orders/${orderId}/pay`)
  ElMessage.success(t('order.paymentSuccess'))
  fetchOrders()
}

const deleteOrder = async (orderId) => {
  try {
    await request.delete('/api/orders/' + orderId)
    ElMessage.success(t('order.deleted'))
    fetchOrders()
  } catch(e) {}
}

onMounted(() => {
  fetchOrders()
})
</script>
