<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DataTableFrame from '../../components/common/DataTableFrame.vue'
import PageContainer from '../../components/common/PageContainer.vue'
import {
  createRole,
  deleteRole,
  fetchPermissionTree,
  fetchRoles,
  replaceRolePermissions,
  updateRole,
} from '../../api/system'

const loading = ref(false)
const saving = ref(false)
const roles = ref([])
const permissionTree = ref([])
const formVisible = ref(false)
const permissionVisible = ref(false)
const editingId = ref(null)
const selectedRole = ref(null)
const formRef = ref()
const treeRef = ref()
const form = reactive({
  roleName: '',
  roleCode: '',
  description: '',
})
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^ROLE_[A-Z0-9_]+$/, message: '角色编码必须以 ROLE_ 开头', trigger: 'blur' },
  ],
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    [roles.value, permissionTree.value] = await Promise.all([
      fetchRoles(),
      fetchPermissionTree(),
    ])
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { roleName: '', roleCode: 'ROLE_', description: '' })
  formVisible.value = true
}

function openEdit(role) {
  editingId.value = role.id
  Object.assign(form, {
    roleName: role.roleName,
    roleCode: role.roleCode,
    description: role.description || '',
  })
  formVisible.value = true
}

async function saveRole() {
  if (!await formRef.value.validate().catch(() => false)) return
  saving.value = true
  try {
    if (editingId.value) await updateRole(editingId.value, form)
    else await createRole(form)
    formVisible.value = false
    ElMessage.success(editingId.value ? '角色已更新' : '角色已创建')
    await load()
  } finally {
    saving.value = false
  }
}

async function openPermissions(role) {
  selectedRole.value = role
  permissionVisible.value = true
  await nextTick()
  treeRef.value.setCheckedKeys(role.permissionIds || [])
}

async function savePermissions() {
  saving.value = true
  try {
    const checked = treeRef.value.getCheckedKeys(false)
    await replaceRolePermissions(selectedRole.value.id, checked)
    permissionVisible.value = false
    ElMessage.success('权限已保存')
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(role) {
  await ElMessageBox.confirm(
    `确定删除角色 ${role.roleName}？`,
    '删除角色',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
  )
  await deleteRole(role.id)
  ElMessage.success('角色已删除')
  await load()
}
</script>

<template>
  <PageContainer
    title="角色权限"
    description="维护角色定义及其可访问的菜单和操作权限。"
  >
    <template #actions>
      <el-button
        v-permission="'role:update'"
        type="primary"
        :icon="Plus"
        @click="openCreate"
      >
        新建角色
      </el-button>
    </template>

    <DataTableFrame
      :data="roles"
      :loading="loading"
      :pagination="false"
      empty-title="暂无角色"
      empty-description="当前没有可用的角色定义"
    >
      <template #toolbar>
        <span class="result-count">共 {{ roles.length }} 个角色</span>
      </template>
      <el-table-column label="角色" min-width="190">
        <template #default="{ row }">
          <div class="role-cell">
            <strong>{{ row.roleName }}</strong>
            <el-tag size="small" type="info" effect="plain">
              {{ row.roleCode }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="description"
        label="说明"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column label="权限摘要" width="120">
        <template #default="{ row }">
          <span class="permission-count">
            <strong>{{ row.permissionIds?.length || 0 }}</strong> 项权限
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-tooltip content="分配权限">
              <el-button
                v-permission="'role:update'"
                text
                :icon="Setting"
                @click="openPermissions(row)"
              />
            </el-tooltip>
            <el-tooltip content="编辑角色">
              <el-button
                v-permission="'role:update'"
                text
                :icon="Edit"
                @click="openEdit(row)"
              />
            </el-tooltip>
            <el-tooltip content="删除角色">
              <el-button
                v-permission="'role:update'"
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
      v-model="formVisible"
      :title="editingId ? '编辑角色' : '新建角色'"
      width="min(520px, 92vw)"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model.trim="form.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model.trim="form.roleCode" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model.trim="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="saving" @click="saveRole">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="permissionVisible"
      :title="`分配权限 - ${selectedRole?.roleName || ''}`"
      size="min(480px, 92vw)"
    >
      <div class="permission-context">
        <el-tag type="info" effect="plain">
          {{ selectedRole?.roleCode }}
        </el-tag>
        <span>当前已分配 {{ selectedRole?.permissionIds?.length || 0 }} 项权限</span>
      </div>
      <div class="permission-tree">
        <el-tree
          ref="treeRef"
          :data="permissionTree"
          node-key="id"
          show-checkbox
          default-expand-all
          :props="{ label: 'permissionName', children: 'children' }"
        />
      </div>
      <template #footer>
        <el-button @click="permissionVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="saving" @click="savePermissions">
          保存权限
        </el-button>
      </template>
    </el-drawer>
  </PageContainer>
</template>

<style scoped>
.result-count,
.permission-context,
.permission-count {
  color: var(--app-muted);
  font-size: var(--font-size-12);
}

.role-cell {
  display: flex;
  min-width: 0;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.role-cell strong {
  color: var(--app-text);
  font-weight: var(--font-weight-semibold);
}

.permission-count strong {
  color: var(--app-text);
  font-size: var(--font-size-14);
}

.permission-context {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--app-border);
}

.permission-tree {
  min-height: 240px;
}
</style>
