<script setup>
defineProps({
  label: {
    type: String,
    default: '筛选条件',
  },
  compact: {
    type: Boolean,
    default: false,
  },
})
</script>

<template>
  <section
    class="filter-bar"
    :class="{ 'filter-bar--compact': compact }"
    role="search"
    :aria-label="label"
  >
    <div class="filter-bar__fields">
      <slot />
    </div>
    <div
      v-if="$slots.actions"
      class="filter-bar__actions"
    >
      <slot name="actions" />
    </div>
    <div
      v-if="$slots.trailing"
      class="filter-bar__trailing"
    >
      <slot name="trailing" />
    </div>
  </section>
</template>

<style scoped>
.filter-bar {
  display: flex;
  min-width: 0;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.filter-bar--compact {
  margin-bottom: var(--space-3);
}

.filter-bar__fields,
.filter-bar__actions,
.filter-bar__trailing {
  display: flex;
  min-width: 0;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}

.filter-bar__actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.filter-bar__trailing {
  margin-left: auto;
}

@media (max-width: 720px) {
  .filter-bar,
  .filter-bar__fields {
    align-items: stretch;
  }

  .filter-bar__fields {
    width: 100%;
  }

  .filter-bar__fields :deep(.el-input),
  .filter-bar__fields :deep(.el-select),
  .filter-bar__fields :deep(.el-date-editor),
  .filter-bar__fields :deep(.el-input-number) {
    width: 100% !important;
  }

  .filter-bar__actions,
  .filter-bar__trailing {
    margin-left: 0;
  }
}
</style>
