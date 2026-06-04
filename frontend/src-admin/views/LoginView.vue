<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { Lock, Message } from '@element-plus/icons-vue'
import { persistAdminSession } from '../stores/auth'
import { loginAuth, registerAuth, resetPasswordAuth, sendAuthCode } from '../api/blog'

const router = useRouter()
const sending = ref(false)
const submitting = ref(false)
const activeTab = ref('login')
const devCode = ref('')

const form = reactive({
  email: '',
  code: '',
  nickname: '',
  password: '',
  confirmPassword: '',
})

const actionLabel = computed(() => {
  if (activeTab.value === 'register') return '注册管理员并登录'
  if (activeTab.value === 'forgot') return '重置密码并登录'
  return '登录控制台'
})
const siteUrl = computed(() => {
  const { protocol, hostname } = window.location
  return `${protocol}//${hostname}:5173`
})
const codePurpose = computed(() => (activeTab.value === 'register' ? 'REGISTER' : 'RESET_PASSWORD'))

async function handleSendCode() {
  if (!form.email.trim()) {
    ElMessage.warning('请先输入管理员邮箱')
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

function completeAuth(response) {
  if (response.user?.role !== 'ADMIN') {
    ElMessage.error('当前账号不是管理员，不能进入控制台')
    return false
  }

  persistAdminSession(response.token, response.user)
  ElMessage.success(response.message)
  router.push({ name: 'admin-dashboard' })
  return true
}

async function handleSubmit() {
  if (!form.email.trim()) {
    ElMessage.warning('请先输入管理员邮箱')
    return
  }

  submitting.value = true
  try {
    if (activeTab.value === 'login') {
      if (!form.password) {
        ElMessage.warning('请输入密码')
        return
      }
      completeAuth(await loginAuth({
        email: form.email.trim(),
        password: form.password,
      }))
      return
    }

    if (activeTab.value === 'register') {
      if (!form.code.trim()) {
        ElMessage.warning('请输入验证码')
        return
      }
      if (!form.nickname.trim()) {
        ElMessage.warning('请输入管理员昵称')
        return
      }
      if (!form.password || form.password.length < 8) {
        ElMessage.warning('密码至少 8 位')
        return
      }
      if (form.password !== form.confirmPassword) {
        ElMessage.warning('两次输入的密码不一致')
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

    if (!form.code.trim()) {
      ElMessage.warning('请输入验证码')
      return
    }
    if (!form.password || form.password.length < 8) {
      ElMessage.warning('新密码至少 8 位')
      return
    }
    if (form.password !== form.confirmPassword) {
      ElMessage.warning('两次输入的密码不一致')
      return
    }
    completeAuth(await resetPasswordAuth({
      email: form.email.trim(),
      code: form.code.trim(),
      password: form.password,
    }))
  } catch (error) {
    ElMessage.error(error?.response?.data?.message ?? '登录失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login-shell">
    <section class="login-hero">
      <div class="login-hero__badge">BiliBlog Console</div>
      <h1>把后台独立出来，像 Halo 一样专注做内容管理。</h1>
      <p>
        这里是独立的管理控制台入口。站点文章、用户管理、公告推送和数据概览都放在这一侧，
        博客前台只保留对访客友好的阅读与互动体验。
      </p>

      <div class="hero-grid">
        <article>
          <strong>文章工作台</strong>
          <span>主站文章发布、编辑、下线统一在后台完成</span>
        </article>
        <article>
          <strong>管理员认证</strong>
          <span>管理员注册、登录和找回密码都集中在这里</span>
        </article>
        <article>
          <strong>公告推送</strong>
          <span>站点通知和运营信息集中维护</span>
        </article>
      </div>
    </section>

    <section class="console-card login-panel">
      <div class="login-panel__head">
        <span class="login-panel__eyebrow">管理员入口</span>
        <h2>管理员控制台认证</h2>
        <p>前台已经不再开放普通用户注册和投稿，博客管理统一由管理员在这里完成。</p>
      </div>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="密码登录" name="login">
          <el-form label-position="top">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="admin@example.com" size="large">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码">
              <el-input v-model="form.password" placeholder="输入管理员密码" show-password size="large">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="管理员注册" name="register">
          <el-form label-position="top">
            <el-form-item label="管理员邮箱">
              <el-input v-model="form.email" placeholder="admin@example.com" size="large">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="验证码">
              <div class="code-row">
                <el-input v-model="form.code" placeholder="输入邮箱验证码" size="large" />
                <el-button :loading="sending" round type="primary" @click="handleSendCode">
                  发送验证码
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="管理员昵称">
              <el-input v-model="form.nickname" placeholder="例如：站长" size="large" />
            </el-form-item>

            <el-form-item label="密码">
              <el-input v-model="form.password" placeholder="至少 8 位" show-password size="large" />
            </el-form-item>

            <el-form-item label="确认密码">
              <el-input v-model="form.confirmPassword" placeholder="再次输入密码" show-password size="large" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="找回密码" name="forgot">
          <el-form label-position="top">
            <el-form-item label="管理员邮箱">
              <el-input v-model="form.email" placeholder="admin@example.com" size="large">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="验证码">
              <div class="code-row">
                <el-input v-model="form.code" placeholder="输入邮箱验证码" size="large" />
                <el-button :loading="sending" round type="primary" @click="handleSendCode">
                  发送验证码
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="新密码">
              <el-input v-model="form.password" placeholder="至少 8 位" show-password size="large" />
            </el-form-item>

            <el-form-item label="确认新密码">
              <el-input v-model="form.confirmPassword" placeholder="再次输入新密码" show-password size="large" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div v-if="devCode" class="dev-code-box">
        当前为开发模式验证码：<strong>{{ devCode }}</strong>
      </div>

      <div class="login-panel__actions">
        <a :href="siteUrl" target="_blank" rel="noopener">打开博客前台</a>
        <el-button :loading="submitting" round size="large" type="primary" @click="handleSubmit">
          {{ actionLabel }}
        </el-button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(420px, 520px);
  gap: 28px;
  align-items: center;
  min-height: 100vh;
  padding: 32px;
}

.login-hero {
  padding: 28px;
}

.login-hero__badge,
.login-panel__eyebrow {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
  color: var(--console-primary-deep);
  font-size: 13px;
  font-weight: 700;
}

.login-hero h1,
.login-panel h2 {
  margin: 18px 0 0;
  font-family: var(--console-display);
  line-height: 1.15;
}

.login-hero h1 {
  max-width: 760px;
  font-size: 54px;
}

.login-panel h2 {
  font-size: 32px;
}

.login-hero p,
.login-panel p {
  color: var(--console-text-muted);
  line-height: 1.85;
}

.hero-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 28px;
}

.hero-grid article {
  padding: 18px;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.74), rgba(255, 255, 255, 0.94));
  box-shadow: var(--console-shadow);
}

.hero-grid strong,
.hero-grid span {
  display: block;
}

.hero-grid span {
  margin-top: 10px;
  color: var(--console-text-muted);
  line-height: 1.7;
}

.login-panel {
  padding: 28px;
}

.login-panel__head {
  margin-bottom: 10px;
}

.login-panel :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.code-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
}

.dev-code-box {
  margin-top: 12px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(59, 130, 246, 0.08);
  color: var(--console-primary-deep);
}

.login-panel__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 22px;
}

.login-panel__actions a {
  color: var(--console-text-muted);
}

@media (max-width: 1440px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .hero-grid {
    grid-template-columns: 1fr;
  }
}
</style>
