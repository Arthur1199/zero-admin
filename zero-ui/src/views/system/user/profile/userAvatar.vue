<template>
  <section class="profile-avatar-editor" aria-label="头像裁剪">
    <el-row>
      <el-col :xs="24" :md="12" :style="{height: '350px'}">
        <vue-cropper
          ref="cropper"
          :img="options.img"
          :info="true"
          :autoCrop="options.autoCrop"
          :autoCropWidth="options.autoCropWidth"
          :autoCropHeight="options.autoCropHeight"
          :fixedBox="options.fixedBox"
          :outputType="options.outputType"
          @realTime="realTime"
        />
      </el-col>
      <el-col :xs="24" :md="12" :style="{height: '350px'}">
        <div class="avatar-upload-preview">
          <img v-if="previews.url" :src="previews.url" :style="previews.img" alt="头像裁剪预览" />
        </div>
      </el-col>
    </el-row>
    <el-row class="avatar-editor-actions">
      <el-col :lg="2" :sm="3" :xs="3">
        <el-upload action="#" :http-request="requestUpload" :show-file-list="false" :before-upload="beforeUpload">
          <el-button size="small">
            选择
            <AppIcon icon-class="el-icon-upload" class="el-icon--right"></AppIcon>
          </el-button>
        </el-upload>
      </el-col>
      <el-col :lg="{span: 1, offset: 2}" :sm="2" :xs="2">
        <el-button :icon="resolveIcon('el-icon-plus')" size="small" aria-label="放大" @click="changeScale(1)"></el-button>
      </el-col>
      <el-col :lg="{span: 1, offset: 1}" :sm="2" :xs="2">
        <el-button :icon="resolveIcon('el-icon-minus')" size="small" aria-label="缩小" @click="changeScale(-1)"></el-button>
      </el-col>
      <el-col :lg="{span: 1, offset: 1}" :sm="2" :xs="2">
        <el-button :icon="resolveIcon('el-icon-refresh-left')" size="small" aria-label="向左旋转" @click="rotateLeft()"></el-button>
      </el-col>
      <el-col :lg="{span: 1, offset: 1}" :sm="2" :xs="2">
        <el-button :icon="resolveIcon('el-icon-refresh-right')" size="small" aria-label="向右旋转" @click="rotateRight()"></el-button>
      </el-col>
      <el-col :lg="{span: 2, offset: 6}" :sm="2" :xs="2">
        <el-button type="primary" size="small" :loading="uploading" @click="uploadImg">提 交</el-button>
      </el-col>
      <el-col :lg="{span: 2, offset: 1}" :sm="2" :xs="2">
        <el-button size="small" @click="$emit('close')">关闭</el-button>
      </el-col>
    </el-row>
  </section>
</template>

<script>
/**
 * 个人头像裁剪与上传；维护裁剪参数、文件校验和认证上传，成功后更新当前账号头像。
 */
import store from "@/store"
import { VueCropper } from "vue-cropper"
import "vue-cropper/dist/index.css"
import { uploadAvatar } from "@/api/system/user"
import { debounce } from '@/utils'
import { isHttp } from "@/utils/validate"

export default {
  components: { VueCropper },
  emits: ["close"],
  data() {
    return {
      uploading: false,
      options: {
        img: store.getters.avatar,  //裁剪图片的地址
        autoCrop: true,             // 是否默认生成截图框
        autoCropWidth: 200,         // 默认生成截图框宽度
        autoCropHeight: 200,        // 默认生成截图框高度
        fixedBox: true,             // 固定截图框大小 不允许改变
        outputType:"png",           // 默认生成截图为PNG格式
        filename: 'avatar'          // 文件名称
      },
      previews: {},
      resizeHandler: null
    }
  },
  mounted() {
    this.resizeHandler = debounce(() => this.$refs.cropper?.refresh(), 100)
    window.addEventListener("resize", this.resizeHandler)
  },
  beforeUnmount() {
    window.removeEventListener("resize", this.resizeHandler)
  },
  methods: {
    // 覆盖默认的上传行为
    requestUpload() {
    },
    // 向左旋转
    rotateLeft() {
      this.$refs.cropper.rotateLeft()
    },
    // 向右旋转
    rotateRight() {
      this.$refs.cropper.rotateRight()
    },
    // 图片缩放
    changeScale(num) {
      num = num || 1
      this.$refs.cropper.changeScale(num)
    },
    // 上传预处理
    beforeUpload(file) {
      if (file.type.indexOf("image/") == -1) {
        this.$modal.msgError("文件格式错误，请上传图片类型,如：JPG，PNG后缀的文件。")
      } else {
        const reader = new FileReader()
        reader.readAsDataURL(file)
        reader.onload = () => {
          this.options.img = reader.result
          this.options.filename = file.name
        }
      }
    },
    // 上传图片
    uploadImg() {
      if (this.uploading) return
      this.uploading = true
      this.$refs.cropper.getCropBlob(data => {
        let formData = new FormData()
        formData.append("avatarfile", data, this.options.filename)
        uploadAvatar(formData).then(response => {
          const imgUrl = response.imgUrl
          const avatarUrl = isHttp(imgUrl) ? imgUrl : import.meta.env.VITE_APP_BASE_API + imgUrl
          // 只更新已保存的头像，当前裁剪器继续使用选图和裁剪位置。
          store.commit('SET_AVATAR', avatarUrl)
          this.$modal.msgSuccess("修改成功")
        }).catch(() => {
          // 上传失败由请求拦截器提示，保留裁剪结果供重试，避免回调中的未处理异常。
        }).finally(() => {
          this.uploading = false
        })
      })
    },
    // 实时预览
    realTime(data) {
      this.previews = data
    }
  }
}
</script>
<style scoped lang="scss">
// 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。
.avatar-editor-actions {
  margin-top: 16px;
}

@media (max-width: 767px) {
  .avatar-editor-actions {
    gap: 8px;
  }

  .avatar-editor-actions > .el-col {
    flex: 0 0 auto;
    max-width: none;
  }
}
</style>
