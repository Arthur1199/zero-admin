/**
 * Vue Options API 字典插件：把组件 dicts 配置转为响应式字典对象，并在异步加载完成后通知组件。
 */
import { reactive } from 'vue'
import Dict from './Dict'
import { mergeOptions } from './DictOptions'

export default function(app, options) {
  mergeOptions(options)
  app.mixin({
    data() {
      if (this.$options === undefined || this.$options.dicts === undefined || this.$options.dicts === null) {
        return {}
      }
      const dict = reactive(new Dict())
      dict.owner = this
      return {
        dict
      }
    },
    created() {
      if (!(this.dict instanceof Dict)) {
        return
      }
      options.onCreated && options.onCreated(this.dict)
      this.dict.init(this.$options.dicts).then(() => {
        options.onReady && options.onReady(this.dict)
        this.$nextTick(() => {
          this.$emit('dictReady', this.dict)
          if (this.$options.methods && this.$options.methods.onDictReady instanceof Function) {
            this.$options.methods.onDictReady.call(this, this.dict)
          }
        })
      })
    },
  })
}
