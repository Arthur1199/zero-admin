<template>
  <div class="login">
    <!-- 背景特效层 -->
    <div class="bg-effect">
      <div class="gradient-bg"></div>
      <div class="bubble" v-for="i in 15" :key="i" :style="getBubbleStyle(i)"></div>
    </div>

    <!-- 登录表单层 -->
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{title}}</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          auto-complete="off"
          placeholder="账号"
        >
          <template #prefix><svg-icon  icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          auto-complete="off"
          placeholder="密码"
          show-password
@keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon  icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="googleCode" v-if="googleAuthEnabled">
        <el-input
          v-model="loginForm.googleCode"
          maxlength="6"
          auto-complete="off"
          placeholder="谷歌验证码(首次绑定请输入123456)"
@keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon  icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
@keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon  icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="login-code">
          <el-button v-if="codeFailed" link type="primary" :disabled="codeLoading" @click="getCode">重新获取验证码</el-button>
          <img v-else :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          :disabled="codeLoading || codeFailed"
size="default"
          type="primary"
          style="width:100%;"
@click.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2026 Zero. All Rights Reserved.</span>
    </div>
    <el-dialog
class="google-bind-dialog"
v-model="googleBindOpen"
      append-to-body
      width="420px"
      :close-on-click-modal="false"
    >
      <template #header><div  class="google-dialog-title">
        <span class="google-title-icon">
          <svg-icon icon-class="lock" />
        </span>
        <span>绑定谷歌动态码</span>
      </div></template>
      <el-form ref="googleBindForm" :model="googleBindForm" :rules="googleBindRules" class="google-bind-form">
        <div class="google-qrcode-wrap">
          <img :src="googleImageUrl" class="google-qrcode" alt="">
        </div>
        <el-form-item prop="googleCode">
          <el-input
            v-model="googleBindForm.googleCode"
            maxlength="6"
            placeholder="请输入6位谷歌验证码"
@keyup.enter="handleBindGoogle"
          >
            <template #prefix><svg-icon  icon-class="validCode" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer><div  class="dialog-footer">
        <el-button @click="googleBindOpen = false">取 消</el-button>
        <el-button type="primary" :loading="bindLoading" @click="handleBindGoogle">确 定</el-button>
      </div></template>
    </el-dialog>
  </div>
</template>

<script>
import { bindGoogle, getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      title: import.meta.env.VITE_APP_TITLE,
      codeUrl: "",
      codeLoading: false,
      codeFailed: false,
      loginForm: {
        username: "admin",
        password: "",
        rememberMe: false,
        code: "",
        uuid: "",
        googleCode: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        googleCode: [
          { pattern: /^\d{6}$/, trigger: "blur", message: "请输入6位数字验证码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      googleBindForm: {
        googleCode: ""
      },
      googleBindRules: {
        googleCode: [
          { required: true, trigger: "blur", message: "请输入谷歌验证码" },
          { pattern: /^\d{6}$/, trigger: "blur", message: "请输入6位数字验证码" }
        ]
      },
      loading: false,
      bindLoading: false,
      googleBindOpen: false,
      googleImageUrl: "",
      // 验证码开关
      captchaEnabled: true,
      // 谷歌动态码开关
      googleAuthEnabled: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getBubbleStyle(index) {
      const size = Math.random() * 80 + 40
      const left = Math.random() * 100
      const duration = Math.random() * 10 + 15
      const delay = Math.random() * 5

      return {
        width: size + 'px',
        height: size + 'px',
        left: left + '%',
        animationDuration: duration + 's',
        animationDelay: delay + 's'
      }
    },
    getCode() {
      if (this.codeLoading) return
      this.codeLoading = true
      getCodeImg().then(res => {
        this.codeFailed = false
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        this.googleAuthEnabled = res.googleAuthEnabled === true || res.googleAuthEnabled === 'true'
        this.codeUrl = this.captchaEnabled ? "data:image/gif;base64," + res.img : ""
        this.loginForm.uuid = this.captchaEnabled ? res.uuid : ""
      }).catch(() => {
        // 刷新失败后不能继续提交旧验证码，错误提示仍由请求拦截器负责。
        this.codeFailed = true
        this.codeUrl = ""
        this.loginForm.uuid = ""
        this.loginForm.code = ""
      }).finally(() => {
        this.codeLoading = false
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
        code: "",
        uuid: "",
        googleCode: ""
      }
    },
    openGoogleBindDialog() {
      if (!this.loginForm.username) {
        this.$message.warning("请先输入账号")
        return
      }
      this.googleBindForm.googleCode = ""
      this.googleImageUrl = import.meta.env.VITE_APP_BASE_API + "/google/generateGaImage?userName=" + encodeURIComponent(this.loginForm.username) + "&t=" + new Date().getTime()
      this.googleBindOpen = true
    },
    handleBindGoogle() {
      this.$refs.googleBindForm.validate(valid => {
        if (!valid) {
          return
        }
        this.bindLoading = true
        bindGoogle({
          userName: this.loginForm.username,
          googleCode: this.googleBindForm.googleCode
        }).then(() => {
          this.$modal.msgSuccess("绑定成功，请重新登录")
          this.loginForm.googleCode = ""
          this.googleBindOpen = false
        }).catch(() => {
          // 接口错误由全局拦截器提示，保留输入供重试，避免未处理的绑定异常。
        }).finally(() => {
          this.bindLoading = false
        })
      })
    },
    handleLogin() {
      if (this.codeLoading || this.codeFailed) return
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            return this.$router.push({ path: this.redirect || "/" })
          }).catch(error => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
            if (error && error.code === -1001) {
              this.openGoogleBindDialog()
            }
          }).finally(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  position: relative;
  overflow: hidden;
}

// 背景特效层
.bg-effect {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

// 渐变背景
.gradient-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg,
    #667eea 0%,
    #764ba2 25%,
    #f093fb 50%,
    #4facfe 75%,
    #00f2fe 100%
  );
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

// 气泡样式
.bubble {
  position: absolute;
  bottom: -100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(5px);
  animation: bubbleRise linear infinite;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
}

@keyframes bubbleRise {
  0% {
    bottom: -100px;
    opacity: 0;
    transform: translateX(0) scale(1);
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    bottom: 110%;
    opacity: 0;
    transform: translateX(50px) scale(0.8);
  }
}

// 登录表单
.login-form {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  width: 400px;
  padding: 25px 25px 5px 25px;
  z-index: 1;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

  .el-input {
    height: 38px;
    input {
      height: 38px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}

.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #333;
  font-weight: 600;
}

.login-code {
  width: 33%;
  height: 38px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}

.login-code-img {
  height: 38px;
  border-radius: 4px;
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: rgba(255, 255, 255, 0.9);
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
  z-index: 2;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

:deep(.google-bind-dialog) {
  min-height: 0 !important;
  max-height: none !important;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  box-shadow: 0 22px 70px rgba(54, 34, 91, 0.32);

  .el-dialog__header {
    padding: 24px 26px 10px;
  }

  .el-dialog__headerbtn {
    top: 26px;
    right: 24px;
  }

  .el-dialog__body {
    flex: none !important;
    padding: 8px 34px 6px;
    overflow: visible;
  }

  .el-dialog__footer {
    padding: 8px 34px 28px;
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
  }

  .el-button {
    min-width: 92px;
    border-radius: 8px;
  }

  .el-button--primary {
    border: none;
    background: linear-gradient(135deg, #667eea 0%, #4facfe 100%);
    box-shadow: 0 8px 18px rgba(79, 172, 254, 0.26);
  }

  .el-input__inner {
    height: 42px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.88);
  }
}

.google-dialog-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.google-title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8px 18px rgba(102, 126, 234, 0.28);
}

.google-bind-form {
  .input-icon {
    height: 42px;
    width: 14px;
    margin-left: 2px;
  }
}

.google-qrcode-wrap {
  width: 256px;
  height: 256px;
  margin: 8px auto 22px;
  padding: 14px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(54, 34, 91, 0.12);
}

.google-qrcode {
  width: 228px;
  height: 228px;
  display: block;
}
</style>
