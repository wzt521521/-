<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { getLoginErrorMessage } from '../../utils/loginError'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  username: '',
  password: '',
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/')
      ? route.query.redirect
      : '/dashboard'
    await router.replace(redirect)
  } catch (error) {
    ElMessage.error(getLoginErrorMessage(error))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <aside class="auth-brand">
      <div class="brand-lockup">
        <div class="brand-mark">
          CA
        </div>
        <span>CAREER ABILITY PLATFORM</span>
      </div>

      <div class="brand-copy">
        <p>职业能力大数据服务平台</p>
        <h1>管理控制台</h1>
      </div>

      <small>统一身份认证</small>
    </aside>

    <section class="auth-form-panel">
      <div class="auth-form-wrap">
        <header>
          <span class="form-kicker">账号登录</span>
          <h2>登录平台</h2>
        </header>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          @keyup.enter="submit"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model.trim="form.username"
              :prefix-icon="User"
              autocomplete="username"
              placeholder="请输入用户名"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              :prefix-icon="Lock"
              type="password"
              show-password
              autocomplete="current-password"
              placeholder="请输入密码"
            />
          </el-form-item>
          <el-button
            type="primary"
            class="submit-button"
            :loading="submitting"
            @click="submit"
          >
            登录
          </el-button>
        </el-form>

        <div class="auth-switch">
          <span>还没有账号？</span>
          <RouterLink to="/register">
            创建账号
          </RouterLink>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.auth-page {
  display: grid;
  min-height: 100vh;
  grid-template-columns: minmax(340px, 44%) minmax(420px, 1fr);
  background: var(--app-surface);
}

.auth-brand {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  justify-content: space-between;
  padding: var(--space-10) var(--space-12);
  background: var(--color-neutral-900);
  color: var(--color-neutral-0);
}

.brand-lockup {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  color: var(--color-neutral-300);
  font-size: 11px;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0;
}

.brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: var(--radius-6);
  background: var(--color-brand-600);
  color: white;
  font-size: var(--font-size-13);
  font-weight: var(--font-weight-bold);
}

.brand-copy {
  max-width: 440px;
}

.brand-copy p {
  margin: 0 0 var(--space-2);
  color: var(--color-neutral-300);
  font-size: var(--font-size-16);
}

.brand-copy h1 {
  margin: 0;
  color: white;
  font-size: 36px;
  font-weight: var(--font-weight-semibold);
  line-height: var(--line-height-tight);
  letter-spacing: 0;
}

.auth-brand > small {
  color: var(--color-neutral-400);
  font-size: var(--font-size-12);
}

.auth-form-panel {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: var(--space-10);
  border-left: 1px solid var(--app-border);
  background: var(--app-surface);
}

.auth-form-wrap {
  width: min(100%, 380px);
}

.auth-form-wrap header {
  margin-bottom: var(--space-8);
}

.form-kicker {
  color: var(--app-accent);
  font-size: var(--font-size-12);
  font-weight: var(--font-weight-semibold);
}

.auth-form-wrap h2 {
  margin: var(--space-2) 0 0;
  color: var(--app-text);
  font-size: var(--font-size-28);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0;
}

.submit-button {
  width: 100%;
  margin-top: var(--space-2);
}

.auth-switch {
  display: flex;
  justify-content: center;
  gap: var(--space-1);
  margin-top: var(--space-6);
  color: var(--app-muted);
  font-size: var(--font-size-14);
}

.auth-switch a {
  color: var(--app-accent);
  font-weight: var(--font-weight-semibold);
}

@media (max-width: 820px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-brand {
    min-height: 176px;
    padding: var(--space-6);
  }

  .brand-copy h1 {
    font-size: var(--font-size-28);
  }

  .auth-brand > small {
    display: none;
  }

  .auth-form-panel {
    min-height: calc(100vh - 176px);
    place-items: start center;
    padding: var(--space-8) var(--space-5);
    border-left: 0;
  }
}
</style>
