'use strict';

Vue.directive('tooltip', function (el, binding) {
  if (binding.value !== $(el).attr('data-original-title')) {
    $(el)
      .attr('data-original-title', binding.value)
      .tooltip({
        title: binding.value,
        placement: binding.arg,
        trigger: "hover",
        selector: true
      });

    if ($($(el).data("bs.tooltip").tip).hasClass("show")) {
      $(el).tooltip('show')
    }
  }
});

Vue.component('Main', new Promise(function (resolve) {
    requirejs([
      "/panel/assets/js/chart.min.js"
    ], function () {
      loadComponent('Navbar', '/panel/src/components/Main/Navbar').then(function (Navbar) {
        loadComponent('Sidebar', '/panel/src/components/Main/Sidebar').then(function (Sidebar) {
          resolve({
            components: {
              Navbar,
              Sidebar
            },
            computed: {
              routePageLoading() {
                return this.$store.state.routePageLoading
              },

              initialPageDataLoading() {
                return this.$store.state.initialPageDataLoading
              }
            },
            template: PANO.UI,
            mounted() {
              $('[data-toggle="tooltip"]').tooltip()
            }
          });
        });
      });
    });
  })
);