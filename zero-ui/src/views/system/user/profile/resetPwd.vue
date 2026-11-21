<template>
  <el-form ref="form" :model="user" :rules="rules" label-width="80px">
    <el-form-item label="旧密码" prop="oldPassword">
      <el-input v-model="user.oldPassword" placeholder="请输入旧密码" type="password" autocomplete="current-password" show-password/>
    </el-form-item>
    <el-form-item label="新密码" prop="newPassword">
      <el-input v-model="user.newPassword" placeholder="请输入新密码" type="password" autocomplete="new-password" show-password/>
    </el-form-item>
    <el-form-item label="确认密码" prop="confirmPassword">
      <el-input v-model="user.confirmPassword" placeholder="请确认新密码" type="password" autocomplete="new-password" show-password/>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="small" :loading="submitting" @click="submit">保存</el-button>
      <el-button type="danger" size="small" :disabled="submitting" @click="close">关闭</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
/**
 * 当前账号修改密码表单；提交成功后通过全局退出流程清理会话并跳转，失败时保留重试入口。
 */
import { updateUserPwd } from "@/api/system/user"
import { getToken } from "@/utils/auth"

export default {
  emits: ["close"],
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (this.user.newPassword !== value) {
        callback(new Error("两次输入的密码不一致"))
      } else {
        callback()
      }
    }
    return {
      submitting: false,
      user: {
        oldPassword: undefined,
        newPassword: undefined,
        confirmPassword: undefined
      },
      // 表单校验
      rules: {
        oldPassword: [
          { required: true, message: "旧密码不能为空", trigger: "blur" }
        ],
        newPassword: [
          { required: true, message: "新密码不能为空", trigger: "blur" },
          { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
          { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, message: "确认密码不能为空", trigger: "blur" },
          { required: true, validator: equalToPassword, trigger: "blur" }
        ]
      }
    }
  },
  methods: {
    async submit() {
      const token = getToken()
      if (this.submitting || !token || !this.$refs.form) return
      this.submitting = true
      try {
        const valid = await this.$refs.form.validate().catch(() => false)
        if (!valid || getToken() !== token) return
        const saved = await updateUserPwd(this.user.oldPassword, this.user.newPassword)
          .then(() => true, () => false)
        if (!saved) return

        this.$refs.form?.resetFields()
        this.$modal.msgSuccess("密码修改成功，请重新登录")
        // 只退出提交改密时的会话，退出接口失败时仍清理本地登录状态。
        await this.$store.dispatch("ForceLogOut", token)
        if (!getToken()) await this.$router.replace({ name: "Index" })
      } finally {
        this.submitting = false
      }
    },
    close() {
      this.$emit("close")
    }
  }
}
</script>
