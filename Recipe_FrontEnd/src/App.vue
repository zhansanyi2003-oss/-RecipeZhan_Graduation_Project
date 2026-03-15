<script setup>
// App.vue 保持没有任何逻辑
</script>

<template>
  <router-view />
</template>

<style>
/* --- 全局重置 --- */
* {
  box-sizing: border-box; /* ✅ 关键：所有的 padding 都算在宽度里，不会撑开 */
}

html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow-x: hidden; /* ✅ 关键：强制禁止左右横向滚动 */
  line-height: 1.5;
  -webkit-text-size-adjust: 100%;
  font-family:
    'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial,
    sans-serif;
  background-color: #f0f2f5;
}

#app {
  height: 100%;
  min-height: 100%;
}

a {
  text-decoration: none;
}
</style>
