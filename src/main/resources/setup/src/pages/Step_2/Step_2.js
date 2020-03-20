'use strict';

Vue.component('Step_2', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {
          disableNextButton: true,
          errorCode: "NOTHING",
          loading: false,
          nextButtonLoading: false,
          backButtonLoading: false
        }
      },
      methods: {
        submit() {
          if (!this.nextButtonLoading) {
            this.checkDB()
          }
        },

        passStep() {
          if (!this.backButtonLoading) {
            this.nextButtonLoading = true

            this.$store.dispatch("nextStep", {
              step: 2,
              host: this.host,
              dbName: this.dbName,
              username: this.username,
              password: this.password,
              prefix: this.prefix
            })
          }
        },

        back() {
          if (!this.nextButtonLoading) {
            this.backButtonLoading = true

            this.$store.dispatch("backStep", {
              step: 2
            })
          }
        },

        checkDB() {
          this.loading = true

          const {
            host,
            dbName,
            username,
            password
          } = this

          ApiUtil.post("/api/setup/dbConnectionTest", {
            host,
            dbName,
            username,
            password
          })
            .then(response => {
              if (response.data.result === "ok") {
                this.passStep()

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

        checkForm() {
          this.disableNextButton = !(this.host !== "" && this.dbName !== "" && this.username !== "");
        },

        showError: function (error) {
          this.loading = false

          this.errorCode = error;

          $("#databaseError").fadeIn();
        },

        dismissErrorBox() {
          $("#databaseError").fadeOut("slow");
        }
      },
      beforeMount() {
        if (this.$store.state.stepState !== 2) {
          this.$router.push('/')
        } else {
          this.checkForm()
        }
      },
      computed: {
        host: {
          get() {
            return this.$store.state.data.db.host
          },
          set(value) {
            this.$store.state.data.db.host = value

            this.checkForm()
          }
        },

        dbName: {
          get() {
            return this.$store.state.data.db.dbName
          },
          set(value) {
            this.$store.state.data.db.dbName = value

            this.checkForm()
          }
        },

        username: {
          get() {
            return this.$store.state.data.db.username
          },
          set(value) {
            this.$store.state.data.db.username = value

            this.checkForm()
          }
        },

        password: {
          get() {
            return this.$store.state.data.db.password
          },
          set(value) {
            this.$store.state.data.db.password = value

            this.checkForm()
          }
        },

        prefix: {
          get() {
            return this.$store.state.data.db.prefix
          },
          set(value) {
            this.$store.state.data.db.prefix = value

            this.checkForm()
          }
        }
      }
    });
  })
);