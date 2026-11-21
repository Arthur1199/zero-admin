<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="12" class="card-box">
        <el-card>
          <template #header><div ><span><AppIcon icon-class="el-icon-cpu" class=""></AppIcon> CPU</span></div></template>
          <el-table :data="cpuRows" border>
            <el-table-column label="属性" prop="label" />
            <el-table-column label="值" prop="value" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <template #header><div ><span><AppIcon icon-class="el-icon-tickets" class=""></AppIcon> 内存</span></div></template>
          <el-table :data="memoryRows" border>
            <el-table-column label="属性" prop="label" />
            <el-table-column label="内存" prop="memory">
              <template #default="{ row }">
                <span :class="{ 'text-danger': row.memoryHigh }">{{ row.memory }}</span>
              </template>
            </el-table-column>
            <el-table-column label="JVM" prop="jvm">
              <template #default="{ row }">
                <span :class="{ 'text-danger': row.jvmHigh }">{{ row.jvm }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="24" class="card-box">
        <el-card>
          <template #header><div >
            <span><AppIcon icon-class="el-icon-monitor" class=""></AppIcon> 服务器信息</span>
          </div></template>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell">服务器名称</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.computerName }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">操作系统</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.osName }}</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell">服务器IP</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.computerIp }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">系统架构</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.osArch }}</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="24" class="card-box">
        <el-card>
          <template #header><div >
            <span><AppIcon icon-class="el-icon-coffee-cup" class=""></AppIcon> Java虚拟机信息</span>
          </div></template>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;table-layout:fixed;">
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell">Java名称</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.name }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">Java版本</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.version }}</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell">启动时间</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.startTime }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">运行时长</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.runTime }}</div></td>
                </tr>
                <tr>
                  <td colspan="1" class="el-table__cell is-leaf"><div class="cell">安装路径</div></td>
                  <td colspan="3" class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.home }}</div></td>
                </tr>
                <tr>
                  <td colspan="1" class="el-table__cell is-leaf"><div class="cell">项目路径</div></td>
                  <td colspan="3" class="el-table__cell is-leaf"><div class="cell" v-if="server.sys">{{ server.sys.userDir }}</div></td>
                </tr>
                <tr>
                  <td colspan="1" class="el-table__cell is-leaf"><div class="cell">运行参数</div></td>
                  <td colspan="3" class="el-table__cell is-leaf"><div class="cell" v-if="server.jvm">{{ server.jvm.inputArgs }}</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="24" class="card-box">
        <el-card>
          <template #header><div >
            <span><AppIcon icon-class="el-icon-receiving" class=""></AppIcon> 磁盘状态</span>
          </div></template>
          <el-table :data="server.sysFiles || []" border>
            <el-table-column label="盘符路径" prop="dirName" />
            <el-table-column label="文件系统" prop="sysTypeName" />
            <el-table-column label="盘符类型" prop="typeName" />
            <el-table-column label="总大小" prop="total" />
            <el-table-column label="可用大小" prop="free" />
            <el-table-column label="已用大小" prop="used" />
            <el-table-column label="已用百分比" prop="usage" min-width="110">
              <template #default="{ row }">
                <span :class="{ 'text-danger': row.usage > 80 }">{{ row.usage }}%</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
/**
 * 服务器资源概览；加载 CPU、内存、JVM 和磁盘信息，页面负责加载提示及数值格式化。
 */
import { getServer } from "@/api/monitor/server"

function formatMetric(value, unit) {
  return value == null ? "" : `${value}${unit}`
}

export default {
  name: "Server",
  data() {
    return {
      // 服务器信息
      server: {}
    }
  },
  computed: {
    cpuRows() {
      const cpu = this.server.cpu
      return [
        { label: "核心数", value: cpu?.cpuNum },
        { label: "用户使用率", value: formatMetric(cpu?.used, "%") },
        { label: "系统使用率", value: formatMetric(cpu?.sys, "%") },
        { label: "当前空闲率", value: formatMetric(cpu?.free, "%") }
      ]
    },
    memoryRows() {
      const { mem, jvm } = this.server
      return [
        { label: "总内存", key: "total" },
        { label: "已用内存", key: "used" },
        { label: "剩余内存", key: "free" },
        { label: "使用率", key: "usage" }
      ].map(({ label, key }) => {
        const isUsage = key === "usage"
        return {
          label,
          memory: formatMetric(mem?.[key], isUsage ? "%" : "G"),
          jvm: formatMetric(jvm?.[key], isUsage ? "%" : "M"),
          memoryHigh: isUsage && mem?.usage > 80,
          jvmHigh: isUsage && jvm?.usage > 80
        }
      })
    }
  },
  created() {
    this.getList()
    this.openLoading()
  },
  methods: {
    /** 查询服务器信息 */
    getList() {
      getServer().then(response => {
        this.server = response.data || {}
        this.$modal.closeLoading()
      }).catch(() => {
        this.$modal.closeLoading()
      })
    },
    // 打开加载层
    openLoading() {
      this.$modal.loading("正在加载服务监控数据，请稍候！")
    }
  }
}
</script>
