'use strict';

Vue.component('Step_3', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {
          justVariable: 54,
          backButtonLoading: false,
          connectPanoAccountButtonStatus: "",
          savingPanoAccount: false,
          disableFinishButton: true,
          finishButtonLoading: false,
          errorCode: ""
        }
      },
      methods: {
        popupCenter(url, title, w, h) {
          // Fixes dual-screen position                         Most browsers      Firefox
          const dualScreenLeft = window.screenLeft !== undefined ? window.screenLeft : window.screenX;
          const dualScreenTop = window.screenTop !== undefined ? window.screenTop : window.screenY;

          const width = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
          const height = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;

          const systemZoom = width / window.screen.availWidth;
          const left = (width - w) / 2 / systemZoom + dualScreenLeft;
          const top = (height - h) / 2 / systemZoom + dualScreenTop;
          const newWindow = window.open(url, title, 'toolbar=no, location=no, directories=no, status=no, menubar=no, resizable=no, copyhistory=no, scrollbars=yes, width=' + w / systemZoom + ', height=' + h / systemZoom + ', top=' + top + ', left=' + left);

          // Puts focus on the newWindow
          if (window.focus) newWindow.focus();

          return newWindow;
        },

        showError(error) {
          this.finishButtonLoading = false

          this.errorCode = error;

          $("#finishError").fadeIn();
        },

        sendPlatformData(window) {
          window.postMessage({
            type: "PanoPlatform",
            command: "returnPlatformData",
            platform: {
              websiteName: this.websiteName,
              websiteDescription: this.websiteDescription,
              host: this.host,
              ip: this.ip
            }
          }, "http://localhost:8080");
        },

        savePanoAccount() {
          ApiUtil.post("/api/setup/panoAccount/save", {
            username: this.panoAccountUsername,
            email: this.panoAccountEmail,
            access_token: this.panoAccountAccessToken
          })
            .then(response => {
              if (response.data.result === "ok") {
                this.savingPanoAccount = false

              } else if (response.data.result === "error") {
                const errorCode = response.data.error

                this.showError(errorCode)
              } else
                this.showError(NETWORK_ERROR)
            })
            .catch(() => {
              this.showError(NETWORK_ERROR)
            })

        },

        disconnectPanoAccount() {
          this.savingPanoAccount = true;
          this.connectPanoAccountButtonStatus = "LOADING"

          ApiUtil.post("/api/setup/panoAccount/disconnect", {})
            .then(response => {
              this.savingPanoAccount = false;

              if (response.data.result === "ok") {
                this.connectPanoAccountButtonStatus = "";

                this.$store.state.data.panoAccount.access_token = "";
                this.$store.state.data.panoAccount.username = "";
                this.$store.state.data.panoAccount.email = "";

              } else if (response.data.result === "error") {
                const errorCode = response.data.error;

                this.showError(errorCode)
              } else
                this.showError(NETWORK_ERROR)
            })
            .catch(() => {
              this.showError(NETWORK_ERROR)
            })
        },

        connectPanoAccount() {
          const vue = this;

          const newWindow = this.popupCenter("http://localhost:8080/login-platform", "Pano Giriş", "550", "620")

          // Create IE + others compatible event handler
          const eventMethod = window.addEventListener ? "addEventListener" : "attachEvent";
          const eventer = window[eventMethod];
          const messageEvent = eventMethod === "attachEvent" ? "onmessage" : "message";

          // Listen to message from child window
          eventer(messageEvent, function (event) {
            // Check if origin is proper
            if (event.origin !== 'http://localhost:8080' && event.data.type !== "PanoMC") {
              return
            }

            if (event.data.command === "getPlatformData")
              vue.sendPlatformData(newWindow)
            else if (event.data.command === "returnConnectedData") {
              newWindow.close()

              vue.connectPanoAccountButtonStatus = "LOADING"

              vue.savingPanoAccount = true

              vue.$store.state.data.panoAccount.access_token = event.data.token;
              vue.$store.state.data.panoAccount.username = event.data.account.username;
              vue.$store.state.data.panoAccount.email = event.data.account.email;

              if (vue.$store.state.data.account.username === "")
                vue.$store.state.data.account.username = event.data.account.username;

              if (vue.$store.state.data.account.email === "")
                vue.$store.state.data.account.email = event.data.account.email;

              vue.checkForm()

              vue.savePanoAccount()
            }
          }, false);
        },

        finish() {
          this.finishButtonLoading = true

          const {
            username,
            email,
            password
          } = this

          ApiUtil.post("/api/setup/finish", {
            username,
            email,
            password
          })
            .then(response => {
              if (response.data.result === "ok") {
                window.location.assign("/panel")

              } else if (response.data.result === "error") {
                const errorCode = response.data.error

                this.showError(errorCode)
              } else {
                this.showError(NETWORK_ERROR)
                console.log(response)
              }
            })
            .catch(() => {
              this.showError(NETWORK_ERROR)
            })
        },

        back() {
          if (!this.nextButtonLoading) {
            this.backButtonLoading = true;

            this.$store.dispatch("backStep", {
              step: 3
            })
          }
        },

        checkForm() {
          this.disableFinishButton = !(this.username !== "" && this.email !== "" && this.password !== "");
        },

        dismissErrorBox() {
          $("#finishError").fadeOut("slow");
        }
      },
      beforeMount() {
        if (this.$store.state.stepState !== 3)
          this.$router.push('/');
      },
      computed: {
        websiteName() {
          return this.$store.state.data.websiteName
        },

        websiteDescription() {
          return this.$store.state.data.websiteDescription
        },

        host() {
          return this.$store.state.data.host
        },

        ip() {
          return this.$store.state.data.ip
        },

        panoAccountUsername() {
          return this.$store.state.data.panoAccount.username
        },

        panoAccountEmail() {
          return this.$store.state.data.panoAccount.email
        },

        panoAccountAccessToken() {
          return this.$store.state.data.panoAccount.access_token
        },

        username: {
          get() {
            return this.$store.state.data.account.username
          },
          set(value) {
            this.$store.state.data.account.username = value

            this.checkForm()
          }
        },

        email: {
          get() {
            return this.$store.state.data.account.email
          },
          set(value) {
            this.$store.state.data.account.email = value

            this.checkForm()
          }
        },

        password: {
          get() {
            return this.$store.state.data.account.password
          },
          set(value) {
            this.$store.state.data.account.password = value

            this.checkForm()
          }
        }
      }
    });
  })
);