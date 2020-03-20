function loadComponent(componentName, path) {
  return new Promise(function (resolve, reject) {
    requirejs([path], function () {
      const component = Vue.component(componentName);

      if (component) {
        resolve(component);
      } else {
        reject();
      }
    });
  });
}

function LoadCSS(cssURL) {
  return new Promise(function (resolve, reject) {

    const link = document.createElement('link');

    link.rel = 'stylesheet';

    link.href = cssURL;

    document.head.appendChild(link);

    link.onload = function () {
      resolve();
    };
  });
}

loadMainJSFiles(function () {
  requirejs.config({
    paths: {
      'router': '/assets/js/router',
      'api-util': '/assets/js/api.util',
      'storage-util': '/assets/js/storage.util',
      'vuex-store': '/assets/js/vuex-store',
      'i18n': '/assets/js/i18n'
    },
    shim: {
      'api-util': ['router'],
      'storage-util': ['api-util'],
      'vuex-store': ['storage-util'],
      'i18n': ['vuex-store']
    }
  });

  requirejs(["i18n"], function () {
    LoadCSS('/assets/css/style.css').then(function () {
      ApiUtil.init();

      const app = new Vue({
        el: '#app',
        template: PANO.UI,
        router,
        i18n,
        store,
        metaInfo() {
          return {
            title: this.$t("Common.Page.title"),
            meta: [
              {name: 'description', content: this.$t("Common.Page.description")}
            ]
          }
        },
        methods: {
          setLang(lang) {
            this.$store.dispatch('setLang', lang)
          }
        },
        computed: {
          langLoading() {
            return this.$store.state.langLoading
          },

          routePageLoading() {
            return this.$store.state.routePageLoading
          }
        },
        beforeMount() {
          let loadLanguage;

          if (LanguageUtil.isThereLanguage())
            loadLanguage = LanguageUtil.getLanguage()
          else if (navigator.language.toUpperCase() === "tr".toUpperCase() || navigator.language.toUpperCase() === "tr-tr".toUpperCase())
            loadLanguage = "tr"
          else
            loadLanguage = "en"

          loadLanguageAsync(loadLanguage)

          this.$store.dispatch("checkCurrentStep")
        }
      });
    });
  });
});