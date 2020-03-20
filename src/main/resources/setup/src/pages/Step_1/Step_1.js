'use strict';

Vue.component('Step_1', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {
          disableNextButton: true,
          nextButtonLoading: false,
          backButtonLoading: false
        }
      },
      methods: {
        submit() {
          if (!this.backButtonLoading) {
            this.nextButtonLoading = true

            this.$store.dispatch("nextStep", {
              step: 1,
              websiteName: this.websiteName,
              websiteDescription: this.websiteDescription
            })
          }
        },

        back() {
          if (!this.nextButtonLoading) {
            this.backButtonLoading = true

            this.$store.dispatch("backStep", {
              step: 1
            })
          }
        },

        checkForm() {
          this.disableNextButton = !(this.websiteName !== "" && this.websiteDescription !== "");
        }
      },
      beforeMount() {
        if (this.$store.state.stepState !== 1)
          this.$router.push('/')
        else {
          this.checkForm()
        }
      },
      computed: {
        websiteName: {
          get() {
            return this.$store.state.data.websiteName
          },
          set(value) {
            this.$store.state.data.websiteName = value

            this.checkForm()
          }
        },

        websiteDescription: {
          get() {
            return this.$store.state.data.websiteDescription
          },
          set(value) {
            this.$store.state.data.websiteDescription = value

            this.checkForm()
          }
        }
      }
    });
  })
);