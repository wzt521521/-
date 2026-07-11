<script setup>
import { computed, reactive, ref, watchEffect } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '../../components/common/PageContainer.vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const profileRef = ref()
const passwordRef = ref()
const savingProfile = ref(false)
const savingPassword = ref(false)

const roleLabels = {
  ROLE_ADMIN: '系统管理员',
  ROLE_STUDENT: '学生',
  ROLE_TEACHER: '教师',
  ROLE_ANALYST: '数据分析员',
  ROLE_COLLEGE_ADMIN: '学院管理员',
}

const profile = reactive({
  realName: '',
  email: '',
  phone: '',
  college: '',
})
const password = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

watchEffect(() => {
  const current = userStore.userInfo || {}
  profile.realName = current.realName || ''
  profile.email = current.email || ''
  profile.phone = current.phone || ''
  profile.college = current.college || ''
})

const roleNames = computed(() => (userStore.userInfo?.roles || []).map(
  (role) => roleLabels[role] || role,
))
const userInitial = computed(() => userStore.displayName.trim().charAt(0).toUpperCase() || 'U')
const profileCompletion = computed(() => {
  const fields = ['realName', 'email', 'phone', 'college']
  return Math.round((fields.filter((field) => Boolean(profile[field]?.trim())).length / fields.length) * 100)
})
const accountEnabled = computed(() => userStore.userInfo?.status === 1)

const profileRules = {
  email: [{ type: 'email', message: '请输入有效邮箱', trigger: 'blur' }],
}
const passwordRules = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== password.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function saveProfile() {
  if (!await profileRef.value.validate().catch(() => false)) return
  savingProfile.value = true
  try {
    await userStore.updateProfile({
      realName: profile.realName,
      email: profile.email,
      phone: profile.phone,
    })
    ElMessage.success('个人资料已更新')
  } finally {
    savingProfile.value = false
  }
}

async function savePassword() {
  if (!await passwordRef.value.validate().catch(() => false)) return
  savingPassword.value = true
  try {
    await userStore.changePassword({
      currentPassword: password.currentPassword,
      newPassword: password.newPassword,
    })
    password.currentPassword = ''
    password.newPassword = ''
    password.confirmPassword = ''
    passwordRef.value.clearValidate()
    ElMessage.success('密码已更新')
  } finally {
    savingPassword.value = false
  }
}
</script>

<template>
  <PageContainer
    title="个人资料"
    :description="`账号 ${userStore.userInfo?.username || '-'} 的资料与安全设置。`"
    :framed="false"
  >
    <section class="account-overview">
      <div class="account-identity">
        <el-avatar :size="44">
          {{ userInitial }}
        </el-avatar>
        <div>
          <strong>{{ userStore.displayName }}</strong>
          <span>{{ userStore.userInfo?.username || '-' }}</span>
        </div>
      </div>

      <div class="overview-item">
        <span>角色</span>
        <div class="role-list">
          <el-tag
            v-for="role in roleNames"
            :key="role"
            size="small"
            effect="plain"
          >
            {{ role }}
          </el-tag>
          <strong v-if="!roleNames.length">未分配</strong>
        </div>
      </div>

      <div class="overview-item">
        <span>账号状态</span>
        <el-tag :type="accountEnabled ? 'success' : 'danger'" effect="plain">
          {{ accountEnabled ? '已启用' : '已停用' }}
        </el-tag>
      </div>

      <div class="overview-item completion-item">
        <span>资料完整度</span>
        <strong>{{ profileCompletion }}%</strong>
        <el-progress
          :percentage="profileCompletion"
          :stroke-width="6"
          :show-text="false"
        />
      </div>
    </section>

    <div class="settings-grid">
      <section class="settings-panel">
        <header>
          <span>资料设置</span>
          <h2>基本信息</h2>
        </header>
        <el-form
          ref="profileRef"
          :model="profile"
          :rules="profileRules"
          label-position="top"
        >
          <div class="profile-form-grid">
            <el-form-item label="姓名">
              <el-input v-model.trim="profile.realName" autocomplete="name" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model.trim="profile.email" autocomplete="email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model.trim="profile.phone" autocomplete="tel" />
            </el-form-item>
            <el-form-item label="所属学院">
              <el-input v-model="profile.college" disabled placeholder="由管理员维护" />
            </el-form-item>
          </div>
          <div class="panel-actions">
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">
              保存资料
            </el-button>
          </div>
        </el-form>
      </section>

      <section class="settings-panel">
        <header>
          <span>安全设置</span>
          <h2>修改密码</h2>
        </header>
        <el-form
          ref="passwordRef"
          :model="password"
          :rules="passwordRules"
          label-position="top"
        >
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input
              v-model="password.currentPassword"
              type="password"
              show-password
              autocomplete="current-password"
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="password.newPassword"
              type="password"
              show-password
              autocomplete="new-password"
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="password.confirmPassword"
              type="password"
              show-password
              autocomplete="new-password"
            />
          </el-form-item>
          <div class="panel-actions">
            <el-button type="primary" :loading="savingPassword" @click="savePassword">
              更新密码
            </el-button>
          </div>
        </el-form>
      </section>
    </div>
  </PageContainer>
</template>

<style scoped>
.account-overview {
  display: grid;
  grid-template-columns: minmax(220px, 1.2fr) repeat(3, minmax(150px, 1fr));
  min-width: 0;
  margin-bottom: var(--space-4);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-6);
  background: var(--app-surface);
  box-shadow: var(--shadow-xs);
}

.account-identity,
.overview-item {
  min-width: 0;
  padding: var(--space-4) var(--space-5);
}

.account-identity {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.account-identity :deep(.el-avatar) {
  flex: 0 0 auto;
  background: var(--color-brand-100);
  color: var(--color-brand-700);
  font-weight: var(--font-weight-semibold);
}

.account-identity > div,
.overview-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-1);
}

.account-identity strong,
.account-identity span,
.overview-item strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-identity strong,
.overview-item strong {
  color: var(--app-text);
  font-size: var(--font-size-14);
}

.account-identity span,
.overview-item > span {
  color: var(--app-muted);
  font-size: var(--font-size-12);
}

.overview-item {
  border-left: 1px solid var(--app-border);
}

.role-list {
  display: flex;
  min-width: 0;
  flex-wrap: wrap;
  gap: var(--space-1);
}

.completion-item :deep(.el-progress) {
  width: 100%;
  margin-top: var(--space-1);
}

.settings-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(320px, 0.75fr);
  gap: var(--space-4);
}

.settings-panel {
  min-width: 0;
  padding: var(--space-5);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-6);
  background: var(--app-surface);
  box-shadow: var(--shadow-xs);
}

.settings-panel header {
  margin-bottom: var(--space-5);
}

.settings-panel header span {
  color: var(--app-muted);
  font-size: var(--font-size-12);
}

.settings-panel h2 {
  margin: var(--space-1) 0 0;
  color: var(--app-text);
  font-size: var(--font-size-16);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0;
}

.profile-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 var(--space-4);
}

.panel-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--space-4);
  border-top: 1px solid var(--app-border);
}

@media (max-width: 1080px) {
  .account-overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .overview-item:nth-child(3) {
    border-left: 0;
  }

  .overview-item:nth-child(n + 3) {
    border-top: 1px solid var(--app-border);
  }

  .settings-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 620px) {
  .account-overview,
  .profile-form-grid {
    grid-template-columns: 1fr;
  }

  .overview-item,
  .overview-item:nth-child(3) {
    border-top: 1px solid var(--app-border);
    border-left: 0;
  }

  .settings-panel {
    padding: var(--space-4);
  }
}
</style>
