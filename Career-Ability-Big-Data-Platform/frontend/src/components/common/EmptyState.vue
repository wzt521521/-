<script setup>
import { Box } from '@element-plus/icons-vue'

defineProps({
  title: {
    type: String,
    default: '暂无数据',
  },
  description: {
    type: String,
    default: '',
  },
  icon: {
    type: [Object, Function],
    default: () => Box,
  },
  compact: {
    type: Boolean,
    default: false,
  },
})
</script>

<template>
  <div
    class="empty-state"
    :class="{ 'empty-state--compact': compact }"
    role="status"
    aria-live="polite"
  >
    <div class="empty-state__icon">
      <slot name="icon">
        <el-icon><component :is="icon" /></el-icon>
      </slot>
    </div>
    <strong>{{ title }}</strong>
    <p v-if="description || $slots.description">
      <slot name="description">
        {{ description }}
      </slot>
    </p>
    <div
      v-if="$slots.actions"
      class="empty-state__actions"
    >
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  display: grid;
  min-height: 220px;
  place-content: center;
  justify-items: center;
  gap: var(--space-2);
  padding: var(--space-6);
  color: var(--app-muted);
  text-align: center;
}

.empty-state--compact {
  min-height: 140px;
  padding: var(--space-4);
}

.empty-state__icon {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: var(--radius-8);
  background: var(--app-surface-muted);
  color: var(--color-neutral-500);
  font-size: 22px;
}

.empty-state strong {
  color: var(--app-text-regular);
  font-size: var(--font-size-14);
  font-weight: var(--font-weight-semibold);
}

.empty-state p {
  max-width: 420px;
  margin: 0;
  font-size: var(--font-size-13);
  line-height: var(--line-height-base);
}

.empty-state__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--space-2);
  margin-top: var(--space-2);
}

.empty-state__actions :deep(.el-button + .el-button) {
  margin-left: 0;
}
</style>
