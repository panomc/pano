'use strict';

Vue.component('EditPost', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      components: {
        PostEditor: function (resolve, reject) {
          loadComponent('PostEditor', '/panel/src/views/PostEditor').then(resolve, reject);
        }
      }
    });
  })
);