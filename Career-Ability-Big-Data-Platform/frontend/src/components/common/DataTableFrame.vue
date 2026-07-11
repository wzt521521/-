<script setup>
import EmptyState from './EmptyState.vue'

defineOptions({
  inheritAttrs: false,
})

defineProps({
  data: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  rowKey: {
    type: [String, Function],
    default: 'id',
  },
  stripe: {
    type: Boolean,
    default: true,
  },
  pagination: {
    type: Boolean,
    default: true,
  },
  page: {
    type: Number,
    default: 1,
    validator: (value) => value >= 1,
  },
  pageSize: {
    type: Number,
    default: 20,
    validator: (value) => value > 0,
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100],
  },
  total: {
    type: Number,
    default: 0,
    validator: (value) => value >= 0,
  },
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next',
  },
  emptyTitle: {
    type: String,
    default: '暂无数据',
  },
  emptyDescription: {
    type: String,
    default: '',
  },
})

const emit = defineEmits([
  'update:page',
  'update:pageSize',
  'page-change',
  'page-size-change',
])

function handlePageChange(value) {
  emit('update:page', value)
  emit('page-change', value)
}

function handlePageSizeChange(value) {
  emit('update:pageSize', value)
  emit('page-size-change', value)
}
</script>

<template>
  <section
    class="data-table-frame"
    :aria-busy="loading"
  >
    <div
      v-if="$slots.toolbar"
      class="data-table-frame__toolbar"
    >
      <slot name="toolbar" />
    </div>

    <div class="data-table-frame__table">
      <el-table
        v-loading="loading"
        v-bind="$attrs"
        :data="data"
        :row-key="rowKey"
        :stripe="stripe"
      >
        <slot />
        <template #empty>
          <slot name="empty">
            <EmptyState
              :title="emptyTitle"
              :description="emptyDescription"
              compact
            >
              <template
                v-if="$slots['empty-actions']"
                #actions
              >
                <slot name="empty-actions" />
              </template>
            </EmptyState>
          </slot>
        </template>
      </el-table>
    </div>

    <footer
      v-if="pagination"
      class="data-table-frame__footer"
    >
      <div
        v-if="$slots['footer-start']"
        class="data-table-frame__footer-start"
      >
        <slot name="footer-start" />
      </div>
      <el-pagination
        :page-size="pageSize"
        :current-page="page"
        :total="total"
        :page-sizes="pageSizes"
        :layout="paginationLayout"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
      />
    </footer>
  </section>
</template>

<style scoped>
.data-table-frame,
.data-table-frame__table {
  min-width: 0;
}

.data-table-frame__toolbar {
  margin-bottom: var(--space-3);
}

.data-table-frame__footer {
  display: flex;
  min-height: var(--control-height-md);
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-4);
  margin-top: var(--space-4);
}

.data-table-frame__footer-start {
  min-width: 0;
  margin-right: auto;
  color: var(--app-muted);
  font-size: var(--font-size-13);
}

@media (max-width: 720px) {
  .data-table-frame__footer {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: var(--space-1);
  }

  .data-table-frame__footer-start {
    display: none;
  }
}
</style>
