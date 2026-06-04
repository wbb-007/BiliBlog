<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { persistAuthSession } from '../stores/auth'
import { loginAuth, registerAuth, resetPasswordAuth, sendAuthCode } from '../api/blog'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'success'])

const sending = ref(false)
const submitting = ref(false)
const activeTab = ref('login')
const devCode = ref('')

const form = reactive({
  email: '',
  nickname: '',
  code: '',
  password: '',
  confirmPassword: '',
})

const codePurpose = computed(() =>
  activeTab.value === 'register' ? 'REGISTER' : activeTab.value === 'forgot' ? 'RESET_PASSWORD' : '',
)

const submitLabel = computed(() => {
  if (activeTab.value === 'register') return '注册并登录'
  if (activeTab.value === 'forgot') return '重置密码并登录'
  return '登录'
})

watch(
  () => props.modelValue,
  (value) => {
    if (!value) {
      resetTransientFields()
      activeTab.value = 'login'
    }
  },
)

function resetTransientFields() {
  form.code = ''
  form.password = ''
  form.confirmPassword = ''
  devCode.value = ''
}

function completeAuth(response) {
  persistAuthSession(response.token, response.user)
  ElMessage.success(response.message)
  emit('success', response.user)
  emit('update:modelValue', false)
}

async function handleSendCode() {
  if (!form.email.trim()) {
    ElMessage.warning('先输入邮箱地址')
    return
  }
  if (!codePurpose.value) {
    return
  }

  sending.value = true
  try {
    const response = await sendAuthCode(form.email.trim(), codePurpose.value)
    devCode.value = response.devCode ?? ''
    ElMessage.success(response.message)
  } catch (error) {
    ElMessage.error(error?.response?.data?.message ?? '验证码发送失败')
  } finally {
    sending.value = false
  }
}

function validatePasswordPair() {
  if (!form.password) {
    ElMessage.warning('请填写密码')
    return false
  }
  if (form.password.length < 8) {
    ElMessage.warning('密码至少 8 位')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return false
  }
  return true
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (!form.email.trim()) {
      ElMessage.warning('请先填写邮箱')
      return
    }

    if (activeTab.value === 'login') {
      if (!form.password) {
        ElMessage.warning('请填写密码')
        return
      }
      completeAuth(await loginAuth({
        email: form.email.trim(),
        password: form.password,
      }))
      return
    }

    if (!form.code.trim()) {
      ElMessage.warning('请填写验证码')
      return
    }

    if (!validatePasswordPair()) {
      return
    }

    if (activeTab.value === 'register') {
      if (!form.nickname.trim()) {
        ElMessage.warning('首次注册需要昵称')
        return
      }
      completeAuth(await registerAuth({
        email: form.email.trim(),
        code: form.code.trim(),
        nickname: form.nickname.trim(),
        password: form.password,
      }))
      return
    }

    completeAuth(await resetPasswordAuth({
      email: form.email.trim(),
      code: form.code.trim(),
      password: form.password,
    }))
  } catch (error) {
    ElMessage.error(error?.response?.data?.message ?? '认证失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="账户中心"
    width="520px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="auth-dialog">
      <p class="auth-dialog__intro">
        现在已经切到真实账号体系：注册需要邮箱验证码，登录使用邮箱和密码，忘记密码也走真实邮箱找回。
      </p>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="密码登录" name="login">
          <el-form label-position="top">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="name@example.com" size="large" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="form.password" placeholder="输入账户密码" show-password size="large" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册账户" name="register">
          <el-form label-position="top">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="name@example.com" size="large" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="给自己起一个昵称" size="large" />
            </el-form-item>
            <el-form-item label="验证码">
              <div class="auth-dialog__code-row">
                <el-input v-model="form.code" placeholder="输入 6 位验证码" size="large" />
                <el-button :loading="sending" round type="primary" @click="handleSendCode">
                  获取验证码
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="设置密码">
              <el-input v-model="form.password" placeholder="至少 8 位" show-password size="large" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="form.confirmPassword" placeholder="再次输入密码" show-password size="large" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="找回密码" name="forgot">
          <el-form label-position="top">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="name@example.com" size="large" />
            </el-form-item>
            <el-form-item label="验证码">
              <div class="auth-dialog__code-row">
                <el-input v-model="form.code" placeholder="输入邮箱验证码" size="large" />
                <el-button :loading="sending" round type="primary" @click="handleSendCode">
                  发送验证码
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="form.password" placeholder="输入新密码" show-password size="large" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="form.confirmPassword" placeholder="再次输入新密码" show-password size="large" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div v-if="devCode" class="auth-dialog__dev-code">
        当前为开发模式，验证码：<strong>{{ devCode }}</strong>
      </div>
    </div>

    <template #footer>
      <el-button round @click="emit('update:modelValue', false)">取消</el-button>
      <el-button :loading="submitting" round type="primary" @click="handleSubmit">
        {{ submitLabel }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.auth-dialog {
  display: grid;
  gap: 10px;
}

.auth-dialog__intro {
  margin: 0 0 6px;
  color: var(--text-muted);
  line-height: 1.75;
}

.auth-dialog :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.auth-dialog__code-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
}

.auth-dialog__dev-code {
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(251, 114, 153, 0.1);
  color: var(--brand-pink-deep);
  font-size: 14px;
}

@media (max-width: 640px) {
  .auth-dialog__code-row {
    grid-template-columns: 1fr;
  }
}
</style>
