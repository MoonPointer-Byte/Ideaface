import axios from 'axios';
import { useUserStore } from '@/store/user';
import { ElMessage } from 'element-plus';


const api = axios.create({
  baseURL: 'http://localhost:8080/api', 
  timeout: 60000,
});

export function setupAxiosInterceptors() {
  api.interceptors.request.use(
    (config) => {
      const userStore = useUserStore();
      const token = userStore.getToken();
      
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  api.interceptors.response.use(
    (response) => response,
    (error) => {
      const message = error.response?.data?.error || error.response?.data?.message || '请求失败';

      if (error.response?.status === 401) {
        const userStore = useUserStore();
        if (userStore.isLoggedIn) {
            userStore.clearAuth();
            ElMessage.error("登录已过期，请重新登录。");
            window.location.replace(`/login?redirect=${window.location.pathname}`);
        }
      } else {
        ElMessage.error(message);
      }
      return Promise.reject(error);
    }
  );
}

export default api;