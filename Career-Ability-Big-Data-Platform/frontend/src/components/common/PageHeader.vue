<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
  bordered: {
    type: Boolean,
    default: true,
  },
})
</script>

<template>
  <header
    class="page-header"
    :class="{ 'page-header--borderless': !bordered }"
  >
    <div class="page-header__copy">
      <h1>
        <slot name="title">
          {{ title }}
        </slot>
      </h1>
      <p v-if="description || $slots.description">
        <slot name="description">
          {{ description }}
        </slot>
      </p>
      <div
        v-if="$slots.meta"
        class="page-header__meta"
      >
        <slot name="meta" />
      </div>
    </div>

    <div
      v-if="$slots.actions"
      class="page-header__actions"
    >
      <slot name="actions" />
    </div>
  </header>
</template>

<style scoped>
.page-header {
  display: flex;
  min-height: 58px;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-5);
  margin-bottom: var(--space-5);
  border-bottom: 1px solid var(--app-border);
}

.page-header--borderless {
  min-height: auto;
  border-bottom: 0;
}

.page-header__copy {
  min-width: 0;
}

.page-header h1 {
  margin: 0;
  color: var(--app-text);
  font-size: var(--font-size-22);
  font-weight: var(--font-weight-semibold);
  line-height: var(--line-height-tight);
  letter-spacing: 0;
  overflow-wrap: anywhere;
}

.page-header p {
  max-width: 760px;
  margin: var(--space-2) 0 var(--space-4);
  color: var(--app-muted);
  font-size: var(--font-size-14);
  line-height: var(--line-height-base);
}

.page-header__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  margin: var(--space-2) 0 var(--space-4);
  color: var(--app-muted);
  font-size: var(--font-size-12);
}

.page-header__actions {
  display: flex;
  flex: 0 0 auto;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--space-2);
}

.page-header__actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

@media (max-width: 720px) {
  .page-header {
    flex-direction: column;
    gap: var(--space-2);
  }

  .page-header__actions {
    width: 100%;
    justify-content: flex-start;
    padding-bottom: var(--space-3);
  }
}
</style>
