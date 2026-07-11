<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number],
    default: '--',
  },
  unit: {
    type: String,
    default: '',
  },
  description: {
    type: String,
    default: '',
  },
  icon: {
    type: [Object, Function],
    default: null,
  },
  tone: {
    type: String,
    default: 'neutral',
    validator: (value) => ['neutral', 'brand', 'info', 'success', 'warning', 'danger'].includes(value),
  },
  loading: {
    type: Boolean,
    default: false,
  },
})
</script>

<template>
  <article
    class="stat-tile"
    :class="`stat-tile--${tone}`"
    :aria-label="`${label}: ${value}${unit}`"
    :aria-busy="loading"
  >
    <el-skeleton
      v-if="loading"
      :rows="1"
      animated
    />
    <template v-else>
      <div
        v-if="icon || $slots.icon"
        class="stat-tile__icon"
      >
        <slot name="icon">
          <el-icon><component :is="icon" /></el-icon>
        </slot>
      </div>
      <div class="stat-tile__body">
        <span class="stat-tile__label">{{ label }}</span>
        <div class="stat-tile__value">
          <strong>
            <slot name="value">
              {{ value }}
            </slot>
          </strong>
          <small v-if="unit">{{ unit }}</small>
        </div>
        <p v-if="description">
          {{ description }}
        </p>
        <div
          v-if="$slots.footer"
          class="stat-tile__footer"
        >
          <slot name="footer" />
        </div>
      </div>
    </template>
  </article>
</template>

<style scoped>
.stat-tile {
  --stat-tile-accent: var(--color-neutral-600);
  --stat-tile-soft: var(--app-surface-muted);
  display: flex;
  min-width: 0;
  min-height: 108px;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-6);
  background: var(--app-surface-subtle);
}

.stat-tile--brand {
  --stat-tile-accent: var(--app-accent);
  --stat-tile-soft: var(--color-brand-100);
}

.stat-tile--info {
  --stat-tile-accent: var(--app-info);
  --stat-tile-soft: var(--color-info-100);
}

.stat-tile--success {
  --stat-tile-accent: var(--app-success);
  --stat-tile-soft: var(--color-success-100);
}

.stat-tile--warning {
  --stat-tile-accent: var(--app-warning);
  --stat-tile-soft: var(--color-warning-100);
}

.stat-tile--danger {
  --stat-tile-accent: var(--app-danger);
  --stat-tile-soft: var(--color-danger-100);
}

.stat-tile__icon {
  display: grid;
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  place-items: center;
  border-radius: var(--radius-6);
  background: var(--stat-tile-soft);
  color: var(--stat-tile-accent);
  font-size: var(--font-size-18);
}

.stat-tile__body {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: var(--space-1);
}

.stat-tile__label,
.stat-tile p,
.stat-tile__footer {
  color: var(--app-muted);
  font-size: var(--font-size-13);
}

.stat-tile__value {
  display: flex;
  min-width: 0;
  align-items: baseline;
  gap: var(--space-1);
  color: var(--app-text);
}

.stat-tile__value strong {
  overflow-wrap: anywhere;
  font-size: 25px;
  font-weight: var(--font-weight-semibold);
  line-height: var(--line-height-tight);
  letter-spacing: 0;
}

.stat-tile__value small {
  font-size: var(--font-size-12);
  font-weight: var(--font-weight-medium);
}

.stat-tile p {
  margin: var(--space-1) 0 0;
  line-height: var(--line-height-base);
}

.stat-tile__footer {
  margin-top: var(--space-1);
}
</style>
