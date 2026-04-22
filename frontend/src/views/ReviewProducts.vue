<template>
  <div class="review-products">
    <h2>{{ $t('nav.reviewProducts') }}</h2>

    <div class="filter-section">
      <el-form :inline="true" class="search-form">
        <el-form-item :label="$t('product.productName')">
          <el-input v-model="searchParams.name" :placeholder="$t('product.productName')" clearable @keyup.enter="fetchProducts" />
        </el-form-item>
        <el-form-item :label="$t('product.farmerName')">
          <el-input v-model="searchParams.farmerName" :placeholder="$t('product.farmerName')" clearable @keyup.enter="fetchProducts" />
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-select v-model="searchParams.status" :placeholder="$t('common.all')" clearable style="width: 150px">
            <el-option label="PENDING" value="PENDING" />
            <el-option label="APPROVED" value="APPROVED" />
            <el-option label="REJECTED" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchProducts">{{ $t('common.search') }}</el-button>
          <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="products" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" :label="$t('common.id')" width="80" />
      <el-table-column prop="farmerId" :label="$t('product.farmerId')" width="100" />
      <el-table-column :label="$t('product.image')" width="100">
        <template #default="scope">
          <el-image :src="scope.row.imageUrl" style="width: 50px; height: 50px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="name" :label="$t('product.productName')" />
      <el-table-column prop="category" :label="$t('product.category')" width="120">
        <template #default="scope">
          {{ getCategoryLabel(scope.row.category) }}
        </template>
      </el-table-column>
      <el-table-column prop="price" :label="$t('product.price')" width="120" />
      <el-table-column prop="stock" :label="$t('product.stock')" width="100" />
      <el-table-column prop="status" :label="$t('common.status')" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.actions')" width="200">
        <template #default="scope">
          <el-button size="small" type="success" @click="updateStatus(scope.row.id, 'APPROVED')" :disabled="scope.row.status === 'APPROVED'">{{ $t('common.approve') }}</el-button>
          <el-button size="small" type="danger" @click="updateStatus(scope.row.id, 'REJECTED')" :disabled="scope.row.status === 'REJECTED'">{{ $t('common.reject') }}</el-button>
          <el-button size="small" type="warning" @click="deleteProduct(scope.row.id)">{{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/axios'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const products = ref([])
const loading = ref(false)

const searchParams = reactive({
  name: '',
  farmerName: '',
  status: 'PENDING'
})

const getCategoryLabel = (category) => {
  if (!category || category === 'Others') return t('categories.others')
  if (category === 'Fruits') return t('categories.fruits')
  if (category === 'Vegetables') return t('categories.vegetables')
  if (category === 'Meat') return t('categories.meat')
  if (category === 'Dairy') return t('categories.dairy')
  if (category === 'Grains') return t('categories.grains')
  return category
}

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
  ElMessage.success(t('common.success'))
  fetchProducts()
}

const deleteProduct = async (id) => {
  try {
    await request.delete('/api/products/' + id)
    ElMessage.success(t('product.deleted'))
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
