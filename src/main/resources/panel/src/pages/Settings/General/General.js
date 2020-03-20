'use strict';

Vue.component('General', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      computed: {
        websiteName() {
          return this.$store.state.website.name
        },

        websiteDescription() {
          return this.$store.state.website.description
        }
      }
    });
  })
);