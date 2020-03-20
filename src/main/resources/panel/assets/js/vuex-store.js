Vue.use(Vuex);

const store = new Vuex.Store({
  state: {
    langLoading: false,
    routePageLoading: true,
    splashLoadedForRoutePageLoading: false,
    user: {},
    website: {},
    initialPageDataLoading: false,
    splashLoadedForPageDataInitializationLoading: false,
    splashLoadedForLanguage: false,
    logoutLoading: false,
    currentServerPlatformMatchKey: "",
    platformAddress: "",
    servers: [],
    selected_server: [],
    main_server: [],
    serverListLoading: false,
    isSidebarOpen: PanelSidebarStorageUtil.isThereSideBarOpenStatus() ? PanelSidebarStorageUtil.getSidebarOpenStatus() : true,
    sidebarTabsState: PanelSidebarStorageUtil.isThereSideBarTabsState() ? PanelSidebarStorageUtil.getSidebarTabsState() : "website",
    quickNotifications: []
  },

  mutations: {
    SET_LANG(state, payload) {
      loadLanguageAsync(payload);
      LanguageUtil.saveLanguage(payload)
    },

    initializeBasicData(state, data) {
      state.user = data.user;
      state.website = data.website;
      state.currentServerPlatformMatchKey = data.platform_server_match_key;
      state.platformAddress = data.platform_host_address;
      state.servers = data.servers;
      state.quickNotifications = data.quick_notifications;
    }
  },

  actions: {
    setLang({commit}, payload) {
      commit('SET_LANG', payload)
    },

    getBasicUserData(context) {
      return new Promise((resolve, reject) => {
        ApiUtil.get("/api/panel/basicData").then(response => {
          if (response.data.result === "ok") {
            context.commit("initializeBasicData", response.data);

            resolve(response);
          } else if (response.data.result === "error") {
            const errorCode = response.data.error;

            reject(errorCode);
          } else
            reject(NETWORK_ERROR);
        }).catch(() => {
          reject(NETWORK_ERROR);
        });
      });
    },

    toggleSidebar(context) {
      context.state.isSidebarOpen = !context.state.isSidebarOpen;

      PanelSidebarStorageUtil.savePanelSidebarStorageUtil(context.state.isSidebarOpen.toString());
    },

    setSidebarTabsState(context, state) {
      context.state.sidebarTabsState = state;

      PanelSidebarStorageUtil.setSidebarTabsState(state);
    }
  },

  getters: {}
});