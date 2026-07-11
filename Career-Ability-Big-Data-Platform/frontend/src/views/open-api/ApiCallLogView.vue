<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { CircleCheck, DataAnalysis, Refresh, Search, Timer, Warning } from '@element-plus/icons-vue'
import DataTableFrame from '../../components/common/DataTableFrame.vue'
import FilterBar from '../../components/common/FilterBar.vue'
import PageContainer from '../../components/common/PageContainer.vue'
import StatTile from '../../components/common/StatTile.vue'
import { fetchApiCallLogs, fetchApiCallStatistics } from '../../api/openApi'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const statistics = ref({
  totalCalls: 0,
  successfulCalls: 0,
  failedCalls: 0,
  averageDuration: 0,
})
const query = reactive({ path: '', page: 0, size: 20 })
const successRate = computed(() => {
  if (!statistics.value.totalCalls) return '0.0'
  return ((statistics.value.successfulCalls / statistics.value.totalCalls) * 100).toFixed(1)
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const [logPage, summary] = await Promise.all([
      fetchApiCallLogs(query),
      fetchApiCallStatistics(),
    ])
    logs.value = logPage.content
    total.value = logPage.totalElements
    statistics.value = summary
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 0
  load()
}

function reset() {
  Object.assign(query, { path: '', page: 0 })
  load()
}

function methodType(method) {
  return {
    GET: 'info',
    POST: 'success',
    PUT: 'warning',
    PATCH: 'warning',
    DELETE: 'danger',
  }[method] || 'info'
}

function statusType(statusCode) {
  if (statusCode >= 500) return 'danger'
  if (statusCode >= 400) return 'warning'
  if (statusCode >= 300) return 'info'
  return 'success'
}
</script>

<template>
  <PageContainer
    title="API 调用统计"
    description="汇总开放接口的调用量、成功率、响应耗时和请求明细。"
  >
    <div class="statistics-grid">
      <StatTile
        label="累计调用"
        :value="statistics.totalCalls"
        :icon="DataAnalysis"
        tone="info"
      />
      <StatTile
        label="调用成功率"
        :value="successRate"
        unit="%"
        :description="`${statistics.successfulCalls} 次成功调用`"
        :icon="CircleCheck"
        tone="success"
      />
      <StatTile
        label="失败调用"
        :value="statistics.failedCalls"
        :icon="Warning"
        tone="danger"
      />
      <StatTile
        label="平均耗时"
        :value="Number(statistics.averageDuration).toFixed(1)"
        unit="ms"
        :icon="Timer"
        tone="brand"
      />
    </div>

    <FilterBar label="API 调用记录筛选">
      <el-input
        v-model.trim="query.path"
        placeholder="筛选接口路径"
        clearable
        style="width: 300px"
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
      empty-title="暂无调用记录"
      empty-description="当前筛选条件下没有匹配的接口调用"
      @page-change="(value) => { query.page = value - 1; load() }"
      @page-size-change="(value) => { query.size = value; query.page = 0; load() }"
    >
      <el-table-column prop="apiKeyId" label="Key ID" width="90" />
      <el-table-column label="方法" width="90">
        <template #default="{ row }">
          <el-tag :type="methodType(row.method)" effect="plain" size="small">
            {{ row.method }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="接口路径"
        min-width="240"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          <code class="api-path">{{ row.apiPath }}</code>
        </template>
      </el-table-column>
      <el-table-column
        prop="params"
        label="参数名"
        min-width="130"
        show-overflow-tooltip
      />
      <el-table-column prop="ip" label="IP" min-width="120" />
      <el-table-column label="耗时" width="100">
        <template #default="{ row }">
          <span class="duration-value">{{ row.duration }} ms</span>
        </template>
      </el-table-column>
      <el-table-column label="状态码" width="90">
        <template #default="{ row }">
          <el-tag
            :type="statusType(row.statusCode)"
            effect="plain"
            size="small"
          >
            {{ row.statusCode }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="调用时间" min-width="170" />
    </DataTableFrame>
  </PageContainer>
</template>

<style scoped>
.result-count {
  color: var(--app-muted);
  font-size: var(--font-size-12);
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.api-path {
  color: var(--app-text-regular);
  font-family: var(--font-family-mono);
  font-size: var(--font-size-12);
}

.duration-value {
  font-variant-numeric: tabular-nums;
}

@media (max-width: 900px) {
  .statistics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 480px) {
  .statistics-grid {
    grid-template-columns: 1fr;
  }
}
</style>
