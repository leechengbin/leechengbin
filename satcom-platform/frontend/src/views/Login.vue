<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>卫星通信综合管理平台</h2>
        <p>请登录您的账号</p>
      </div>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            prefix-icon="User"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-btn" 
            :loading="loading" 
            @click="handleLogin"
            native-type="submit"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>© 2024 卫星通信综合管理平台</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import http from '../api/http';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loginFormRef = ref();
const loading = ref(false);

const loginForm = reactive({
  username: '',
  password: ''
});

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true;
      try {
        const response = await http.post('/auth/login', loginForm);
        if (response.success) {
          // 存储登录信息
          localStorage.setItem('token', response.data.token);
          localStorage.setItem('role', response.data.role);
          localStorage.setItem('displayName', response.data.displayName);
          localStorage.setItem('tokenExpiresAt', String(Date.now() + 24 * 60 * 60 * 1000));
          
          ElMessage.success('登录成功');
          
          // 检查是否有保存的跳转地址，没有则跳转到首页
          const redirectPath = sessionStorage.getItem('redirectAfterLogin');
          sessionStorage.removeItem('redirectAfterLogin');
          
          // 跳转到首页并刷新菜单栏
          router.replace(redirectPath || '/dashboard');
          // 刷新页面以确保菜单栏重新加载
          window.location.reload();
        } else {
          ElMessage.error(response.message || '登录失败');
        }
      } catch (error) {
        ElMessage.error('网络错误，请稍后重试');
        console.error('登录错误:', error);
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 40px;
  animation: fadeInUp 0.5s ease;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #303133;
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: 600;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  color: #909399;
  font-size: 12px;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>