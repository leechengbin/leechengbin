import axios from "axios";
import { ElMessage } from 'element-plus';
import router from '../router';

const http = axios.create({
  baseURL: "/api",
  timeout: 10000
});

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    const expiresAt = localStorage.getItem('tokenExpiresAt');
    const isExpired = expiresAt ? Date.now() > Number(expiresAt) : false;

    if (token && !isExpired) {
      config.headers.Authorization = `Bearer ${token}`;
    } else if (token && isExpired) {
      localStorage.removeItem('token');
      localStorage.removeItem('tokenExpiresAt');
      localStorage.removeItem('role');
      localStorage.removeItem('displayName');
      ElMessage.error('登录已过期，请重新登录');
      // 记录需要跳转的页面，登录后可返回
      sessionStorage.setItem('redirectAfterLogin', window.location.pathname);
      router.push('/login');
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const isTimeout = error.code === 'ECONNABORTED' || error.message?.includes('timeout');
    if (isTimeout) {
      ElMessage.error('请求超时，请稍后重试');
      return Promise.reject(error);
    }

    if (error.response?.status === 401) {
      // 未授权，跳转到登录页
      localStorage.removeItem('token');
      localStorage.removeItem('tokenExpiresAt');
      localStorage.removeItem('role');
      localStorage.removeItem('displayName');
      ElMessage.error('登录已过期，请重新登录');
      // 记录需要跳转的页面，登录后可返回
      sessionStorage.setItem('redirectAfterLogin', window.location.pathname);
      router.push('/login');
    } else if (error.response?.status === 403) {
      localStorage.removeItem('token');
      localStorage.removeItem('tokenExpiresAt');
      localStorage.removeItem('role');
      localStorage.removeItem('displayName');
      ElMessage.error(error.response?.data?.message || '无权限访问');
      sessionStorage.setItem('redirectAfterLogin', window.location.pathname);
      router.push('/login');
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误');
    }
    return Promise.reject(error);
  }
);

export default http;
