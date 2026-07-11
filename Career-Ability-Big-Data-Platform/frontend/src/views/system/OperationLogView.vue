<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search, View } from '@element-plus/icons-vue'
import DataTableFrame from '../../components/common/DataTableFrame.vue'
import FilterBar from '../../components/common/FilterBar.vue'
import PageContainer from '../../components/common/PageContainer.vue'
import { fetchOperationLog, fetchOperationLogs } from '../../api/system'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const detail = ref(null)
const detailVisible = ref(false)
const detailLoading = ref(false)
const query = reactive({
  username: '',
  module: '',
  page: 0,
  size: 20,
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const result = await fetchOperationLogs(query)
    logs.value = result.content
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
  Object.assign(query, { username: '', module: '', page: 0 })
  load()
}

async function showDetail(row) {
  detail.value = null
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await fetchOperationLog(row.id)
  } finally {
    detailLoading.value = false
  }
}

function statusType(status) {
  return status === 1 ? 'success' : 'danger'
}
</script>

<template>
  <PageContainer
    title="操作日志"
    description="查看平台管理操作的执行结果、来源与耗时。"
  >
    <FilterBar label="操作日志筛选">
      <el-input
        v-model.trim="query.username"
        placeholder="操作人"
        clearable
        style="width: 210px"
        @keyup.enter="search"
      />
      <el-input
        v-model.trim="query.module"
        placeholder="模块"
        clearable
        style="width: 210px"
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
        <span class="result-count">共 {{ total }} 条记录</span>
      </template>
    </FilterBar>

    <DataTableFrame
      :data="logs"
      :loading="loading"
      :page="query.page + 1"
      :page-size="query.size"
      :total="total"
      empty-title="暂无操作日志"
      empty-description="当前筛选条件下没有匹配的操作记录"
      @page-change="(value) => { query.page = value - 1; load() }"
      @page-size-change="(value) => { query.size = value; query.page = 0; load() }"
    >
      <el-table-column prop="username" label="操作人" min-width="110" />
      <el-table-column label="模块" min-width="110">
        <template #default="{ row }">
          <el-tag size="small" type="info" effect="plain">
            {{ row.module }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operation" label="操作" min-width="120" />
      <el-table-column
        prop="description"
        label="说明"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column prop="ip" label="IP" min-width="120" />
      <el-table-column label="耗时" width="100">
        <template #default="{ row }">
          <span class="duration-value">{{ row.duration }} ms</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="plain" size="small">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" min-width="170" />
      <el-table-column label="详情" width="70" fixed="right">
        <template #default="{ row }">
          <el-button
            text
            :icon="View"
            title="查看详情"
            @click="showDetail(row)"
          />
        </template>
      </el-table-column>
    </DataTableFrame>

    <el-drawer v-model="detailVisible" title="日志详情" size="min(520px, 92vw)">
      <div v-loading="detailLoading" class="detail-body">
        <div
          v-if="detail"
          class="detail-status"
          :class="{ failed: detail.status !== 1 }"
        >
          <div>
            <span>执行结果</span>
            <strong>{{ detail.status === 1 ? '操作成功' : '操作失败' }}</strong>
          </div>
          <el-tag :type="statusType(detail.status)" effect="plain">
            {{ detail.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </div>
        <el-descriptions v-if="detail" :column="1" border>
          <el-descriptions-item label="操作人">
            {{ detail.username }}
          </el-descriptions-item>
          <el-descriptions-item label="模块">
            {{ detail.module }}
          </el-descriptions-item>
          <el-descriptions-item label="操作">
            {{ detail.operation }}
          </el-descriptions-item>
          <el-descriptions-item label="方法">
            {{ detail.method }}
          </el-descriptions-item>
          <el-descriptions-item label="参数摘要">
            <span class="long-value">{{ detail.params || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="IP">
            {{ detail.ip }}
          </el-descriptions-item>
          <el-descriptions-item label="耗时">
            {{ detail.duration }} ms
          </el-descriptions-item>
          <el-descriptions-item label="结果">
            <el-tag :type="statusType(detail.status)" effect="plain" size="small">
              {{ detail.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="错误">
            <span class="long-value">{{ detail.errorMessage || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="时间">
            {{ detail.createTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </PageContainer>
</template>

<style scoped>
.result-count {
  color: var(--app-muted);
  font-size: var(--font-size-12);
}

.duration-value {
  color: var(--app-text-regular);
  font-variant-numeric: tabular-nums;
}

.detail-body {
  min-height: 220px;
}

.detail-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
  padding: var(--space-4);
  border: 1px solid var(--color-success-100);
  border-radius: var(--radius-6);
  background: var(--color-success-50);
}

.detail-status.failed {
  border-color: var(--color-danger-100);
  background: var(--color-danger-50);
}

.detail-status > div {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.detail-status span {
  color: var(--app-muted);
  font-size: var(--font-size-12);
}

.detail-status strong {
  color: var(--app-text);
  font-size: var(--font-size-14);
}

.long-value {
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}
</style>
