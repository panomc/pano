'use strict';

Vue.component('Sidebar', new Promise(function (resolve) {
    requirejs([
      "/panel/assets/js/simplecopy.min.js"
    ], function () {
      resolve({
        template: PANO.UI,
        data() {
          return {
            timeToRefreshKey: 30,
            isPlatformAddressCopied: false,
            isPlatformMatchKeyCopied: false,
            copyClickIDForAddress: 0,
            copyClickIDForMatchKey: 0,
            removingServer: [],
            removingServerForm: {
              password: "",
              removing: false,
              error: {
                code: "",
                password: false
              }
            }
          }
        },
        methods: {
          onWebsiteClick() {
            this.$store.dispatch("setSidebarTabsState", "website")
          },

          onGameClick() {
            this.$store.dispatch("setSidebarTabsState", "game")
          },

          onMobileSideBarCollapseClick() {
            this.$store.dispatch("toggleSidebar")
          },

          refreshKey() {
            ApiUtil.get("/api/panel/platformAuth/refreshKey")
              .then(response => {
                if (response.data.result === "ok") {
                  this.$store.state.currentServerPlatformMatchKey = response.data.key
                } else {
                  this.$store.state.currentServerPlatformMatchKey = ""
                }

                this.startCountDown()
              })
              .catch(() => {
                this.$store.state.currentServerPlatformMatchKey = "";

                this.startCountDown()
              });
          },

          startCountDown() {
            this.timeToRefreshKey = 30;

            const timer = setInterval(() => {
              if (this.timeToRefreshKey > 0) {
                this.timeToRefreshKey--
              } else {
                clearInterval(timer);

                this.timeToRefreshKey = "...";

                this.refreshKey();
              }
            }, 1000)
          },

          onCopyPlatformAddressClick() {
            this.copyClickIDForAddress++;

            const id = this.copyClickIDForAddress;
            const vue = this;

            simplecopy(this.platformAddress);

            this.isPlatformAddressCopied = true;

            setTimeout(function () {
              if (vue.copyClickIDForAddress === id) {
                vue.isPlatformAddressCopied = false;
              }
            }, 1000);
          },

          onCopyPlatformMatchKeyClick() {
            this.copyClickIDForMatchKey++;

            const id = this.copyClickIDForMatchKey;
            const vue = this;

            simplecopy('/pano connect ' + this.platformAddress + ' ' + this.key);

            this.isPlatformMatchKeyCopied = true;

            setTimeout(function () {
              if (vue.copyClickIDForMatchKey === id) {
                vue.isPlatformMatchKeyCopied = false;
              }
            }, 1000);
          },

          selectServer(server) {
            if (this.$store.state.selected_server.id !== server.id)
              this.$store.state.selected_server = server;
          },

          removeServer() {
            return new Promise((resolve, reject) => {
              ApiUtil.post("/api/panel/server/remove", {
                id: this.removingServer.id,
                password: this.removingServerForm.password
              }).then(response => {
                if (response.data.result === "ok") {
                  resolve(response);
                } else if (response.data.result === "error") {
                  const error = response.data.error;

                  reject(error);
                } else
                  reject([{"code": NETWORK_ERROR}]);
              }).catch(() => {
                reject([{"code": NETWORK_ERROR}]);
              });
            });
          },

          getServerList() {
            return new Promise((resolve, reject) => {
              ApiUtil.get("/api/panel/server/list").then(response => {
                if (response.data.result === "ok") {
                  resolve(response);
                } else if (response.data.result === "error") {
                  const error = response.data.error;

                  reject(error);
                } else
                  reject(NETWORK_ERROR);
              }).catch(() => {
                reject(NETWORK_ERROR);
              });
            });
          },

          onRemoveClick(server) {
            if (this.removingServer.id !== server.id) {
              this.removingServer = server;
              this.removingServerForm.removing = false;
              this.removingServerForm.password = "";
              this.removingServerForm.error.password = false;
              this.removingServerForm.error.code = "";
            }
          },

          submitRemoveServer() {
            this.removingServerForm.removing = true;

            this.removeServer()
              .then(response => {
                this.removingServerForm.removing = false;

                $('#confirmRemoveServer').modal('hide');

                this.reloadServerList()
              })
              .catch(error => {
                this.removingServerForm.removing = false;

                this.removingServerForm.error = error;
              });
          },

          reloadServerList() {
            this.$store.state.serverListLoading = true;

            this.getServerList()
              .then(response => {
                this.$store.state.serverListLoading = false;

                this.$store.state.servers = response.data.servers
              })
              .catch(() => {
                this.$store.state.serverListLoading = false;
              });
          }
        },
        computed: {
          websiteName() {
            return this.$store.state.website.name
          },

          path() {
            return this.$route.path
          },

          key() {
            return this.$store.state.currentServerPlatformMatchKey
          },

          platformAddress() {
            return this.$store.state.platformAddress
          },

          servers() {
            return this.$store.state.servers
          },

          selected_server() {
            return this.$store.state.selected_server
          },

          serverListLoading() {
            return this.$store.state.serverListLoading
          },

          main_server() {
            return this.$store.state.main_server
          },

          isSidebarOpen() {
            return this.$store.state.isSidebarOpen
          },

          sidebarTabsState() {
            return this.$store.state.sidebarTabsState
          }
        },
        mounted() {
          this.startCountDown();
        }
      });
    })
  })
);