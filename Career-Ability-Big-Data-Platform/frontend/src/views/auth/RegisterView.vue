<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  roleCode: 'ROLE_STUDENT',
  realName: '',
  email: '',
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]{4,20}$/, message: '请输入 4-20 位字母、数字或下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  roleCode: [{ required: true, message: '请选择账号类型', trigger: 'change' }],
  email: [{ type: 'email', message: '请输入有效邮箱', trigger: 'blur' }],
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await userStore.register({
      username: form.username,
      password: form.password,
      roleCode: form.roleCode,
      realName: form.realName,
      email: form.email,
    })
    ElMessage.success('注册成功，请登录')
    await router.replace('/login')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="register-page">
    <aside class="register-brand">
      <div class="brand-lockup">
        <div class="brand-mark">
          CA
        </div>
        <span>CAREER ABILITY PLATFORM</span>
      </div>
      <div class="brand-copy">
        <p>职业能力大数据服务平台</p>
        <strong>平台账号</strong>
      </div>
    </aside>

    <section class="register-content">
      <div class="register-wrap">
        <header class="register-header">
          <div>
            <span>创建账号</span>
            <h1>注册平台账号</h1>
          </div>
          <RouterLink to="/login" class="back-link">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回登录</span>
          </RouterLink>
        </header>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="register-form"
        >
          <section class="form-section">
            <h2>账号信息</h2>
            <div class="form-grid">
              <el-form-item label="账号类型" prop="roleCode" class="full-width">
                <el-segmented
                  v-model="form.roleCode"
                  :options="[
                    { label: '学生', value: 'ROLE_STUDENT' },
                    { label: '教师', value: 'ROLE_TEACHER' },
                  ]"
                  block
                />
              </el-form-item>
              <el-form-item label="用户名" prop="username" class="full-width">
                <el-input
                  v-model.trim="form.username"
                  autocomplete="username"
                  placeholder="4-20 位字母、数字或下划线"
                />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  show-password
                  autocomplete="new-password"
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  show-password
                  autocomplete="new-password"
                />
              </el-form-item>
            </div>
          </section>

          <section class="form-section">
            <h2>个人信息 <small>选填</small></h2>
            <div class="form-grid">
              <el-form-item label="姓名" prop="realName">
                <el-input v-model.trim="form.realName" autocomplete="name" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model.trim="form.email" autocomplete="email" />
              </el-form-item>
            </div>
          </section>

          <div class="form-actions">
            <el-button
              type="primary"
              size="large"
              class="register-button"
              :loading="submitting"
              @click="submit"
            >
              创建账号
            </el-button>
          </div>
        </el-form>
      </div>
    </section>
  </main>
</template>

<style scoped>
.register-page {
  display: grid;
  min-height: 100vh;
  grid-template-columns: 300px minmax(0, 1fr);
  background: var(--app-surface);
}

.register-brand {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  justify-content: space-between;
  padding: var(--space-8);
  background: var(--color-neutral-900);
  color: white;
}

.brand-lockup {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  color: var(--color-neutral-300);
  font-size: 11px;
  font-weight: var(--font-weight-semibold);
}

.brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: var(--radius-6);
  background: var(--color-brand-600);
  font-size: var(--font-size-13);
  font-weight: var(--font-weight-bold);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.brand-copy p,
.brand-copy strong {
  margin: 0;
}

.brand-copy p {
  color: var(--color-neutral-300);
  font-size: var(--font-size-14);
}

.brand-copy strong {
  font-size: var(--font-size-22);
  font-weight: var(--font-weight-semibold);
}

.register-content {
  display: grid;
  place-items: center;
  padding: var(--space-10) var(--space-6);
}

.register-wrap {
  width: min(100%, 760px);
}

.register-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-5);
  margin-bottom: var(--space-6);
}

.register-header > div > span {
  color: var(--app-accent);
  font-size: var(--font-size-12);
  font-weight: var(--font-weight-semibold);
}

.register-header h1 {
  margin: var(--space-2) 0 0;
  color: var(--app-text);
  font-size: var(--font-size-28);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0;
}

.back-link {
  display: inline-flex;
  min-height: var(--control-height-md);
  align-items: center;
  gap: var(--space-1);
  color: var(--app-accent);
  font-size: var(--font-size-13);
  font-weight: var(--font-weight-medium);
}

.form-section {
  padding: var(--space-5) 0;
  border-top: 1px solid var(--app-border);
}

.form-section h2 {
  margin: 0 0 var(--space-4);
  color: var(--app-text);
  font-size: var(--font-size-16);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0;
}

.form-section h2 small {
  margin-left: var(--space-1);
  color: var(--app-muted);
  font-size: var(--font-size-12);
  font-weight: var(--font-weight-regular);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 var(--space-5);
}

.full-width {
  grid-column: 1 / -1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--space-4);
  border-top: 1px solid var(--app-border);
}

.register-button {
  min-width: 180px;
}

@media (max-width: 820px) {
  .register-page {
    grid-template-columns: 1fr;
  }

  .register-brand {
    min-height: 112px;
    padding: var(--space-5);
  }

  .brand-copy {
    display: none;
  }

  .register-content {
    place-items: start center;
    padding: var(--space-6) var(--space-5);
  }
}

@media (max-width: 560px) {
  .register-header {
    flex-direction: column-reverse;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: auto;
  }

  .form-actions,
  .register-button {
    width: 100%;
  }
}
</style>
