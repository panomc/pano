'use strict';

Vue.component('Categories', new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {
          category_count: 0,
          categories: [],
          page: 0,
          total_page: 1,
          deleteID: 0,
          deleting: false,
          host: "",
          showEditCategory: false,
          categoryForm: {
            id: 0,
            name: '',
            description: '',
            url: '',
            colorCode: '#E6E9ED',
            error: {
              name: false,
              description: false,
              url: false
            }
          },
          addingOrSaving: false
        }
      },
      methods: {
        getInitialData(page) {
          return new Promise((resolve, reject) => {
            ApiUtil.post("/api/panel/initPage/posts/categoryPage", {page: page}).then(response => {
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

                this.category_count = response.data.category_count;
                this.categories = response.data.categories;
                this.total_page = response.data.total_page;

                this.page = page;

                this.host = response.data.host;

                if (page === 1 && this.$route.path !== '/panel/posts/categories' && this.$route.path !== '/panel/posts/categories/')
                  this.$router.push('/panel/posts/categories/' + page);
                else if (page !== 1)
                  this.$router.push('/panel/posts/categories/' + page);
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
        },

        onDeleteClick(id) {
          this.deleteID = id;
        },

        deleteCategory() {
          this.deleting = true;

          ApiUtil.post("/api/panel/post/category/delete", {id: this.deleteID}).then(response => {
            if (response.data.result === "ok") {
              this.deleting = false;

              $('#confirmDeleteCategory').modal('hide');

              this.routePage(this.page, true);
            } else {
              this.refreshBrowserPage()
            }
          }).catch(() => {
            this.refreshBrowserPage()
          });
        },

        resetFormErrors() {
          this.categoryForm.error.name = false;
          this.categoryForm.error.description = false;
          this.categoryForm.error.url = false;
        },

        onShowCreateCategoryButtonClick() {
          this.resetFormErrors();

          this.showEditCategory = false;

          this.categoryForm.id = 0;
          this.categoryForm.name = "";
          this.categoryForm.description = "";
          this.categoryForm.url = "";
          this.categoryForm.colorCode = "#E6E9ED";
        },

        onShowEditCategoryButtonClick(index) {
          this.resetFormErrors();

          this.showEditCategory = true;

          this.categoryForm.id = this.categories[index].id;
          this.categoryForm.name = this.categories[index].title;
          this.categoryForm.description = this.categories[index].description;
          this.categoryForm.url = this.categories[index].url;
          this.categoryForm.colorCode = "#" + this.categories[index].color;
        },

        submitSaveOrAdd() {
          this.addingOrSaving = true;

          if (this.showEditCategory) {
            ApiUtil.post("/api/panel/post/category/update", {
              id: this.categoryForm.id,
              name: this.categoryForm.name,
              description: this.categoryForm.description,
              url: this.categoryForm.url,
              colorCode: this.categoryForm.colorCode
            }).then(response => {
              this.addingOrSaving = false;

              if (response.data.result === "ok") {
                $('#addEditCategory').modal('hide');

                this.routePage(this.page, true);
              } else if (response.data.result === 'error' && typeof response.data.error !== 'undefined') {
                this.categoryForm.error = response.data.error;
              } else {
                this.refreshBrowserPage()
              }
            }).catch(() => {
              this.refreshBrowserPage()
            });
          } else {
            ApiUtil.post("/api/panel/post/category/add", {
              name: this.categoryForm.name,
              description: this.categoryForm.description,
              url: this.categoryForm.url,
              colorCode: this.categoryForm.colorCode
            }).then(response => {
              this.addingOrSaving = false;

              if (response.data.result === "ok") {
                $('#addEditCategory').modal('hide');

                this.routePage(1, true);
              } else if (response.data.result === 'error' && typeof response.data.error !== 'undefined') {
                this.categoryForm.error = response.data.error;
              } else {
                this.refreshBrowserPage()
              }
            }).catch(() => {
              this.refreshBrowserPage()
            });
          }
        }
      },
      computed: {
        isSaveAddButtonDisabled() {
          return this.categoryForm.name === "" || this.categoryForm.description === "" || this.categoryForm.url === ""
        }
      },
      beforeMount() {
        this.routePage(typeof this.$route.params.page === 'undefined' ? 1 : parseInt(this.$route.params.page))
      },
      watch: {
        '$route'(to, from) {
          this.routePage(typeof this.$route.params.page === 'undefined' ? 1 : parseInt(this.$route.params.page))
        }
      }
    });
  })
);