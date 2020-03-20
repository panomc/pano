'use strict';

Vue.component('AllTickets', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {
          tickets_count: 0,
          tickets: [],
          page: 0,
          total_page: 1
        }
      },
      props: {
        page_type: {
          type: String,
          default: 'all'
        }
      },
      methods: {
        getInitialData(page) {
          return new Promise((resolve, reject) => {
            ApiUtil.post("/api/panel/initPage/ticketPage", {
              page: page,
              page_type: this.getStatusFromPageType
            }).then(response => {
              if (response.data.result === "ok") {
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

        routePage(page, forceReload = false) {
          if (page !== this.page || forceReload) {
            this.$store.state.initialPageDataLoading = true;

            this.getInitialData(page)
              .then(response => {
                this.$store.state.initialPageDataLoading = false;

                if (this.$store.state.splashLoadedForPageDataInitializationLoading === false) {
                  this.$store.state.splashLoadedForPageDataInitializationLoading = true;
                }

                this.tickets_count = response.data.tickets_count;
                this.tickets = response.data.tickets;
                this.total_page = response.data.total_page;

                this.page = page;

                this.host = response.data.host;

                if (page === 1 && this.$route.path !== '/panel/tickets' && this.$route.path !== '/panel/tickets/' && this.$route.path !== '/panel/tickets/' + this.page_type && this.$route.path !== '/panel/tickets/' + this.page_type + '/')
                  this.$router.push('/panel/tickets/' + this.page_type + '/' + page);
                else if (page !== 1)
                  this.$router.push('/panel/tickets/' + this.page_type + '/' + page);
              })
              .catch(error => {
                if (error === 'PAGE_NOT_FOUND') {
                  this.$store.state.initialPageDataLoading = false;

                  if (this.$store.state.splashLoadedForPageDataInitializationLoading === false) {
                    this.$store.state.splashLoadedForPageDataInitializationLoading = true;
                  }

                  this.$router.push('/panel/error-404');
                }
              });
          }
        },

        refreshBrowserPage() {
          location.reload();
        }
      },
      beforeMount() {
        this.routePage(typeof this.$route.params.page === 'undefined' ? 1 : parseInt(this.$route.params.page) === 0 ? 1 : parseInt(this.$route.params.page))
      },
      computed: {
        getStatusFromPageType() {
          return this.page_type === 'all' ? 1 : this.page_type === 'waitingReply' ? 2 : 0;
        }
      },
      watch: {
        '$route'(to, from) {
          this.routePage(typeof this.$route.params.page === 'undefined' ? 1 : parseInt(this.$route.params.page), true)
        }
      }
    });
  })
);