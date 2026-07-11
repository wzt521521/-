<script setup>
import { ref } from 'vue'
import { Refresh, TopRight } from '@element-plus/icons-vue'
import PageContainer from '../../components/common/PageContainer.vue'

const frameKey = ref(0)
const loading = ref(true)

function refreshDocs() {
  loading.value = true
  frameKey.value += 1
}

function openDocs() {
  window.open('/swagger-ui/index.html', '_blank', 'noopener,noreferrer')
}
</script>

<template>
  <PageContainer
    title="API 文档"
    description="查看 OpenAPI 接口定义、请求模型和响应结构。"
    flush
  >
    <template #actions>
      <el-button :icon="TopRight" @click="openDocs">
        新窗口打开
      </el-button>
      <el-button :icon="Refresh" @click="refreshDocs">
        刷新
      </el-button>
    </template>
    <div v-loading="loading" class="docs-stage">
      <iframe
        :key="frameKey"
        class="docs-frame"
        src="/swagger-ui/index.html"
        title="OpenAPI 文档"
        @load="loading = false"
      />
    </div>
  </PageContainer>
</template>

<style scoped>
.docs-stage,
.docs-frame {
  width: 100%;
  height: calc(100vh - 202px);
  min-height: 560px;
}

.docs-stage {
  background: var(--app-surface);
}

.docs-frame {
  display: block;
  border: 0;
  background: white;
}

@media (max-width: 720px) {
  .docs-stage,
  .docs-frame {
    height: calc(100vh - 248px);
    min-height: 480px;
  }
}
</style>
