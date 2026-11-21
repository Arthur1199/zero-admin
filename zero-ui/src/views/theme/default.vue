<template>
  <main class="default-preview">
    <header class="preview-header">
      <div>
        <h1>原始风格预览</h1>
        <p>深色侧栏、浅色工作区和蓝色操作控件。</p>
      </div>
      <router-link to="/index">返回首页</router-link>
    </header>

    <div class="preview-layout">
      <aside class="preview-sidebar">
        <strong>
          <img src="@/assets/logo/zero.ico" width="24" height="24" alt="">
          Zero 管理系统
        </strong>
        <ul aria-label="示例菜单">
          <li><AppIcon icon-class="el-icon-house" />系统首页</li>
          <li class="preview-menu-group"><AppIcon icon-class="el-icon-setting" />系统管理</li>
          <li class="preview-submenu preview-current"><AppIcon icon-class="el-icon-user" />用户管理</li>
          <li class="preview-submenu">角色管理</li>
          <li class="preview-submenu">菜单管理</li>
          <li><AppIcon icon-class="el-icon-monitor" />系统监控</li>
          <li><AppIcon icon-class="el-icon-tools" />系统工具</li>
          <li><AppIcon icon-class="el-icon-document" />日志管理</li>
        </ul>
      </aside>

      <section class="preview-workspace" aria-labelledby="preview-users">
        <div class="preview-toolbar">系统管理 / 用户管理</div>
        <section class="preview-panel" aria-labelledby="preview-intro">
          <h2 id="preview-intro">主题特色</h2>
          <p>以深蓝灰侧栏区分导航和工作区，白色面板承载表单与数据，蓝色突出当前菜单和主要操作。</p>
        </section>

        <div class="preview-grid">
          <article class="preview-panel">
            <h3>按钮组件</h3>
            <div class="preview-controls">
              <button type="button" class="preview-primary" @click="buttonExample = '已触发主要操作'">主要操作</button>
              <button type="button" @click="buttonExample = '已触发次要操作'">次要操作</button>
            </div>
            <p v-if="buttonExample" role="status">{{ buttonExample }}</p>
          </article>
          <article class="preview-panel">
            <h3>标签组件</h3>
            <div class="preview-controls">
              <span class="preview-tag is-enabled">正常</span>
              <span class="preview-tag is-pending">待处理</span>
              <span class="preview-tag">停用</span>
            </div>
          </article>
          <article class="preview-panel">
            <h3>输入框</h3>
            <input v-model="inputExample" aria-label="示例输入框" placeholder="请输入内容" />
          </article>
        </div>

        <section class="preview-panel" aria-labelledby="preview-colors">
          <h2 id="preview-colors">配色方案</h2>
          <dl class="preview-palette">
            <div v-for="color in palette" :key="color.value">
              <dt><span :style="{ backgroundColor: color.value }" aria-hidden="true" />{{ color.name }}</dt>
              <dd><code>{{ color.value }}</code></dd>
            </div>
          </dl>
        </section>

        <div class="preview-grid">
          <article v-for="feature in features" :key="feature.title" class="preview-panel">
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </article>
        </div>

        <div class="preview-panel">
          <h2 id="preview-users">用户管理</h2>
          <p>示例数据，可按名称筛选。</p>
          <form class="preview-filters" @submit.prevent="query = keyword.trim()">
            <label for="preview-keyword">用户名称</label>
            <input id="preview-keyword" v-model="keyword" placeholder="请输入用户名称" />
            <button type="submit" class="preview-primary">查询</button>
            <button type="button" @click="keyword = ''; query = ''">重置</button>
          </form>
          <div class="preview-table">
            <table>
              <thead>
                <tr><th scope="col">用户名称</th><th scope="col">部门</th><th scope="col">状态</th></tr>
              </thead>
              <tbody>
                <tr v-for="user in filteredUsers" :key="user.name">
                  <td>{{ user.name }}</td>
                  <td>{{ user.department }}</td>
                  <td><span class="preview-tag" :class="{ 'is-enabled': user.enabled }">{{ user.enabled ? '正常' : '停用' }}</span></td>
                </tr>
                <tr v-if="!filteredUsers.length"><td colspan="3">没有匹配的用户</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>

<script>
export default {
  name: 'DefaultThemePreview',
  data() {
    return {
      keyword: '',
      query: '',
      buttonExample: '',
      inputExample: '',
      palette: [
        { name: '侧栏背景', value: '#304156' },
        { name: '工作区背景', value: '#F0F2F5' },
        { name: '面板背景', value: '#FFFFFF' },
        { name: '主题蓝', value: '#409EFF' }
      ],
      features: [
        { title: '分层导航', description: '系统目录与子菜单缩进区分，当前页面使用蓝色高亮。' },
        { title: '浅色工作区', description: '灰色页面背景衬托白色面板，表格用细线区分行列。' },
        { title: '明确的操作状态', description: '按钮区分主次操作，状态标签用颜色辅助识别。' }
      ],
      users: [
        { name: '示例管理员', department: '系统管理部', enabled: true },
        { name: '示例运营员', department: '运营部', enabled: true },
        { name: '示例访客', department: '运营部', enabled: false }
      ]
    }
  },
  computed: {
    filteredUsers() {
      return this.users.filter(user => user.name.includes(this.query))
    }
  }
}
</script>

<style scoped lang="scss">
.default-preview {
  // 独立预览固定使用原始配色，当前账号的主题设置不参与示例渲染。
  min-height: 100vh;
  padding: clamp(16px, 4vw, 48px);
  background: #f0f2f5;
  color: #303133;

  :is(button, input) {
    padding: 7px 10px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #fff;
    color: #303133;
    font: inherit;
  }

  input {
    max-width: 100%;
    width: 200px;
  }

  button {
    cursor: pointer;
  }

  .preview-primary {
    border-color: #235ca0;
    background: #235ca0;
    color: #fff;
  }

  .preview-primary:hover {
    background: #1b4980;
  }

  :is(button, input, a):focus-visible {
    outline: 2px solid #235ca0;
    outline-offset: 2px;
  }
}

.preview-header,
.preview-layout {
  max-width: 1120px;
  margin-inline: auto;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;

  h1 {
    margin: 0;
    font-size: clamp(23px, 3vw, 32px);
  }

  p {
    margin: 8px 0 0;
  }

  a {
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #fff;
    color: #235ca0;
  }
}

.preview-layout {
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr);
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
}

.preview-sidebar {
  align-self: start;
  padding: 18px 0;
  background: #304156;
  color: #e6eaf0;

  strong {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-inline: 16px;
  }

  ul {
    padding: 0;
    list-style: none;
  }

  li {
    display: flex;
    align-items: center;
    gap: 9px;
    padding: 12px 16px;
  }

  .preview-current {
    background: #263445;
    color: #79bbff;
  }

  .preview-submenu {
    padding-inline-start: 32px;
  }
}

.preview-workspace {
  background: #f0f2f5;
}

.preview-toolbar {
  padding: 14px 18px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.preview-panel {
  margin: 16px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;

  h2,
  h3 {
    margin: 0;
    font-size: 19px;
  }

  p:last-child {
    margin-bottom: 0;
  }
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 16px;

  .preview-panel {
    margin: 0;
    min-width: 0;
  }

  h3 {
    margin-bottom: 12px;
    font-size: 16px;
  }
}

.preview-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preview-tag {
  display: inline-block;
  padding: 3px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #f4f4f5;
  color: #50565e;

  &.is-enabled {
    border-color: #b8dfaa;
    background: #f0f9eb;
    color: #2e6224;
  }

  &.is-pending {
    border-color: #f1d8a4;
    background: #fdf6ec;
    color: #815000;
  }
}

.preview-palette {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 0;

  dt span {
    display: block;
    height: 48px;
    margin-bottom: 8px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
  }

  dd {
    margin: 4px 0 0;
    color: #606266;
  }
}

.preview-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;

}

.preview-table {
  overflow-x: auto;

  table {
    width: 100%;
    border-collapse: collapse;
    text-align: left;
  }

  th,
  td {
    padding: 10px;
    border-bottom: 1px solid #ebeef5;
  }

  th {
    background: #f5f7fa;
  }
}

@media (max-width: 620px) {
  .preview-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .preview-sidebar ul {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 0;
  }

  .preview-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .preview-palette {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
