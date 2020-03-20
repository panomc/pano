'use strict';

Vue.component('Navbar', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      methods: {
        onSideBarCollapseClick() {
          this.$store.dispatch("toggleSidebar")
        },

        md5(string) {
          return md5(string)
        },

        setLang(lang) {
          this.$store.dispatch('setLang', lang)
        },

        logout() {
          this.$store.state.logoutLoading = true;

          ApiUtil.post("/api/auth/logout", {})
            .then(() => {
              window.location.href = '/';
            })
            .catch(() => {
              window.location.href = '/';
            });
        },

        getQuickNotifications() {
          ApiUtil.get("/api/panel/quickNotifications")
            .then(response => {
              if (response.data.result === "ok") {
                this.quickNotifications = response.data.notifications
              }

              this.startQuickNotificationsCountDown()
            })
            .catch(() => {
              this.startQuickNotificationsCountDown()
            });
        },

        startQuickNotificationsCountDown() {
          let timeToRefreshKey = 1;

          const timer = setInterval(() => {
            if (timeToRefreshKey > 0) {
              timeToRefreshKey--
            } else {
              clearInterval(timer);

              this.getQuickNotifications();
            }
          }, 1000)
        }
      },
      computed: {
        langLoading() {
          return this.$store.state.langLoading
        },

        username() {
          return this.$store.state.user.username
        },

        email() {
          return this.$store.state.user.email
        },

        quickNotifications: {
          get() {
            return this.$store.state.quickNotifications
          },
          set(value) {
            this.$store.state.quickNotifications = value
          }
        }
      },
      mounted() {
        this.startQuickNotificationsCountDown();
      }
    });
  })
);