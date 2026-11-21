<template>
  <el-form ref="form" :model="form" :rules="rules" label-position="top" class="profile-form" :disabled="saving">
    <el-form-item label="用户昵称" prop="nickName">
      <el-input v-model="form.nickName" maxlength="30" autocomplete="nickname" />
    </el-form-item>
    <el-form-item label="手机号码" prop="phonenumber">
      <el-input v-model="form.phonenumber" maxlength="11" inputmode="tel" autocomplete="tel" />
    </el-form-item>
    <el-form-item label="邮箱" prop="email">
      <el-input v-model="form.email" maxlength="50" inputmode="email" autocomplete="email" />
    </el-form-item>
    <el-form-item label="性别">
      <el-radio-group v-model="form.sex">
        <el-radio value="0">男</el-radio>
        <el-radio value="1">女</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item class="profile-form-actions">
      <el-button type="primary" :loading="saving" :disabled="!hasChanges" @click="submit">保存</el-button>
      <el-button :disabled="!hasChanges" @click="restoreForm">还原修改</el-button>
      <el-button @click="close">关闭</el-button>
      <span v-if="hasChanges" class="profile-form-status" role="status">有未保存的修改</span>
    </el-form-item>
  </el-form>
</template>

<script>
/**
 * 个人资料编辑草稿；与当前展示信息分离，成功后通知父组件更新摘要，关闭时保留既有恢复行为。
 */
import { updateUserProfile } from "@/api/system/user"

export default {
  emits: ["saved", "close"],
  props: {
    user: {
      type: Object
    }
  },
  data() {
    return {
      form: {},
      saving: false,
      // 表单校验
      rules: {
        nickName: [
          { required: true, message: "用户昵称不能为空", trigger: "blur" }
        ],
        email: [
          { required: true, message: "邮箱地址不能为空", trigger: "blur" },
          {
            type: "email",
            message: "请输入正确的邮箱地址",
            trigger: ["blur", "change"]
          }
        ],
        phonenumber: [
          { required: true, message: "手机号码不能为空", trigger: "blur" },
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: "请输入正确的手机号码",
            trigger: "blur"
          }
        ]
      }
    }
  },
  computed: {
    hasChanges() {
      return Object.keys(this.form).some(key => (this.form[key] ?? "") !== (this.user[key] ?? ""))
    }
  },
  watch: {
    user: {
      handler() {
        this.restoreForm()
      },
      immediate: true
    }
  },
  methods: {
    restoreForm() {
      const { nickName, phonenumber, email, sex } = this.user || {}
      this.form = { nickName, phonenumber, email, sex }
      this.$refs.form?.clearValidate()
    },
    submit() {
      if (this.saving || !this.hasChanges) return
      this.$refs["form"].validate(valid => {
        if (valid && !this.saving) {
          this.saving = true
          const profile = { ...this.form }
          updateUserProfile(profile).then(() => {
            this.$emit("saved", profile)
            this.$modal.msgSuccess("修改成功")
          }).catch(() => {
            // 保存失败保留输入，错误提示由请求拦截器负责。
          }).finally(() => {
            this.saving = false
          })
        }
      })
    },
    close() {
      this.$emit("close")
    }
  }
}
</script>

<style scoped lang="scss">
// 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。
.profile-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 230px), 1fr));
  column-gap: 20px;
  max-width: 680px;
  padding-top: 8px;
}

.profile-form-actions {
  grid-column: 1 / -1;
}

.profile-form-status {
  margin-inline-start: 12px;
  opacity: .8;
}
</style>
