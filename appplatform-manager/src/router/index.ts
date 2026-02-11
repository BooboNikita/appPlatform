import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import Layout from "@/views/layout/Index.vue";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/Login.vue"),
    meta: { title: "登录" },
  },
  {
    path: "/download",
    name: "Download",
    component: () => import("@/views/app/Download.vue"),
    meta: { title: "应用下载" },
  },
  {
    path: "/",
    component: Layout,
    redirect: "/app/list",
    children: [
      {
        path: "/app/list",
        name: "AppList",
        component: () => import("@/views/app/List.vue"),
        meta: { title: "应用列表", icon: "Suitcase" },
      },
      {
        path: "/app/upload",
        name: "AppUpload",
        component: () => import("@/views/app/Upload.vue"),
        meta: { title: "上传应用", icon: "Upload", hiddenInMenu: true },
      },
      {
        path: "/app/store-link",
        name: "StoreLinkConfig",
        component: () => import("@/views/store-link/List.vue"),
        meta: { title: "商店链接", icon: "Link" },
      },
      {
        path: "/app/tracking",
        name: "Tracking",
        component: () => import("@/views/app/Tracking.vue"),
        meta: { title: "埋点数据", icon: "DataAnalysis" },
      },
      {
        path: "/app/dynamic-config",
        name: "DynamicConfig",
        component: () => import("@/views/app/DynamicConfig.vue"),
        meta: { title: "动态配置", icon: "Setting" },
      },
      {
        path: "/logs",
        name: "Logs",
        component: () => import("@/views/logs/List.vue"),
        meta: { title: "日志管理", icon: "Document" },
      },
      {
        path: "/logs/detail/:id",
        name: "LogDetail",
        component: () => import("@/views/logs/Detail.vue"),
        meta: { title: "日志详情", icon: "Document", hiddenInMenu: true },
      },
      {
        path: "/modules",
        name: "Modules",
        component: () => import("@/views/modules/List.vue"),
        meta: { title: "模块管理", icon: "Menu" },
      },
      {
        path: "/modules/edit",
        name: "ModuleEdit",
        component: () => import("@/views/modules/Edit.vue"),
        meta: { title: "编辑模块", icon: "Edit", hiddenInMenu: true },
      },
      {
        path: "/performance",
        name: "PerformanceReview",
        component: () => import("@/views/performance/List.vue"),
        meta: { title: "绩效配置", icon: "Tickets" },
      },
      {
        path: "/app/loginCode",
        name: "loginCode",
        component: () => import("@/views/app/LoginCode.vue"),
        meta: { title: "二维码登录", icon: "FullScreen" },
      },
      {
        path: "#",
        name: "Build",
        component: () => import("@/views/app/LoginCode.vue"),
        meta: {
          title: "构建平台",
          icon: "Open",
          external: true,
          href: "http://172.16.110.45:14808/",
        },
      },
      {
        path: "#",
        name: "Graph",
        component: () => import("@/views/app/LoginCode.vue"),
        meta: {
          title: "报表平台",
          icon: "TrendCharts",
          external: true,
          href: "http://172.16.110.45:3000/d/ad97cvd/appe7aea1-e79086-e5b9b3-e58fb0?orgId=1&from=now-24h&to=now&timezone=browser",
        },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE}`;
  next();
});

export default router;
