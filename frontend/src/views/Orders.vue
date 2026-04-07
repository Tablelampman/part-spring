<template>
  <div class="orders">
    <h2>{{ isFarmer ? 'Orders for My Products' : 'My Orders' }}</h2>

    <div v-if="!isFarmer">
      <el-card v-for="order in consumerOrders" :key="order.id" style="margin-bottom: 20px;">
        <template #header>
          <div class="card-header" style="display: flex; justify-content: space-between;">
            <span>Order #{{ order.id }}</span>
            <span>Total: ￥{{ order.totalAmount }} - <el-tag>{{ order.status }}</el-tag></span>
          </div>
        </template>
        <div v-for="item in order.items" :key="item.id" style="display: flex; align-items: center; margin-bottom: 10px;">
          <img :src="item.product.imageUrl" style="width: 50px; height: 50px; object-fit: cover; margin-right: 10px;" v-if="item.product"/>
          <div>
            <div>{{ item.product ? item.product.name : 'Unknown Product' }}</div>
            <div style="color: #999; font-size: 12px;">Qty: {{ item.quantity }} × ￥{{ item.unitPrice }}</div>
          </div>
        </div>

        <div style="margin-top: 15px; text-align: right;">
          <el-button type="success" @click="simulatePayment(order.id)" v-if="order.status === 'PENDING_PAYMENT'">Simulate Payment</el-button>
          <el-button type="danger" @click="deleteOrder(order.id)">Delete Order</el-button>
        </div>
      </el-card>
    </div>

    <div v-else>
      <el-table :data="farmerOrderItems" style="width: 100%">
        <el-table-column prop="orderId" label="Order ID" width="100" />
        <el-table-column label="Product">
          <template #default="scope">
            {{ scope.row.product.name }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="Qty" width="80" />
        <el-table-column label="Total">
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
  ElMessage.success('Payment successful!')
  fetchOrders()
}


const deleteOrder = async (orderId) => {
  try {
    await request.delete('/api/orders/' + orderId)
    ElMessage.success('Order deleted')
    fetchOrders()
  } catch(e) {}
}

onMounted(() => {
  fetchOrders()
})
</script>
