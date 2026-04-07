<template>
  <div class="user-management">
    <h2>User Management</h2>

    <div class="filter-section">
      <el-form :inline="true" class="search-form">
        <el-form-item label="Username">
          <el-input v-model="searchParams.username" placeholder="Search username" clearable @keyup.enter="fetchUsers" />
        </el-form-item>
        <el-form-item label="Role">
          <el-select v-model="searchParams.role" placeholder="All Roles" clearable style="width: 150px">
            <el-option label="CONSUMER" value="CONSUMER" />
            <el-option label="FARMER" value="FARMER" />
            <el-option label="ADMIN" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">Search</el-button>
          <el-button @click="resetSearch">Reset</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="users" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="Username" />
      <el-table-column prop="role" label="Role" width="120">
        <template #default="scope">
          <el-tag :type="getRoleType(scope.row.role)">{{ scope.row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="Created At" />
      <el-table-column label="Actions" width="120">
        <template #default="scope">
          <el-button size="small" type="danger" @click="deleteUser(scope.row.id)" :disabled="scope.row.id === userStore.userInfo.id">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const users = ref([])
const loading = ref(false)

const searchParams = reactive({
  username: '',
  role: ''
})

const getRoleType = (role) => {
  if (role === 'ADMIN') return 'danger'
  if (role === 'FARMER') return 'success'
  return 'info' // CONSUMER
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchParams.username) params.username = searchParams.username
    if (searchParams.role) params.role = searchParams.role

    users.value = await request.get('/api/users', { params })
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchParams.username = ''
  searchParams.role = ''
  fetchUsers()
}

const deleteUser = async (id) => {
  try {
    await request.delete('/api/users/' + id)
    ElMessage.success('User deleted')
    fetchUsers()
  } catch(e) {}
}

onMounted(() => {
  fetchUsers()
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
