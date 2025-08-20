import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setupAxiosInterceptors } from './services/api'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import '@/assets/mian.css'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)


setupAxiosInterceptors()

app.use(router)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')