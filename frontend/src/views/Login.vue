<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>{{ $t('auth.loginTitle') }}</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" :placeholder="$t('auth.username')" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" :placeholder="$t('auth.password')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">{{ $t('common.login') }}</el-button>
        </el-form-item>
        <div style="text-align: center">
          <el-link type="primary" @click="$router.push('/register')">{{ $t('auth.noAccount') }}</el-link>
        </div>
      </el-form>
      <!-- Dropdown outside navbar just for convenience if user wants to switch language on login screen -->
      <div style="text-align: center; margin-top: 15px;">
        <el-dropdown @command="handleLangChange">
          <span class="el-dropdown-link" style="cursor: pointer; color: #909399; font-size: 12px;">
            🌐 {{ currentLang === 'zh' ? '中文' : 'English' }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="zh">中文</el-dropdown-item>
              <el-dropdown-item command="en">English</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/api/axios'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)

const currentLang = computed(() => locale.value)
const handleLangChange = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const form = reactive({
  username: '',
  password: ''
})

const rules = computed(() => ({
  username: [{ required: true, message: t('auth.usernameReq'), trigger: 'blur' }],
  password: [{ required: true, message: t('auth.passwordReq'), trigger: 'blur' }]
}))

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const data = await request.post('/api/auth/login', form)
        userStore.setToken(data.token)
        userStore.setUserInfo({ id: data.userId, username: data.username, role: data.role })
        ElMessage.success(t('auth.loginSuccess'))
        router.push('/')
      } catch (e) {}
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
h2 {
  text-align: center;
  margin-bottom: 20px;
}
</style>
