<template>
  <div class="review-products">
    <h2>Review Products</h2>
    <el-table :data="products" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="farmerId" label="Farmer ID" width="100" />
      <el-table-column label="Image" width="100">
        <template #default="scope">
          <el-image :src="scope.row.imageUrl" style="width: 50px; height: 50px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="price" label="Price" width="120" />
      <el-table-column prop="stock" label="Stock" width="100" />
      <el-table-column label="Actions" width="200">
        <template #default="scope">
          <el-button size="small" type="success" @click="updateStatus(scope.row.id, 'APPROVED')">Approve</el-button>
          <el-button size="small" type="danger" @click="updateStatus(scope.row.id, 'REJECTED')">Reject</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/axios'
import { ElMessage } from 'element-plus'

const products = ref([])
const loading = ref(false)

const fetchPendingProducts = async () => {
  loading.value = true
  products.value = await request.get('/api/products/pending')
  loading.value = false
}

const updateStatus = async (id, status) => {
  await request.put(`/api/products/${id}/status?status=${status}`)
  ElMessage.success(`Product ${status.toLowerCase()}`)
  fetchPendingProducts()
}

onMounted(() => {
  fetchPendingProducts()
})
</script>
