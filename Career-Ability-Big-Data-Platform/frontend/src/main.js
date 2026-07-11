import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/base.css'
import './styles/tokens.css'
import './styles/element-plus.css'
import './styles/motion.css'
import './styles/main.css'
import App from './App.vue'
import router from './router'
import pinia from './stores'
import permission from './directives/permission'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.directive('permission', permission)
app.mount('#app')
