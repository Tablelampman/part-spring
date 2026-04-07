<template>
  <div class="review-products">
    <h2>Review Products</h2>

    <div class="filter-section">
      <el-form :inline="true" class="search-form">
        <el-form-item label="Product Name">
          <el-input v-model="searchParams.name" placeholder="Search product name" clearable @keyup.enter="fetchProducts" />
        </el-form-item>
        <el-form-item label="Farmer Name">
          <el-input v-model="searchParams.farmerName" placeholder="Search farmer username" clearable @keyup.enter="fetchProducts" />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="searchParams.status" placeholder="All Status" clearable style="width: 150px">
            <el-option label="PENDING" value="PENDING" />
            <el-option label="APPROVED" value="APPROVED" />
            <el-option label="REJECTED" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchProducts">Search</el-button>
          <el-button @click="resetSearch">Reset</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="products" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="farmerId" label="Farmer ID" width="100" />
      <el-table-column label="Image" width="100">
        <template #default="scope">
          <el-image :src="scope.row.imageUrl" style="width: 50px; height: 50px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="category" label="Category" width="120" />
      <el-table-column prop="price" label="Price" width="120" />
      <el-table-column prop="stock" label="Stock" width="100" />
      <el-table-column prop="status" label="Status" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="200">
        <template #default="scope">
          <el-button size="small" type="success" @click="updateStatus(scope.row.id, 'APPROVED')" :disabled="scope.row.status === 'APPROVED'">Approve</el-button>
          <el-button size="small" type="danger" @click="updateStatus(scope.row.id, 'REJECTED')" :disabled="scope.row.status === 'REJECTED'">Reject</el-button>
          <el-button size="small" type="warning" @click="deleteProduct(scope.row.id)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/axios'
import { ElMessage } from 'element-plus'

const products = ref([])
const loading = ref(false)

const searchParams = reactive({
  name: '',
  farmerName: '',
  status: 'PENDING' // default show pending
})

const getStatusType = (status) => {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning' // PENDING
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchParams.name) params.name = searchParams.name
    if (searchParams.farmerName) params.farmerName = searchParams.farmerName
    if (searchParams.status) params.status = searchParams.status

    // We updated backend to use /admin/list for all filtered admin products
    products.value = await request.get('/api/products/admin/list', { params })
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchParams.name = ''
  searchParams.farmerName = ''
  searchParams.status = 'PENDING'
  fetchProducts()
}

const updateStatus = async (id, status) => {
  await request.put(`/api/products/${id}/status?status=${status}`)
  ElMessage.success(`Product ${status.toLowerCase()}`)
  fetchProducts()
}


const deleteProduct = async (id) => {
  try {
    await request.delete('/api/products/' + id)
    ElMessage.success('Product deleted')
    fetchProducts()
  } catch(e) {}
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.filter-section {
  background: #fff;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.05);
}
</style>
