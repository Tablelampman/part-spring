<template>
  <div class="user-management">
    <h2>{{ $t('users.title') }}</h2>

    <div class="filter-section">
      <el-form :inline="true" class="search-form">
        <el-form-item :label="$t('auth.username')">
          <el-input v-model="searchParams.username" :placeholder="$t('users.searchUsername')" clearable @keyup.enter="fetchUsers" />
        </el-form-item>
        <el-form-item :label="$t('auth.role')">
          <el-select v-model="searchParams.role" :placeholder="$t('users.allRoles')" clearable style="width: 150px">
            <el-option :label="$t('roles.consumer')" value="CONSUMER" />
            <el-option :label="$t('roles.farmer')" value="FARMER" />
            <el-option :label="$t('roles.admin')" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">{{ $t('common.search') }}</el-button>
          <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="users" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" :label="$t('common.id')" width="80" />
      <el-table-column prop="username" :label="$t('auth.username')" />
      <el-table-column prop="role" :label="$t('auth.role')" width="120">
        <template #default="scope">
          <el-tag :type="getRoleType(scope.row.role)">{{ $t('roles.' + scope.row.role.toLowerCase()) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" :label="$t('common.createdAt')">
        <template #default="scope">
          {{ new Date(scope.row.createdAt).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.actions')" width="120">
        <template #default="scope">
          <el-button size="small" type="danger" @click="deleteUser(scope.row.id)" :disabled="scope.row.id === userStore.userInfo.id">{{ $t('common.delete') }}</el-button>
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
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
    ElMessage.success(t('users.deleted'))
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
