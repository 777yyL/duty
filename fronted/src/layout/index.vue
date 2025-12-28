<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">
        <h3>值班看板系统</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="el-menu-vertical"
      >
        <el-menu-item index="/schedule">
          <el-icon><Calendar /></el-icon>
          <span>值班表管理</span>
        </el-menu-item>
        <el-menu-item index="/record">
          <el-icon><Document /></el-icon>
          <span>值班记录查询</span>
        </el-menu-item>
        <el-menu-item index="/shift">
          <el-icon><Clock /></el-icon>
          <span>班次配置</span>
        </el-menu-item>
        <el-menu-item index="/template">
          <el-icon><Setting /></el-icon>
          <span>记录模板配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-title">{{ currentTitle }}</div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const activeMenu = computed(() => route.path)

const currentTitle = computed(() => {
  return route.meta.title as string || '留置管理中心电子值班看板系统'
})
</script>

<style scoped>
.layout-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4a;
  border-bottom: 1px solid #1f2d3d;
}

.logo h3 {
  color: #fff;
  font-size: 18px;
  margin: 0;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
