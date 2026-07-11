<script setup>
import { onMounted, reactive, ref } from 'vue'
import { CopyDocument, Delete, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DataTableFrame from '../../components/common/DataTableFrame.vue'
import FilterBar from '../../components/common/FilterBar.vue'
import PageContainer from '../../components/common/PageContainer.vue'
import {
  createApiKey,
  deleteApiKey,
  fetchApiKeys,
  updateApiKeyStatus,
} from '../../api/openApi'

const loading = ref(false)
const saving = ref(false)
const keys = ref([])
const total = ref(0)
const createVisible = ref(false)
const secretVisible = ref(false)
const createdSecret = ref('')
const formRef = ref()
const query = reactive({ appName: '', page: 0, size: 20 })
const form = reactive({ appName: '', rateLimit: 100, expireTime: null })
const rules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  rateLimit: [{ required: true, message: '请输入限流值', trigger: 'blur' }],
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const result = await fetchApiKeys(query)
    keys.value = result.content
    total.value = result.totalElements
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 0
  load()
}

function reset() {
  Object.assign(query, { appName: '', page: 0 })
  load()
}

function expiryState(expireTime) {
  if (!expireTime) return { label: '长期有效', type: 'info' }
  if (new Date(expireTime).getTime() < Date.now()) return { label: '已过期', type: 'danger' }
  return { label: '有效期内', type: 'success' }
}

function openCreate() {
  Object.assign(form, { appName: '', rateLimit: 100, expireTime: null })
  createVisible.value = true
}

async function save() {
  if (!await formRef.value.validate().catch(() => false)) return
  saving.value = true
  try {
    const result = await createApiKey({
      appName: form.appName,
      rateLimit: form.rateLimit,
      expireTime: form.expireTime || null,
    })
    createdSecret.value = result.apiKey
    createVisible.value = false
    secretVisible.value = true
    await load()
  } finally {
    saving.value = false
  }
}

async function changeStatus(apiKey) {
  const nextStatus = apiKey.status
  try {
    await updateApiKeyStatus(apiKey.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? 'API Key 已启用' : 'API Key 已禁用')
  } catch (error) {
    apiKey.status = nextStatus === 1 ? 0 : 1
    throw error
  }
}

async function remove(apiKey) {
  await ElMessageBox.confirm(
    `确定删除 ${apiKey.appName} 的 API Key？`,
    '删除 API Key',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
  )
  await deleteApiKey(apiKey.id)
  ElMessage.success('API Key 已删除')
  await load()
}

async function copySecret() {
  await navigator.clipboard.writeText(createdSecret.value)
  ElMessage.success('已复制')
}

function closeSecret() {
  createdSecret.value = ''
  secretVisible.value = false
}
</script>

<template>
  <PageContainer
    title="API Key"
    description="管理开放接口的调用凭证、限流额度和有效期。"
  >
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="openCreate">
        创建 API Key
      </el-button>
    </template>

    <FilterBar label="API Key 筛选">
      <el-input
        v-model.trim="query.appName"
        placeholder="搜索应用名称"
        clearable
        style="width: 260px"
        @keyup.enter="search"
      />
      <template #actions>
        <el-button :icon="Search" @click="search">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="reset">
          重置
        </el-button>
      </template>
      <template #trailing>
        <span class="result-count">共 {{ total }} 个凭证</span>
      </template>
    </FilterBar>

    <DataTableFrame
      :data="keys"
      :loading="loading"
      :page="query.page + 1"
      :page-size="query.size"
      :total="total"
      empty-title="暂无 API Key"
      empty-description="当前没有匹配的 API 调用凭证"
      @page-change="(value) => { query.page = value - 1; load() }"
      @page-size-change="(value) => { query.size = value; query.page = 0; load() }"
    >
      <el-table-column label="应用" min-width="210">
        <template #default="{ row }">
          <div class="app-cell">
            <strong>{{ row.appName }}</strong>
            <small>Key #{{ row.id }} · 创建于 {{ row.createTime || '-' }}</small>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="所属用户" width="100">
        <template #default="{ row }">
          #{{ row.userId }}
        </template>
      </el-table-column>
      <el-table-column label="限流额度" width="120">
        <template #default="{ row }">
          {{ row.rateLimit }} / 分钟
        </template>
      </el-table-column>
      <el-table-column prop="totalCalls" label="累计调用" width="110" />
      <el-table-column label="有效期" min-width="180">
        <template #default="{ row }">
          <div class="expiry-cell">
            <el-tag
              :type="expiryState(row.expireTime).type"
              effect="plain"
              size="small"
            >
              {{ expiryState(row.expireTime).label }}
            </el-tag>
            <small>{{ row.expireTime || '无固定期限' }}</small>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <div class="status-control">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :aria-label="row.status === 1 ? '禁用 API Key' : '启用 API Key'"
              @change="changeStatus(row)"
            />
            <span :class="{ disabled: row.status !== 1 }">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button
            text
            type="danger"
            :icon="Delete"
            title="删除 API Key"
            @click="remove(row)"
          />
        </template>
      </el-table-column>
    </DataTableFrame>

    <el-dialog v-model="createVisible" title="创建 API Key" width="min(500px, 92vw)">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model.trim="form.appName" maxlength="100" />
        </el-form-item>
        <el-form-item label="每分钟调用上限" prop="rateLimit">
          <el-input-number
            v-model="form.rateLimit"
            :min="1"
            :max="10000"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="留空表示永不过期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="saving" @click="save">
          创建
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      :model-value="secretVisible"
      title="API Key 已创建"
      width="min(560px, 92vw)"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <el-alert
        title="完整密钥只显示一次，关闭后无法再次查看。"
        type="warning"
        :closable="false"
        show-icon
        class="secret-warning"
      />
      <div class="secret-value">
        <code>{{ createdSecret }}</code>
        <el-button :icon="CopyDocument" title="复制密钥" @click="copySecret" />
      </div>
      <template #footer>
        <el-button type="primary" @click="closeSecret">
          我已保存
        </el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.result-count,
.app-cell small,
.expiry-cell small {
  color: var(--app-muted);
  font-size: var(--font-size-12);
}

.app-cell,
.expiry-cell {
  display: flex;
  min-width: 0;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-1);
}

.app-cell strong,
.app-cell small,
.expiry-cell small {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-cell strong {
  color: var(--app-text);
  font-weight: var(--font-weight-semibold);
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

.secret-warning {
  margin-bottom: var(--space-3);
}

.secret-value {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 40px;
  gap: 8px;
  align-items: center;
}

.secret-value code {
  overflow-wrap: anywhere;
  padding: 12px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-4);
  background: var(--app-surface-muted);
  font-size: 13px;
}
</style>
