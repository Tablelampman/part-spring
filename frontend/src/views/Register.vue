<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>{{ $t('auth.registerTitle') }}</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" :placeholder="$t('auth.username')" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" :placeholder="$t('auth.password')" />
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="form.role" :placeholder="$t('auth.role')" style="width: 100%">
            <el-option :label="$t('roles.consumer')" value="CONSUMER" />
            <el-option :label="$t('roles.farmer')" value="FARMER" />
            <el-option :label="$t('roles.admin')" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width: 100%">{{ $t('common.register') }}</el-button>
        </el-form-item>
        <div style="text-align: center">
          <el-link type="primary" @click="$router.push('/login')">{{ $t('auth.hasAccount') }}</el-link>
        </div>
      </el-form>
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
import request from '@/api/axios'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const router = useRouter()
const formRef = ref(null)

const currentLang = computed(() => locale.value)
const handleLangChange = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const form = reactive({
  username: '',
  password: '',
  role: 'CONSUMER'
})

const rules = computed(() => ({
  username: [{ required: true, message: t('auth.usernameReq'), trigger: 'blur' }],
  password: [{ required: true, message: t('auth.passwordReq'), trigger: 'blur' }],
  role: [{ required: true, message: t('auth.roleReq'), trigger: 'change' }]
}))

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.post('/api/auth/register', form)
        ElMessage.success(t('auth.registerSuccess'))
        router.push('/login')
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
