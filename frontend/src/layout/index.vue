<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">{{ ('common.logo') }}</div>
      <div class="nav-menu">
        <el-menu mode="horizontal" :router="true" :default-active="$route.path">
          <el-menu-item index="/">{{ $t('nav.home') }}</el-menu-item>
          <el-menu-item index="/dashboard" v-if="userRole === 'ADMIN' || userRole === 'FARMER'">{{ $t('nav.dashboard') }}</el-menu-item>
          <el-menu-item index="/cart" v-if="userRole === 'CONSUMER'">{{ $t('nav.cart') }}</el-menu-item>
          <el-menu-item index="/orders" v-if="userRole === 'CONSUMER' || userRole === 'FARMER'">{{ $t('nav.orders') }}</el-menu-item>
          <el-menu-item index="/my-products" v-if="userRole === 'FARMER'">{{ $t('nav.myProducts') }}</el-menu-item>
          <el-menu-item index="/review-products" v-if="userRole === 'ADMIN'">{{ $t('nav.reviewProducts') }}</el-menu-item>
          <el-menu-item index="/users" v-if="userRole === 'ADMIN'">{{ $t('nav.userManagement') }}</el-menu-item>
        </el-menu>
      </div>

      <div class="right-actions">
        <!-- Language Switcher -->
        <el-dropdown @command="handleLangChange" style="margin-right: 20px; cursor: pointer;">
          <span class="el-dropdown-link">
            🌐 {{ currentLang === 'zh' ? '中文' : 'English' }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="zh">中文</el-dropdown-item>
              <el-dropdown-item command="en">English</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <div class="user-info">
          <template v-if="isLoggedIn">
            <span>{{ username }} ({{ $t('roles.' + userRole.toLowerCase()) }})</span>
            <el-button type="danger" size="small" @click="handleLogout" style="margin-left: 15px">{{ $t('common.logout') }}</el-button>
          </template>
          <template v-else>
            <el-button type="primary" size="small" @click="$router.push('/login')">{{ $t('nav.loginRegister') }}</el-button>
          </template>
        </div>
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
import { useI18n } from 'vue-i18n'

const router = useRouter()
const userStore = useUserStore()
const { locale } = useI18n()

const currentLang = computed(() => locale.value)

const isLoggedIn = computed(() => !!userStore.token)
const username = computed(() => userStore.userInfo.username)
const userRole = computed(() => userStore.userInfo.role)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const handleLangChange = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
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
.right-actions {
  display: flex;
  align-items: center;
}
.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
