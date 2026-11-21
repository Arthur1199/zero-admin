<!-- @author zhengjie -->
<template>
  <div class="icon-body">
    <el-input v-model="name" class="icon-search" clearable placeholder="请输入图标名称" @clear="filterIcons" @input="filterIcons">
      <template #suffix><AppIcon  icon-class="el-icon-search" class="el-input__icon" /></template>
    </el-input>
    <div class="icon-list">
      <div class="list-container">
        <div v-for="(item, index) in iconList" class="icon-item-wrapper" :key="index" @click="selectedIcon(item)">
          <div :class="['icon-item', { active: activeIcon === item }]">
            <svg-icon :icon-class="item" class-name="icon" style="height: 25px;width: 16px;"/>
            <span>{{ item }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 项目 SVG 图标的筛选与选择面板；回传图标标识，不改写实际 SVG 文件。
 */
import icons from './requireIcons'
export default {
  name: 'IconSelect',
  props: {
    activeIcon: {
      type: String
    }
  },
  data() {
    return {
      name: '',
      iconList: icons
    }
  },
  methods: {
    filterIcons() {
      this.iconList = icons
      if (this.name) {
        this.iconList = this.iconList.filter(item => item.includes(this.name))
      }
    },
    selectedIcon(name) {
      this.$emit('selected', name)
      document.body.click()
    },
    reset() {
      this.name = ''
      this.iconList = icons
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
// 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。
  .icon-body {
    width: 100%;
    padding: 10px;
    color: var(--zero-text);
    .icon-search {
      position: relative;
      margin-bottom: 5px;
    }
    .icon-list {
      height: 200px;
      overflow: auto;
      .list-container {
        display: flex;
        flex-wrap: wrap;
        .icon-item-wrapper {
          width: calc(100% / 3);
          height: 25px;
          line-height: 25px;
          cursor: pointer;
          display: flex;
          .icon-item {
            display: flex;
            max-width: 100%;
            width: 100%;
            min-width: 0;
            height: 100%;
            padding: 0 5px;
            border-radius: 5px;
            // 图标和名称共用主题色，悬浮与当前选择使用同一组状态变量。
            &:hover,
            &.active {
              background: var(--zero-surface-hover);
              color: var(--zero-text-heading);
            }
            .icon {
              flex-shrink: 0;
              color: inherit;
            }
            span {
              display: inline-block;
              vertical-align: -0.15em;
              fill: currentColor;
              padding-left: 2px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }
        }
      }
    }
  }
</style>
