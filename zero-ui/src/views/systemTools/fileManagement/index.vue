<template>
  <div class="app-container file-management-page">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="原始文件名" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          placeholder="请输入原始文件名"
          clearable
          style="width: 240px"
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="存储平台" prop="platform">
        <el-input
          v-model="queryParams.platform"
          placeholder="请输入存储平台"
          clearable
          style="width: 200px"
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文件后缀" prop="ext">
        <el-input
          v-model="queryParams.ext"
          placeholder="请输入后缀"
          clearable
          style="width: 160px"
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="resolveIcon('el-icon-search')" size="small" @click="handleQuery">搜索</el-button>
        <el-button :icon="resolveIcon('el-icon-refresh')" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-upload
          :action="uploadUrl"
          :headers="headers"
          :show-file-list="false"
          :before-upload="handleBeforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          v-hasPermi="['systemTools:fileManagement:add']"
        >
          <el-button type="primary" plain :icon="resolveIcon('el-icon-upload')" size="small">上传文件</el-button>
        </el-upload>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain size="small" :icon="resolveIcon('el-icon-delete')"
          :disabled="loading || deleting || !ids.length" @click="handleDelete()"
          v-hasPermi="['systemTools:fileManagement:remove']">批量删除</el-button>
      </el-col>
      <el-col :span="8" class="upload-tip">请上传大小不超过 <b>{{ uploadLimitText }}</b> 的文件</el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="fileList" @selection-change="handleSelectionChange" border>
      <el-table-column v-if="$auth.hasPermi('systemTools:fileManagement:remove')" type="selection" width="55" align="center" />
      <el-table-column label="文件ID" align="center" prop="id" width="90" />
      <el-table-column label="原始文件名" align="center" prop="originalFilename" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="文件预览" align="center" width="160">
        <template #default="scope">
          <el-image
            v-if="isImage(scope.row)"
            class="file-preview-image"
            :src="getPreviewUrl(scope.row)"
            :preview-src-list="[getPreviewUrl(scope.row)]"
            :preview-teleported="true"
            fit="cover"
          >
            <template #error><div  class="preview-empty">无法预览</div></template>
          </el-image>
          <video
            v-else-if="isVideo(scope.row)"
            class="file-preview-video"
            :src="scope.row.url"
            controls
          />
          <el-link v-else-if="scope.row.url" :href="scope.row.url" target="_blank" type="primary">打开文件</el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="文件访问地址" align="center" prop="url" min-width="220" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-link v-if="scope.row.url" :href="scope.row.url" target="_blank" type="primary">
            {{ scope.row.url }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="文件名" align="center" prop="filename" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="文件大小" align="center" prop="size" width="120">
        <template #default="scope">
          <span>{{ formatFileSize(scope.row.size) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="缩略图地址" align="center" prop="thUrl" min-width="180" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-link v-if="scope.row.thUrl" :href="scope.row.thUrl" target="_blank" rel="noopener noreferrer" type="primary">
            {{ scope.row.thUrl }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="缩略图名" align="center" prop="thFilename" min-width="140" :show-overflow-tooltip="true" />
      <el-table-column label="缩略图大小" align="center" prop="thSize" width="120">
        <template #default="scope">
          <span>{{ formatFileSize(scope.row.thSize) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="基础存储路径" align="center" prop="basePath" min-width="140" :show-overflow-tooltip="true" />
      <el-table-column label="文件后缀" align="center" prop="ext" width="100" />
      <el-table-column label="存储平台" align="center" prop="platform" width="130" />
      <el-table-column label="对象ID" align="center" prop="objectId" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="对象类型" align="center" prop="objectType" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150" fixed="right">
        <template #default="scope">
          <el-button
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-view')"
            @click="handleDetail(scope.row)"
          >查看详情</el-button>
          <el-button
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-delete')"
            :disabled="loading || deleting"
            @click="handleDelete(scope.row)"
            v-hasPermi="['systemTools:fileManagement:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
v-model:page="queryParams.pageNum"
v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" v-model="open" width="720px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="文件ID">
              <el-input v-model="form.id" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存储平台">
              <el-input v-model="form.platform" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="文件访问地址">
          <div class="file-address-field">
            <el-input ref="fileUrlInput" :model-value="form.url" type="textarea" :rows="2" readonly />
            <el-button link type="primary" size="small" :icon="resolveIcon('el-icon-document-copy')"
              aria-label="复制文件访问地址" :disabled="!form.url || copyingAddress"
              @click="copyAddress(form.url, $refs.fileUrlInput)">复制</el-button>
          </div>
        </el-form-item>
        <el-form-item label="文件名">
          <el-input v-model="form.filename" disabled />
        </el-form-item>
        <el-form-item label="原始文件名" prop="originalFilename">
          <el-input v-model="form.originalFilename" disabled />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="文件大小">
              <el-input :value="formatFileSize(form.size)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="文件后缀">
              <el-input v-model="form.ext" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="对象ID">
              <el-input v-model="form.objectId" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="对象类型">
              <el-input v-model="form.objectType" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="缩略图地址">
          <div class="file-address-field">
            <el-input ref="thumbnailUrlInput" :model-value="form.thUrl" type="textarea" :rows="2" readonly />
            <el-button link type="primary" size="small" :icon="resolveIcon('el-icon-document-copy')"
              aria-label="复制缩略图地址" :disabled="!form.thUrl || copyingAddress"
              @click="copyAddress(form.thUrl, $refs.thumbnailUrlInput)">复制</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer><div  class="dialog-footer">
        <el-button @click="cancel">关闭</el-button>
      </div></template>
    </el-dialog>
  </div>
</template>

<script>
import { delFile, getFile, getUploadConfig as getFileUploadConfig, listFile } from "@/api/systemTools/fileManagement"
import { getToken } from "@/utils/auth"

export default {
  name: "FileManagement",
  data() {
    return {
      loading: true,
      ids: [],
      deleting: false,
      showSearch: true,
      total: 0,
      fileList: [],
      dateRange: [],
      open: false,
      copyingAddress: false,
      title: "",
      uploadLimitBytes: 30 * 1024 * 1024,
      uploadLimitText: "30MB",
      uploadUrl: import.meta.env.VITE_APP_BASE_API + "/systemTools/fileManagement/upload",
      headers: {
        Authorization: "Bearer " + getToken()
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        fileName: undefined,
        platform: undefined,
        ext: undefined
      },
      form: {}
    }
  },
  created() {
    this.getUploadConfig()
    this.getList()
  },
  methods: {
    getUploadConfig() {
      getFileUploadConfig().then(response => {
        const data = response.data || {}
        const maxFileSize = Number(data.maxFileSize)
        if (maxFileSize > 0) {
          this.uploadLimitBytes = maxFileSize
        }
        if (data.maxFileSizeText) {
          this.uploadLimitText = data.maxFileSizeText
        }
      }).catch(() => {})
    },
    getList() {
      this.loading = true
      return listFile(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.fileList = response.rows || []
        this.total = response.total || 0
      }).catch(() => {
        // 请求封装已展示下游错误，结束加载后允许用户重新查询。
        this.fileList = []
        this.total = 0
      }).finally(() => {
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        id: undefined,
        url: undefined,
        filename: undefined,
        originalFilename: undefined,
        size: undefined,
        basePath: undefined,
        path: undefined,
        ext: undefined,
        platform: undefined,
        thUrl: undefined,
        thFilename: undefined,
        thSize: undefined,
        objectId: undefined,
        objectType: undefined,
        createTime: undefined
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
    },
    handleBeforeUpload(file) {
      if (file.name && file.name.includes(",")) {
        this.$modal.msgError("文件名不能包含英文逗号")
        return false
      }
      const isLt = file.size <= this.uploadLimitBytes
      if (!isLt) {
        this.$modal.msgError(`上传文件大小不能超过 ${this.uploadLimitText}`)
        return false
      }
      this.$modal.loading("正在上传文件，请稍候...")
      return true
    },
    handleUploadSuccess(res, file) {
      this.$modal.closeLoading()
      if (res.code === 200) {
        this.$modal.msgSuccess("上传成功")
        this.getList()
      } else {
        this.$modal.msgError(res.msg || "上传失败")
      }
    },
    handleUploadError() {
      this.$modal.closeLoading()
      this.$modal.msgError("上传失败，请稍后重试")
    },
    handleDetail(row) {
      this.reset()
      getFile(row.id).then(response => {
        this.form = response.data || {}
        this.title = "文件详情"
        this.open = true
      }).catch(() => {})
    },
    async copyAddress(address, input) {
      if (!address || this.copyingAddress) return
      this.copyingAddress = true
      try {
        let copied = false
        try {
          if (navigator.clipboard?.writeText) {
            await navigator.clipboard.writeText(address)
            copied = true
          }
        } catch {
          // 浏览器拒绝剪贴板 API 时，改用弹窗内的选区复制。
        }
        const textarea = input?.textarea
        if (!copied && this.open && textarea?.value === address) {
          textarea.select()
          copied = document.execCommand("copy")
        }
        if (copied) {
          this.$modal.msgSuccess("复制成功")
          return
        }
      } catch {
        // 浏览器也可能禁用选区复制，统一提示手动复制。
      } finally {
        this.copyingAddress = false
      }
      this.$modal.msgError("复制失败，请手动选择地址复制")
    },
    handleDelete(row) {
      const ids = row ? [row.id] : this.ids
      if (this.loading || this.deleting || !ids.length || ids.some(id => id == null || id === "")) return
      const idText = ids.join(",")
      const confirmation = row
        ? `是否确认删除文件编号为"${idText}"的数据项？`
        : `是否确认删除选中的 ${ids.length} 个文件（编号：${idText}）？`
      this.deleting = true
      return this.$modal.confirm(confirmation).then(() => {
        return delFile(idText).then(response => {
          if (response.data === false) {
            this.$modal.msgWarning(response.msg)
          } else {
            this.$modal.msgSuccess(response.msg || "删除成功")
          }
        }).finally(() => {
          // 数据库已提交的删除会立即反映在列表中，包括存储清理失败的记录。
          return this.getList()
        })
      }).catch(() => {}).finally(() => {
        this.deleting = false
      })
    },
    formatFileSize(size) {
      if (size === undefined || size === null || size === "") {
        return "-"
      }
      const value = Number(size)
      if (Number.isNaN(value)) {
        return "-"
      }
      if (value < 1024) {
        return `${value} B`
      }
      if (value < 1024 * 1024) {
        return `${(value / 1024).toFixed(2)} KB`
      }
      return `${(value / 1024 / 1024).toFixed(2)} MB`
    },
    isImage(row) {
      return ["jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"].includes(this.getExt(row))
    },
    isVideo(row) {
      return ["mp4", "webm", "ogg", "mov"].includes(this.getExt(row))
    },
    getPreviewUrl(row) {
      return row.thUrl || row.url
    },
    getExt(row) {
      return ((row && row.ext) || "").toLowerCase()
    }
  }
}
</script>

<style scoped lang="scss">
.file-address-field {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  width: 100%;
}

.file-management-page {
  .upload-tip {
    height: 28px;
    line-height: 28px;
    color: #606266;
  }

  .file-preview-image,
  .file-preview-video {
    width: 96px;
    height: 72px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #f5f7fa;
  }

  .file-preview-video {
    object-fit: cover;
  }

  .preview-empty {
    height: 72px;
    line-height: 72px;
    color: #909399;
  }
}
</style>
