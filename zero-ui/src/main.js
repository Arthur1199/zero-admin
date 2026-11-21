import { createApp } from 'vue'


import Element from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import AppIcon from '@/components/AppIcon'
import { resolveIcon, normalizeSize } from '@/utils/icons'
import 'element-plus/dist/index.css'
import './assets/styles/element-variables.scss'

import '@/assets/styles/index.scss' // global css
import '@/assets/styles/zero.scss' // zero css
import '@/assets/styles/pagination-override.scss' // pagination theme override
import '@/assets/styles/glassmorphism-readable.scss' // glassmorphism readable overlay override
import '@/assets/styles/vue3-compat.scss'
import '@/assets/styles/table-fixed-column.scss' // final table fixed column hover override
import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive
import plugins from './plugins' // plugins
import { download } from '@/utils/request'

import registerIcons from './assets/icons' // icon
import './permission' // permission control
import { watchThemeChange } from '@/utils/removeMenuHover' // 移除毛玻璃主题菜单hover背景色
import { getDicts } from "@/api/system/dict/data"
import { getConfigKey } from "@/api/system/config"
import { parseTime, resetForm, addDateRange, selectDictLabel, selectDictLabels, handleTree } from "@/utils/zero"
// 分页组件
import Pagination from "@/components/Pagination"
// 自定义表格工具组件
import RightToolbar from "@/components/RightToolbar"
// 富文本组件
import Editor from "@/components/Editor"
// 文件上传组件
import FileUpload from "@/components/FileUpload"
// 图片上传组件
import ImageUpload from "@/components/ImageUpload"
// 图片预览组件
import ImagePreview from "@/components/ImagePreview"
// 字典标签组件
import DictTag from '@/components/DictTag'
// 字典数据组件
import DictData from '@/components/DictData'

const app = createApp(App)
Object.assign(app.config.globalProperties, { getDicts, getConfigKey, parseTime, resetForm, addDateRange, selectDictLabel, selectDictLabels, download, handleTree, resolveIcon, normalizeSize })
Object.entries({ DictTag, Pagination, RightToolbar, Editor, FileUpload, ImageUpload, ImagePreview, AppIcon }).forEach(([name, component]) => app.component(name, component))
app.use(store)
app.use(Element, { locale: zhCn, size: store.getters.size })
app.use(directive)
app.use(plugins)
DictData.install(app)
registerIcons(app)
watchThemeChange()
app.use(router)
app.mount('#app')
