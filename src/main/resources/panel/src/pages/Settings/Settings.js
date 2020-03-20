'use strict';

Vue.component('Settings', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {
          currentActiveNavLink: 1
        }
      },
      methods: {
        setCurrentActiveNav(path) {
          if (path.includes("/panel/settings/updates"))
            this.currentActiveNavLink = 3
          else if (path.includes("/panel/settings/site-settings"))
            this.currentActiveNavLink = 2
          else if (path.includes("/panel/settings/about"))
            this.currentActiveNavLink = 4
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