<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Delete, Download, Edit, Key, Plus, Refresh, Search, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DataTableFrame from '../../components/common/DataTableFrame.vue'
import FilterBar from '../../components/common/FilterBar.vue'
import PageContainer from '../../components/common/PageContainer.vue'
import {
  createUser,
  deleteUser,
  downloadUserImportTemplate,
  fetchRoles,
  fetchUsers,
  importUsers,
  resetUserPassword,
  updateUser,
  updateUserStatus,
} from '../../api/system'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const importResultVisible = ref(false)
const importing = ref(false)
const importResult = ref(null)
const formRef = ref()
const editingId = ref(null)
const users = ref([])
const roles = ref([])
const query = reactive({
  keyword: '',
  page: 0,
  size: 20,
})
const total = ref(0)
const form = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  college: '',
  roleCodes: [],
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]{4,20}$/, message: '请输入 4-20 位字母、数字或下划线', trigger: 'blur' },
  ],
  password: [{
    validator: (_rule, value, callback) => {
      if (!editingId.value && (!value || value.length < 6)) callback(new Error('密码至少 6 位'))
      else callback()
    },
    trigger: 'blur',
  }],
  email: [{ type: 'email', message: '请输入有效邮箱', trigger: 'blur' }],
  roleCodes: [{ type: 'array', required: true, min: 1, message: '至少选择一个角色', trigger: 'change' }],
}

onMounted(async () => {
  await Promise.all([loadUsers(), loadRoles()])
})

async function loadUsers() {
  loading.value = true
  try {
    const result = await fetchUsers(query)
    users.value = result.content
    total.value = result.totalElements
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  roles.value = await fetchRoles()
}

function search() {
  query.page = 0
  loadUsers()
}

function resetSearch() {
  query.keyword = ''
  query.page = 0
  loadUsers()
}

function roleName(roleCode) {
  return roles.value.find((role) => role.roleCode === roleCode)?.roleName || roleCode
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    college: '',
    roleCodes: ['ROLE_STUDENT'],
  })
  dialogVisible.value = true
}

function openEdit(user) {
  editingId.value = user.id
  Object.assign(form, {
    username: user.username,
    password: '',
    realName: user.realName || '',
    email: user.email || '',
    phone: user.phone || '',
    college: user.college || '',
    roleCodes: [...user.roles],
  })
  dialogVisible.value = true
}

async function save() {
  if (!await formRef.value.validate().catch(() => false)) return
  saving.value = true
  try {
    const payload = {
      realName: form.realName,
      email: form.email,
      phone: form.phone,
      college: form.college,
      roleCodes: form.roleCodes,
    }
    if (editingId.value) {
      await updateUser(editingId.value, payload)
    } else {
      await createUser({ ...payload, username: form.username, password: form.password })
    }
    ElMessage.success(editingId.value ? '用户已更新' : '用户已创建')
    dialogVisible.value = false
    await loadUsers()
  } finally {
    saving.value = false
  }
}

async function changeStatus(user) {
  const nextStatus = user.status
  try {
    await updateUserStatus(user.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '账号已启用' : '账号已停用')
  } catch (error) {
    user.status = nextStatus === 1 ? 0 : 1
    throw error
  }
}

async function resetPassword(user) {
  const { value } = await ElMessageBox.prompt(
    `为 ${user.username} 设置新密码`,
    '重置密码',
    {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPattern: /^.{6,20}$/,
      inputErrorMessage: '密码长度为 6-20 位',
    },
  )
  await resetUserPassword(user.id, value)
  ElMessage.success('密码已重置')
}

async function remove(user) {
  await ElMessageBox.confirm(
    `确定删除用户 ${user.username}？该操作不可撤销。`,
    '删除用户',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
  )
  await deleteUser(user.id)
  ElMessage.success('用户已删除')
  await loadUsers()
}

async function handleImport(uploadFile) {
  const file = uploadFile.raw
  if (!file?.name.toLowerCase().endsWith('.xlsx')) {
    ElMessage.error('请选择 .xlsx 文件')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('文件不能超过 2 MB')
    return
  }

  importing.value = true
  try {
    importResult.value = await importUsers(file)
    importResultVisible.value = true
    await loadUsers()
  } finally {
    importing.value = false
  }
}

async function downloadTemplate() {
  const blob = await downloadUserImportTemplate()
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'user-import-template.xlsx'
  link.click()
  URL.revokeObjectURL(url)
}
</script>

<template>
  <PageContainer
    title="用户管理"
    description="管理平台账号、角色归属与启用状态。"
  >
    <template #actions>
      <el-button
        v-permission="'user:create'"
        :icon="Download"
        @click="downloadTemplate"
      >
        下载模板
      </el-button>
      <el-upload
        accept=".xlsx"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleImport"
      >
        <el-button
          v-permission="'user:create'"
          :icon="Upload"
          :loading="importing"
        >
          批量导入
        </el-button>
      </el-upload>
      <el-button
        v-permission="'user:create'"
        type="primary"
        :icon="Plus"
        @click="openCreate"
      >
        新建用户
      </el-button>
    </template>

    <FilterBar label="用户筛选">
      <el-input
        v-model.trim="query.keyword"
        placeholder="搜索用户名"
        clearable
        style="width: 260px"
        @keyup.enter="search"
      />
      <template #actions>
        <el-button :icon="Search" @click="search">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="resetSearch">
          重置
        </el-button>
      </template>
      <template #trailing>
        <span class="result-count">共 {{ total }} 位用户</span>
      </template>
    </FilterBar>

    <DataTableFrame
      :data="users"
      :loading="loading"
      :page="query.page + 1"
      :page-size="query.size"
      :total="total"
      empty-title="暂无用户"
      empty-description="当前筛选条件下没有匹配的用户"
      @page-change="(value) => { query.page = value - 1; loadUsers() }"
      @page-size-change="(value) => { query.size = value; query.page = 0; loadUsers() }"
    >
      <el-table-column label="用户" min-width="190">
        <template #default="{ row }">
          <div class="user-cell">
            <strong>{{ row.username }}</strong>
            <small>{{ row.email || '未填写邮箱' }}</small>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="realName" label="姓名" min-width="110" />
      <el-table-column
        prop="college"
        label="学院"
        min-width="150"
        show-overflow-tooltip
      />
      <el-table-column label="角色" min-width="180">
        <template #default="{ row }">
          <el-tag
            v-for="role in row.roles"
            :key="role"
            size="small"
            effect="plain"
            class="role-tag"
          >
            {{ roleName(role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <div class="status-control">
            <el-switch
              v-model="row.status"
              v-permission="'user:update'"
              :active-value="1"
              :inactive-value="0"
              :aria-label="row.status === 1 ? '停用账号' : '启用账号'"
              @change="changeStatus(row)"
            />
            <span :class="{ disabled: row.status !== 1 }">
              {{ row.status === 1 ? '启用' : '停用' }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-tooltip content="编辑用户">
              <el-button
                v-permission="'user:update'"
                text
                :icon="Edit"
                @click="openEdit(row)"
              />
            </el-tooltip>
            <el-tooltip content="重置密码">
              <el-button
                v-permission="'user:update'"
                text
                :icon="Key"
                @click="resetPassword(row)"
              />
            </el-tooltip>
            <el-tooltip content="删除用户">
              <el-button
                v-permission="'user:delete'"
                text
                type="danger"
                :icon="Delete"
                @click="remove(row)"
              />
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </DataTableFrame>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑用户' : '新建用户'"
      width="min(620px, 92vw)"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <section class="dialog-section">
          <h3>账号信息</h3>
          <div class="dialog-grid">
            <el-form-item label="用户名" prop="username">
              <el-input v-model.trim="form.username" :disabled="Boolean(editingId)" />
            </el-form-item>
            <el-form-item v-if="!editingId" label="初始密码" prop="password">
              <el-input v-model="form.password" type="password" show-password />
            </el-form-item>
            <el-form-item label="角色" prop="roleCodes" class="full-width">
              <el-select v-model="form.roleCodes" multiple style="width: 100%">
                <el-option
                  v-for="role in roles"
                  :key="role.roleCode"
                  :label="role.roleName"
                  :value="role.roleCode"
                />
              </el-select>
            </el-form-item>
          </div>
        </section>
        <section class="dialog-section">
          <h3>个人信息</h3>
          <div class="dialog-grid">
            <el-form-item label="姓名">
              <el-input v-model.trim="form.realName" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model.trim="form.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model.trim="form.phone" />
            </el-form-item>
            <el-form-item label="所属学院">
              <el-input v-model.trim="form.college" />
            </el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="saving" @click="save">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="importResultVisible"
      title="导入结果"
      width="min(720px, 92vw)"
    >
      <div v-if="importResult" class="import-summary">
        <el-alert
          :type="importResult.failedRows ? 'warning' : 'success'"
          :closable="false"
          show-icon
        >
          共读取 {{ importResult.totalRows }} 行，成功 {{ importResult.importedRows }} 行，
          失败 {{ importResult.failedRows }} 行
        </el-alert>
        <el-table
          v-if="importResult.errors?.length"
          :data="importResult.errors"
          max-height="360"
          stripe
        >
          <el-table-column prop="row" label="行号" width="80" />
          <el-table-column prop="username" label="用户名" min-width="130" />
          <el-table-column prop="message" label="失败原因" min-width="280" />
        </el-table>
      </div>
      <template #footer>
        <el-button type="primary" @click="importResultVisible = false">
          完成
        </el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.result-count,
.user-cell small {
  color: var(--app-muted);
  font-size: var(--font-size-12);
}

.user-cell {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.user-cell strong,
.user-cell small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-cell strong {
  color: var(--app-text);
  font-weight: var(--font-weight-semibold);
}

.role-tag {
  margin: 2px 4px 2px 0;
}

.status-control {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.status-control span {
  color: var(--app-success);
  font-size: var(--font-size-12);
}

.status-control span.disabled {
  color: var(--app-muted);
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.dialog-section + .dialog-section {
  margin-top: var(--space-2);
  padding-top: var(--space-4);
  border-top: 1px solid var(--app-border);
}

.dialog-section h3 {
  margin: 0 0 var(--space-3);
  color: var(--app-text);
  font-size: var(--font-size-14);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0;
}

.full-width {
  grid-column: 1 / -1;
}

.import-summary {
  display: grid;
  gap: 16px;
}

@media (max-width: 560px) {
  .dialog-grid {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: auto;
  }
}
</style>
