<script setup>
import { useSlots } from 'vue'
import PageHeader from './PageHeader.vue'

defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
  framed: {
    type: Boolean,
    default: true,
  },
  flush: {
    type: Boolean,
    default: false,
  },
})

const slots = useSlots()
</script>

<template>
  <section class="page-container">
    <PageHeader
      :title="title"
      :description="description"
    >
      <template
        v-if="slots.actions"
        #actions
      >
        <slot name="actions" />
      </template>
    </PageHeader>
    <div
      class="page-content"
      :class="{
        'page-content--unframed': !framed,
        'page-content--flush': flush,
      }"
    >
      <slot />
    </div>
  </section>
</template>

<style scoped>
.page-container {
  width: 100%;
  min-width: 0;
}

.page-content {
  min-width: 0;
  padding: var(--space-5);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-6);
  background: var(--app-surface);
  box-shadow: var(--shadow-xs);
}

.page-content--unframed {
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.page-content--flush {
  overflow: hidden;
  padding: 0;
}

@media (max-width: 720px) {
  .page-content:not(.page-content--unframed, .page-content--flush) {
    padding: var(--space-4);
  }
}
</style>
