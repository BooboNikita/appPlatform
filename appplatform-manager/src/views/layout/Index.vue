<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container">
      <div class="logo">应用管理平台</div>
      <el-menu
        :default-active="$route.path"
        class="el-menu-vertical"
        :collapse="isCollapse"
        router
      >
        <template v-for="route in routes" :key="route.path">
          <el-menu-item v-if="!route.children" :index="route.path">
            <el-icon><component :is="route.meta?.icon" /></el-icon>
            <span>{{ route.meta?.title }}</span>
          </el-menu-item>
          <el-sub-menu v-else :index="route.path">
            <template #title>
              <el-icon><component :is="route.meta?.icon" /></el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="child.path"
            >
              {{ child.meta?.title }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <div class="navbar">
        <div class="left-menu">
          <el-icon @click="toggleSideBar">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="item in matched"
              :key="item.path"
              :to="item.path"
            >
              {{ item.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right-menu">
          <el-dropdown>
            <span class="el-dropdown-link">
              <!-- <el-avatar :size="30" :src="user.avatar" /> -->
              <!-- <span class="username">{{ user.name }}</span> -->
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleLogout"
                  >退出登录</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { ElMessageBox } from "element-plus";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const isCollapse = ref(false);
const user = computed(() => userStore.userInfo);

const routes = computed(() => {
  const root =
    router.options.routes.find((item) => item.path === "/")?.children || [];
  return root.filter((item) => !item.meta?.hiddenInMenu);
});

const matched = computed(() => {
  return route.matched.filter((item) => item.meta.title);
});

const toggleSideBar = () => {
  isCollapse.value = !isCollapse.value;
};

const handleLogout = () => {
  ElMessageBox.confirm("确定要退出登录吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    userStore.logout();
    router.push("/login");
  });
};
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.sidebar-container {
  width: 210px;
  height: 100%;
  background-color: #304156;
  transition: width 0.28s;
  flex-shrink: 0;
  overflow: hidden;

  .logo {
    height: 50px;
    line-height: 50px;
    text-align: center;
    color: #fff;
    font-size: 18px;
    font-weight: 600;
    background-color: #2b2f3a;
  }

  .el-menu-vertical {
    border-right: none;
    height: calc(100% - 50px);
    background-color: #304156;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      color: #bfcbd9;

      &:hover {
        background-color: #263445;
      }

      &.is-active {
        color: #409eff;
        background-color: #263445;
      }
    }
  }

  &.collapse {
    width: 64px;

    .logo {
      padding: 0;
      text-align: center;
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .navbar {
    height: 50px;
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;

    .left-menu {
      display: flex;
      align-items: center;

      .el-icon {
        margin-right: 15px;
        font-size: 20px;
        cursor: pointer;
        color: #5a5e66;
      }
    }

    .right-menu {
      .el-dropdown-link {
        display: flex;
        align-items: center;
        cursor: pointer;

        .username {
          margin-left: 10px;
          color: #606266;
        }
      }
    }
  }

  .app-main {
    flex: 1;
    padding: 20px;
    overflow: auto;
    background-color: #f0f2f5;
  }
}

// 动画效果
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
