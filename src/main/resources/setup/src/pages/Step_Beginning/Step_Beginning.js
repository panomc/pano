'use strict';

Vue.component('Step_Beginning', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {
          nextButtonLoading: false
        }
      },
      methods: {
        start() {
          this.nextButtonLoading = true

          this.$store.dispatch("nextStep", {
            step: 0
          })
        }
      }
    });
  })
);