/**
 * 系统字典的数据源适配：优先使用 Vuex 缓存，未命中时经公共 API 加载，再接入组件 dicts 配置。
 */
import store from '@/store'
import DataDict from '@/utils/dict'
import { getDicts as getDicts } from '@/api/system/dict/data'

function searchDictByKey(dict, key) {
  if (key == null && key == "") {
    return null
  }
  try {
    for (let i = 0; i < dict.length; i++) {
      if (dict[i].key == key) {
        return dict[i].value
      }
    }
  } catch (e) {
    return null
  }
}

function install(app) {
  app.use(DataDict, {
    metas: {
      '*': {
        labelField: 'dictLabel',
        valueField: 'dictValue',
        request(dictMeta) {
          const storeDict = searchDictByKey(store.getters.dict, dictMeta.type)
          if (storeDict) {
            return new Promise(resolve => { resolve(storeDict) })
          } else {
            return new Promise((resolve, reject) => {
              getDicts(dictMeta.type).then(res => {
                store.dispatch('dict/setDict', { key: dictMeta.type, value: res.data })
                resolve(res.data)
              }).catch(error => {
                reject(error)
              })
            })
          }
        },
      },
    },
  })
}

export default {
  install,
}