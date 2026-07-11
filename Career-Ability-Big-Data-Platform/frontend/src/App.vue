<template>
  <RouterView v-slot="{ Component }">
    <Transition
      name="app-route"
      mode="out-in"
    >
      <component :is="Component" />
    </Transition>
  </RouterView>
</template>
