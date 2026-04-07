<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>AgriSales Register</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="Username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="Password" />
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="form.role" placeholder="Select Role" style="width: 100%">
            <el-option label="Consumer" value="CONSUMER" />
            <el-option label="Farmer" value="FARMER" />
            <el-option label="Admin" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width: 100%">Register</el-button>
        </el-form-item>
        <div style="text-align: center">
          <el-link type="primary" @click="$router.push('/login')">Already have an account? Login</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  role: 'CONSUMER'
})

const rules = {
  username: [{ required: true, message: 'Please input username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please input password', trigger: 'blur' }],
  role: [{ required: true, message: 'Please select a role', trigger: 'change' }]
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.post('/api/auth/register', form)
        ElMessage.success('Register success, please login')
        router.push('/login')
      } catch (e) {
        // Error handled
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
