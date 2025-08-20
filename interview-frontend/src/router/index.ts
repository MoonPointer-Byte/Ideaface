import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router'; 
import { useUserStore } from '@/store/user';
import { ElMessage } from 'element-plus';

const routes: Array<RouteRecordRaw> = [
  { path: '/', name: 'home', component: () => import('@/views/HomePage.vue'), meta: { title: '首页' } },
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue'), meta: { title: '注册' } },
  { path: '/questions', name: 'questions', component: () => import('@/views/QuestionBankView.vue'), meta: { title: '题库中心', requiresAuth: true } },
  { path: '/questions/:id', name: 'questionDetail', component: () => import('@/views/QuestionDetailView.vue'), meta: { title: '题目详情', requiresAuth: true }, props: true },
  { path: '/algorithm', name: 'algorithm', component: () => import('@/views/AlgorithmView.vue'), meta: { title: '算法测试', requiresAuth: true } },
  { path: '/algorithm/:id', name: 'algorithmSolve', component: () => import('@/views/AlgorithmSolveView.vue'), meta: { title: '答题界面', requiresAuth: true }, props: true },
  { path: '/algorithm/:id/pass', name: 'algorithmPass', component: () => import('@/views/AlgorithmPassView.vue'), meta: { title: '通过界面', requiresAuth: true }, props: true },
  { path: '/algorithm/:id/fail', name: 'algorithmFail', component: () => import('@/views/AlgorithmFailView.vue'), meta: { title: '未通过界面', requiresAuth: true }, props: true },
  { path: '/algorithm/summary', name: 'algorithmSummary', component: () => import('@/views/AlgorithmSummaryView.vue'), meta: { title: '练习总结', requiresAuth: true } },
  { path: '/learning', name: 'learning', component: () => import('@/views/LearningCenterView.vue'), meta: { title: '学习中心', requiresAuth: true } },
  { path: '/interview', name: 'interviewSetup', component: () => import('@/views/PositionSelectView.vue'), meta: { title: '开始面试', requiresAuth: true } },
  { path: '/interview/:positionId', name: 'interviewSession', component: () => import('@/views/InterviewView.vue'), meta: { title: '进行面试', requiresAuth: true }, props: true },
  { path: '/report/:sessionId', name: 'report', component: () => import('@/views/ReportView.vue'), meta: { title: '面试报告', requiresAuth: true }, props: true },
  { path: '/profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { title: '个人中心', requiresAuth: true } },
  { path: '/history', name: 'history', component: () => import('@/views/HistoryView.vue'), meta: { title: '面试历史', requiresAuth: true } },
  { path: '/blog',    name: 'blog',    component: () => import('@/views/BlogListView.vue'), meta: { title: '博客论坛' ,requiresAuth: true}},
  { 
    path: '/blog/post/:id', 
    name: 'postDetail', 
    component: () => import('@/views/PostDetailView.vue'), 
    meta: { title: '文章详情' },
    props: true 
  },
  { 
    path: '/blog/create', 
    name: 'createPost', 
    component: () => import('@/views/CreatePostView.vue'), 
    meta: { title: '发表新文章', requiresAuth: true } 
  },
  { 
    path: '/blog/edit/:id', 
    name: 'editPost', 
    component: () => import('@/views/EditPostView.vue'), 
    meta: { title: '编辑文章', requiresAuth: true },
    props: true
  }, 
  {
    path: '/resume-assessment', 
    name: 'ResumeAssessment',   
    component: () => import('@/views/ResumeAssessment.vue'),  
    meta: {
        title: '简历智能评估' 
        ,requiresAuth: true 
    }
  },
  {
    path: '/career-planner',
    name: 'CareerPathPlanner',
    component: () => import('@/views/CareerPathPlanner.vue'),
    meta: {
        title: '职业发展规划',
        requiresAuth: true 
  }
},
  {
    path: '/resume-optimizer',
    name: 'ResumeOptimizer',
    component: () => import('@/views/ResumeOptimizer.vue'),
    meta: {
        title: '简历智能优化',
        requiresAuth: true 
    }
  },
  {
    path: '/AgentHub',
    name: 'AgentHub', 
    component: () => import('@/views/AgentHub.vue'), 
    meta: {
        title: '智能体中心'
        ,requiresAuth: true 
    }
  },
  { 
    path: '/blog/search', 
    name: 'searchResult', 
    component: () => import('@/views/blog/SearchResultView.vue'), 
    meta: { title: '搜索结果' } 
  },
   { 
    path: '/user/:userId', 
    name: 'userProfile', 
    component: () => import('@/views/UserProfileView.vue'), 
    meta: { title: '用户主页' },
    props: true
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

router.beforeEach((to, _from, next) => {
  document.title = `AI面试官 - ${to.meta.title || '欢迎'}`;
  const userStore = useUserStore();
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  if (requiresAuth && !userStore.isLoggedIn) {
    ElMessage.warning('此页面需要登录后才能访问');
    next({ name: 'login', query: { redirect: to.fullPath } });
  } else {
    next();
  }
});

export default router;