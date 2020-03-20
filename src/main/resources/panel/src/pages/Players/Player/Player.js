'use strict';

Vue.component('Player', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {}
      }
    });
  })
);