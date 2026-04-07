<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>AgriSales Login</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="Username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="Password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">Login</el-button>
        </el-form-item>
        <div style="text-align: center">
          <el-link type="primary" @click="$router.push('/register')">Don't have an account? Register</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/api/axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Please input username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please input password', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const data = await request.post('/api/auth/login', form)
        userStore.setToken(data.token)
        userStore.setUserInfo({ id: data.userId, username: data.username, role: data.role })
        ElMessage.success('Login success')
        router.push('/')
      } catch (e) {
        // Error handled by interceptor
      }
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
