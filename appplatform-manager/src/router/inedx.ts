import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import Layout from "@/views/layout/Index.vue";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
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
        meta: { title: "应用列表", icon: "App" },
      },
      {
        path: "/app/upload",
        name: "AppUpload",
        component: () => import("@/views/app/Upload.vue"),
        meta: { title: "上传应用", icon: "Upload" },
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
