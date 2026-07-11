<script setup>
import EmptyState from './EmptyState.vue'

defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  empty: {
    type: Boolean,
    default: false,
  },
  emptyTitle: {
    type: String,
    default: '暂无图表数据',
  },
  emptyDescription: {
    type: String,
    default: '',
  },
  minHeight: {
    type: Number,
    default: 320,
    validator: (value) => value >= 160,
  },
  bordered: {
    type: Boolean,
    default: true,
  },
})
</script>

<template>
  <section
    class="chart-panel"
    :class="{ 'chart-panel--borderless': !bordered }"
    :aria-busy="loading"
  >
    <header class="chart-panel__header">
      <div class="chart-panel__copy">
        <h2>{{ title }}</h2>
        <p v-if="description">
          {{ description }}
        </p>
      </div>
      <div
        v-if="$slots.actions"
        class="chart-panel__actions"
      >
        <slot name="actions" />
      </div>
    </header>

    <div
      class="chart-panel__body"
      :style="{ '--chart-panel-min-height': `${minHeight}px` }"
    >
      <div
        v-if="loading"
        class="chart-panel__loading"
      >
        <el-skeleton
          :rows="5"
          animated
        />
      </div>
      <EmptyState
        v-else-if="empty"
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
      <div
        v-else
        class="chart-panel__content"
      >
        <slot />
      </div>
    </div>

    <footer
      v-if="$slots.footer"
      class="chart-panel__footer"
    >
      <slot name="footer" />
    </footer>
  </section>
</template>

<style scoped>
.chart-panel {
  --chart-panel-min-height: 320px;
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-6);
  background: var(--app-surface);
  box-shadow: var(--shadow-xs);
}

.chart-panel--borderless {
  border: 0;
  border-radius: 0;
  box-shadow: none;
}

.chart-panel__header {
  display: flex;
  min-height: 68px;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--app-border);
}

.chart-panel__copy {
  min-width: 0;
}

.chart-panel h2 {
  margin: 0;
  color: var(--app-text);
  font-size: var(--font-size-16);
  font-weight: var(--font-weight-semibold);
  line-height: var(--line-height-tight);
  letter-spacing: 0;
}

.chart-panel__copy p {
  margin: var(--space-1) 0 0;
  color: var(--app-muted);
  font-size: var(--font-size-12);
  line-height: var(--line-height-base);
}

.chart-panel__actions {
  display: flex;
  flex: 0 0 auto;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.chart-panel__actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.chart-panel__body,
.chart-panel__loading,
.chart-panel__content {
  min-height: var(--chart-panel-min-height);
}

.chart-panel__body {
  position: relative;
  min-width: 0;
}

.chart-panel__loading {
  display: grid;
  align-items: center;
  padding: var(--space-6);
}

.chart-panel__content {
  min-width: 0;
  padding: var(--space-4);
}

.chart-panel__footer {
  padding: var(--space-3) var(--space-5);
  border-top: 1px solid var(--app-border);
  color: var(--app-muted);
  font-size: var(--font-size-12);
}

@media (max-width: 720px) {
  .chart-panel__header {
    flex-direction: column;
  }

  .chart-panel__actions {
    width: 100%;
  }
}
</style>
