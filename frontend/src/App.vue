<template>
  <div id="app">
    <NavBar />
    <router-view />
  </div>
</template>

<script setup lang="ts">
import NavBar from './components/NavBar.vue'
</script>

<style scoped>
#app {
  min-height: 100vh;
  background: #181a1b;
  color: #e0e0e0;
  display: flex;
  flex-direction: column;
}
</style>
