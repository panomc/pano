'use strict';

Vue.component('SiteView', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {
          currentActiveNavLink: 1
        }
      },
      methods: {
        setCurrentActiveNav(path) {
          if (path.includes("/panel/view/theme-options"))
            this.currentActiveNavLink = 2
          else
            this.currentActiveNavLink = 1
        }
      },
      beforeMount() {
        this.setCurrentActiveNav(this.$route.path);

        this.$router.afterEach((to, from) => {
          this.setCurrentActiveNav(to.path)
        })
      }
    });
  })
);