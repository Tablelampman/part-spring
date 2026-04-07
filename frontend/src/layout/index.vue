<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">AgriSales Platform</div>
      <div class="nav-menu">
        <el-menu mode="horizontal" :router="true" :default-active="$route.path">
          <el-menu-item index="/">Home (Shop)</el-menu-item>
          <el-menu-item index="/dashboard" v-if="userRole === 'ADMIN' || userRole === 'FARMER'">Dashboard</el-menu-item>
          <el-menu-item index="/cart" v-if="userRole === 'CONSUMER'">Cart</el-menu-item>
          <el-menu-item index="/orders" v-if="userRole === 'CONSUMER' || userRole === 'FARMER'">Orders</el-menu-item>
          <el-menu-item index="/my-products" v-if="userRole === 'FARMER'">My Products</el-menu-item>
          <el-menu-item index="/review-products" v-if="userRole === 'ADMIN'">Review Products</el-menu-item>
        </el-menu>
      </div>
      <div class="user-info">
        <template v-if="isLoggedIn">
          <span>{{ username }} ({{ userRole }})</span>
          <el-button type="danger" size="small" @click="handleLogout" style="margin-left: 15px">Logout</el-button>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="$router.push('/login')">Login / Register</el-button>
        </template>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => !!userStore.token)
const username = computed(() => userStore.userInfo.username)
const userRole = computed(() => userStore.userInfo.role)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #dcdfe6;
  padding: 0 20px;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}
.nav-menu {
  flex-grow: 1;
  display: flex;
  justify-content: center;
}
.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
