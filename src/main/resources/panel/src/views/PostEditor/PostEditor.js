'use strict';

Vue.component('PostEditor', new Promise(function (resolve) {
    LoadCSS('/panel/assets/css/quill.snow.css').then(function () {
      resolve({
        data() {
          return {
            post: {
              id: -1,
              title: "Yazı başlığı 🖊",
              text: "",
              category: -1,
              status: -1,
              date: 0,
              imageCode: ""
            },
            category_count: 0,
            categories: [],
            savedFirstTime: false,
            publishing: false,
            quill: null,
            error: {
              image: false
            },
            drafting: false,
            deleting: false
          }
        },
        template: PANO.UI,
        props: ['editorMode'],
        methods: {
          onEditorInput(event) {
            this.post.text = event.target.innerHTML;
          },

          getInitialCategoriesData() {
            return new Promise((resolve, reject) => {
              ApiUtil.get("/api/panel/post/category/categories").then(response => {
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

          submitPost() {
            this.error.category = false;
            this.publishing = true;

            return new Promise((resolve, reject) => {
              ApiUtil.post("/api/panel/post/publish", {
                id: this.post.id,
                title: this.post.title,
                text: this.quill.getHTML(),
                category: this.post.category,
                imageCode: this.post.imageCode
              }).then(response => {
                this.publishing = false;

                if (response.data.result === "ok") {
                  resolve(response);
                } else if (response.data.result === "error") {
                  this.error = response.data.error;

                  const errorCode = response.data.error;

                  reject(errorCode);
                } else
                  reject(NETWORK_ERROR);
              }).catch(() => {
                this.publishing = false;
                reject(NETWORK_ERROR);
              });
            });
          },

          onSubmit() {
            this.submitPost().then(response => {
              let postID = this.post.id;

              if (this.post.id === -1) {
                postID = response.data.id;
                this.$router.push('/panel/posts/post/' + response.data.id);
              }

              this.$toasted.show("Yazınız başarıyla yayınlandı.", {
                position: "bottom-center",
                duration: 5000,
                action: {
                  text: 'Yazıyı Görüntüle',
                  onClick: (e, toastObject) => {
                    toastObject.goAway(0);

                    window.open('/post/' + postID, '_blank');
                  }
                }
              });
            })
          },

          getInitialData(id) {
            return new Promise((resolve, reject) => {
              ApiUtil.post("/api/panel/initPage/editPost", {id: id}).then(response => {
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

          initPost(id) {
            this.post.id = parseInt(this.$route.params.post_id);
            this.$store.state.initialPageDataLoading = true;

            this.initCategories()
              .then(response => {
                this.getInitialData(id)
                  .then(response => {
                    this.$store.state.initialPageDataLoading = false;

                    if (this.$store.state.splashLoadedForPageDataInitializationLoading === false) {
                      this.$store.state.splashLoadedForPageDataInitializationLoading = true;
                    }

                    this.post.title = response.data.post.title;
                    this.post.text = response.data.post.post;
                    this.post.category = parseInt(response.data.post.category_id);
                    this.post.writer_user_id = response.data.post.writer_user_id;
                    this.post.date = response.data.post.date;
                    this.post.status = parseInt(response.data.post.status);
                    this.post.imageCode = response.data.post.image;

                    this.savedFirstTime = true;

                    this.quill.setHTML(response.data.post.post)
                  })
                  .catch(error => {
                    if (error === 'post_not_found') {
                      this.$store.state.initialPageDataLoading = false;

                      if (this.$store.state.splashLoadedForPageDataInitializationLoading === false) {
                        this.$store.state.splashLoadedForPageDataInitializationLoading = true;
                      }

                      this.$router.push('/panel/error-404');
                    }
                  });
              });
          },

          initCategories() {
            return new Promise((resolve, reject) => {

              this.getInitialCategoriesData()
                .then(response => {
                  this.category_count = response.data.category_count;
                  this.categories = response.data.categories;

                  resolve(response);
                });
            });
          },

          onDraftClick() {
            this.drafting = true;

            ApiUtil.post("/api/panel/post/moveDraft", {id: this.post.id}).then(response => {
              this.$router.push('/panel/posts/draft');
            })
          },

          moveToTrash() {
            this.deleting = true;

            ApiUtil.post("/api/panel/post/moveTrash", {id: this.post.id}).then(response => {
              $('#confirmDeletePost').modal('hide');

              this.$router.push('/panel/posts/trash');
            })
          },

          deletePost() {
            this.deleting = true;

            ApiUtil.post("/api/panel/post/delete", {id: this.post.id}).then(response => {
              this.deleting = false;

              $('#confirmDeletePost').modal('hide');

              this.$router.push('/panel/posts');

              this.$toasted.show("Yazınız kalıcı olarak silindi.", {
                position: "bottom-center",
                duration: 5000
              });
            })
          },

          readFile(event) {
            this.error.image = false;
            const input = event.target;

            if (input.files && input.files[0]) {
              if (input.files[0].size > 1000000) {
                this.error.image = true;
                return;
              }

              const image = new Image();

              const reader = new FileReader();

              const vue = this;

              reader.onload = (e) => {
                image.src = e.target.result;

                image.onload = function () {
                  if (this.width < 600 || this.height < 300) {
                    vue.error.image = true;
                  } else {
                    vue.post.imageCode = e.target.result;
                  }
                };
              };

              reader.readAsDataURL(input.files[0]);
            }
          },

          imageHandler() {
            const range = this.quill.getSelection();
            const value = prompt('What is the image URL');
            if (value) {
              this.quill.insertEmbed(range.index, 'image', value, Quill.sources.USER);
            }
          },

          getFormattedDate(date) {
            const dateFromNumberDate = new Date(date * 1000);

            return dateFromNumberDate.getDate() + "." + (dateFromNumberDate.getMonth() + 1) + "." + dateFromNumberDate.getFullYear() + " - " + dateFromNumberDate.getHours() + ":" + dateFromNumberDate.getMinutes() + ":" + dateFromNumberDate.getSeconds();
          }
        },
        beforeMount() {
          this.$store.state.initialPageDataLoading = true;

          if (this.editorMode !== 'edit') {
            this.initCategories()
              .then(response => {
                this.$store.state.initialPageDataLoading = false;

                if (this.$store.state.splashLoadedForPageDataInitializationLoading === false) {
                  this.$store.state.splashLoadedForPageDataInitializationLoading = true;
                }
              });
          }
        },
        mounted() {
          const quill = new Quill('#editor', {
            modules: {
              toolbar: {
                container: '#editorToolbar',
                handlers: {
                  image: this.imageHandler
                }
              }
            },
            theme: 'snow'
          });

          this.quill = quill;

          quill.setHTML = (html) => {
            quill.container.firstChild.innerHTML = html;
          };

          quill.getHTML = () => {
            return quill.container.firstChild.innerHTML;
          };

          if (this.editorMode === 'edit')
            this.initPost(typeof this.$route.params.post_id === 'undefined' ? 1 : parseInt(this.$route.params.post_id))
        },
        computed: {
          lengthEditorText() {
            return extractContent(this.post.text).length
          },

          status() {
            return this.post.status === 0 ? 'Çöp' : this.post.status === 1 ? 'Yayında' : this.post.status === 2 ? 'Taslak' : '-';
          }
        },
        watch: {
          '$route'(to, from) {
            this.initPost(typeof this.$route.params.post_id === 'undefined' ? 1 : parseInt(this.$route.params.post_id))
          }
        }
      });
    });
  })
);