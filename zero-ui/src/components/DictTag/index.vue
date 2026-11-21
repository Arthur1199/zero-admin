<template>
  <div>
    <template v-for="(item, index) in options" :key="item.value">
      <template v-if="values.includes(item.value)">
        <span
          v-if="(item.raw.listClass == 'default' || item.raw.listClass == '') && (item.raw.cssClass == '' || item.raw.cssClass == null)"
                    :index="index"
          :class="item.raw.cssClass"
          >{{ item.label + ' ' }}</span
        >
        <el-tag
          v-else
          :disable-transitions="true"
                    :index="index"
          :type="['primary', 'success', 'info', 'warning', 'danger'].includes(item.raw.listClass) ? item.raw.listClass : 'primary'"
          :class="item.raw.cssClass"
        >
          {{ item.label + ' ' }}
        </el-tag>
      </template>
    </template>
    <template v-if="unmatch && showValue">
      {{ unmatchArray.join(' ') }}
    </template>
  </div>
</template>

<script>
/**
 * 按字典值匹配标签及其展示类型；未匹配值按现有兼容规则显示，不改变提交值。
 */
export default {
  name: "DictTag",
  props: {
    options: {
      type: Array,
      default: null,
    },
    value: [Number, String, Array],
    // 当未找到匹配的数据时，显示value
    showValue: {
      type: Boolean,
      default: true,
    },
    separator: {
      type: String,
      default: ","
    }
  },
  data() {
    return {
      unmatchArray: [], // 记录未匹配的项
    }
  },
  computed: {
    values() {
      if (this.value === null || typeof this.value === 'undefined' || this.value === '') return []
      return Array.isArray(this.value) ? this.value.map(item => '' + item) : String(this.value).split(this.separator)
    },
    unmatch() {
      this.unmatchArray = []
      // 没有value不显示
      if (this.value === null || typeof this.value === 'undefined' || this.value === '' || this.options.length === 0) return false
      // 传入值为数组
      let unmatch = false // 添加一个标志来判断是否有未匹配项
      this.values.forEach(item => {
        if (!this.options.some(v => v.value === item)) {
          this.unmatchArray.push(item)
          unmatch = true // 如果有未匹配项，将标志设置为true
        }
      })
      return unmatch // 返回标志的值
    },

  },
}
</script>
<style scoped>
/* 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。 */
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
