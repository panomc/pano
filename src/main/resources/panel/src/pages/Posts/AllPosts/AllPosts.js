'use strict';

Vue.component('AllPosts', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {
          posts_count: 0,
          posts: [],
          page: 0,
          total_page: 1,
          drafting: false,
          deletingPostID: -1,
          deleting: false,
          publishing: false
        }
      },
      props: {
        page_type: {
          type: String,
          default: 'published'
        }
      },
      methods: {
        getInitialData(page) {
          return new Promise((resolve, reject) => {
            ApiUtil.post("/api/panel/initPage/postPage", {
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

                this.posts_count = response.data.posts_count;
                this.posts = response.data.posts;
                this.total_page = response.data.total_page;

                this.page = page;

                this.host = response.data.host;

                if (page === 1 && this.$route.path !== '/panel/posts' && this.$route.path !== '/panel/posts/' && this.$route.path !== '/panel/posts/' + this.page_type && this.$route.path !== '/panel/posts/' + this.page_type + '/')
                  this.$router.push('/panel/posts/' + this.page_type + '/' + page);
                else if (page !== 1)
                  this.$router.push('/panel/posts/' + this.page_type + '/' + page);
              })
              .catch(error => {
                if (error === 'page_not_found') {
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
        },

        moveToDraft(index) {
          this.drafting = true;

          ApiUtil.post("/api/panel/post/moveDraft", {id: this.posts[index].id}).then(response => {
            this.drafting = false;

            this.routePage(this.page, true);
          }).catch(() => {
            this.refreshBrowserPage();
          });
        },

        moveToTrash() {
          this.deleting = true;

          ApiUtil.post("/api/panel/post/moveTrash", {id: this.deletingPostID}).then(response => {
            this.deleting = false;

            $('#confirmDeletePost').modal('hide');

            this.$router.push('/panel/posts/trash');
          })
        },

        deletePost() {
          this.deleting = true;

          ApiUtil.post("/api/panel/post/delete", {id: this.deletingPostID}).then(response => {
            this.deleting = false;

            $('#confirmDeletePost').modal('hide');

            this.routePage(this.page, true);

            this.$toasted.show("Yazınız kalıcı olarak silindi.", {
              position: "bottom-center",
              duration: 5000
            });
          })
        },

        getFormattedDate(date) {
          const dateFromNumberDate = new Date(date * 1000);

          return dateFromNumberDate.getDate() + "." + (dateFromNumberDate.getMonth() + 1) + "." + dateFromNumberDate.getFullYear() + " - " + dateFromNumberDate.getHours() + ":" + dateFromNumberDate.getMinutes() + ":" + dateFromNumberDate.getSeconds();
        },

        publish(id) {
          this.publishing = true;

          ApiUtil.post("/api/panel/post/onlyPublish", {id: id}).then(response => {
            this.publishing = false;

            this.$router.push('/panel/posts');

            this.$toasted.show("Yazınız başarıyla yayınlandı.", {
              position: "bottom-center",
              duration: 5000,
              action: {
                text: 'Yazıyı Görüntüle',
                onClick: (e, toastObject) => {
                  toastObject.goAway(0);

                  window.open('/post/' + id, '_blank');
                }
              }
            });
          })
        }
      },
      beforeMount() {
        this.routePage(typeof this.$route.params.page === 'undefined' ? 1 : parseInt(this.$route.params.page) === 0 ? 1 : parseInt(this.$route.params.page))
      },
      computed: {
        getStatusFromPageType() {
          return this.page_type === 'published' ? 1 : this.page_type === 'draft' ? 2 : 0;
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