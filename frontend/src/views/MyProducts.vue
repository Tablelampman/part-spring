<template>
  <div class="my-products">
    <div class="header-action">
      <h2>My Products</h2>
      <el-button type="primary" @click="dialogVisible = true">Add New Product</el-button>
    </div>

    <div class="filter-section">
      <el-form :inline="true" class="search-form">
        <el-form-item label="Product Name">
          <el-input v-model="searchParams.name" placeholder="Search product name" clearable @keyup.enter="fetchProducts" />
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
      <el-table-column label="Actions" width="120">
        <template #default="scope">
          <el-button size="small" type="danger" @click="deleteProduct(scope.row.id)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Add Product Dialog -->
    <el-dialog v-model="dialogVisible" title="Add Product" width="50%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="Name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Category">
          <el-select v-model="form.category" placeholder="Select Category" style="width: 100%">
            <el-option label="Fruits" value="Fruits" />
            <el-option label="Vegetables" value="Vegetables" />
            <el-option label="Meat & Poultry" value="Meat" />
            <el-option label="Dairy & Eggs" value="Dairy" />
            <el-option label="Grains" value="Grains" />
            <el-option label="Others" value="Others" />
          </el-select>
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="Price">
          <el-input-number v-model="form.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="Stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="Image">
          <el-upload
            class="avatar-uploader"
            action="/api/products/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitProduct">Confirm</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/api/axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const products = ref([])
const loading = ref(false)
const dialogVisible = ref(false)

const searchParams = reactive({
  name: '',
  status: ''
})

const uploadHeaders = computed(() => {
  return { Authorization: 'Bearer ' + userStore.token }
})

const form = reactive({
  name: '',
  category: 'Others',
  description: '',
  price: 0,
  stock: 0,
  imageUrl: ''
})

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchParams.name) params.name = searchParams.name
    if (searchParams.status) params.status = searchParams.status

    products.value = await request.get('/api/products/my', { params })
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchParams.name = ''
  searchParams.status = ''
  fetchProducts()
}

const getStatusType = (status) => {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning' // PENDING
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.imageUrl = response.data
    ElMessage.success('Upload success')
  } else {
    ElMessage.error(response.message)
  }
}

const submitProduct = async () => {
  await request.post('/api/products', form)
  ElMessage.success('Product submitted for review')
  dialogVisible.value = false
  // reset form
  form.name = ''
  form.category = 'Others'
  form.description = ''
  form.price = 0
  form.stock = 0
  form.imageUrl = ''
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
.header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.filter-section {
  background: #fff;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.05);
}
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  border: 1px dashed #d9d9d9;
}
</style>
