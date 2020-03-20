const router = new VueRouter({
  mode: 'history',
  routes: [
    {
      path: '/',
      name: 'Main',
      component: function (resolve, reject) {
        loadComponent('Main', '/src/pages/Main').then(resolve, reject);
      }
    },
    {
      path: '/post',
      name: 'Post',
      component: function (resolve, reject) {
        loadComponent('Post', '/src/pages/Post').then(resolve, reject);
      }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: function (resolve, reject) {
        loadComponent('Profile', '/src/pages/Profile').then(resolve, reject);
      }
    }
  ]
});


// router.beforeEach((to, from, next) => {
// })

// router.afterEach((to, from) => {
//     store.state.routePageLoading = false
// })