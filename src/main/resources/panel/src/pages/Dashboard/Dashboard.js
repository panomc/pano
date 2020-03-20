'use strict';

Vue.component(
  'Dashboard',
  new Promise(function (resolve) {
    resolve({
      template: PANO.UI,
      data() {
        return {
          registered_player_count: 0,
          getting_started_blocks: {
            welcome_board: false
          },
          post_count: 0
        };
      },
      methods: {
        getInitialData() {
          return new Promise((resolve, reject) => {
            ApiUtil.get('/api/panel/initPage/dashboard')
              .then(response => {
                if (response.data.result === 'ok') {
                  resolve(response);
                } else if (response.data.result === 'error') {
                  const errorCode = response.data.error;

                  reject(errorCode);
                } else reject(NETWORK_ERROR);
              })
              .catch(() => {
                reject(NETWORK_ERROR);
              });
          });
        },

        onCloseGettingStartedCard() {
          ApiUtil.post(
            '/api/panel/dashboard/closeGettingStartedCard',
            {}
          ).catch(() => {
            // reject(NETWORK_ERROR);
          });
        }
      },
      beforeMount() {
        this.$store.state.initialPageDataLoading = true;

        this.getInitialData()
          .then(response => {
            this.$store.state.initialPageDataLoading = false;

            if (
              this.$store.state.splashLoadedForPageDataInitializationLoading ===
              false
            ) {
              this.$store.state.splashLoadedForPageDataInitializationLoading = true;
            }

            this.registered_player_count =
              response.data.registered_player_count;
            this.post_count = response.data.post_count;

            this.getting_started_blocks = response.data.getting_started_blocks;
          })
          .catch(() => {
            // this.$refs.recaptcha.reset();
            //
            // this.resetFormButtonStatus()
            //
            // if (typeof error != "undefined")
            //     this.showLoginError(error)
          });
      },
      mounted() {
        const ctx1 = document.getElementById('playersChart');
        const playersChart = new Chart(ctx1, {
          type: 'line',
          data: {
            labels: ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'],
            datasets: [
              {
                data: [8, 5, 9, 7, 12, 8, 9],
                backgroundColor: 'rgba(114, 192, 118, .08)',
                borderColor: '#72c076',
                borderWidth: 2.5,
                pointRadius: 0
              }
            ]
          },
          options: {
            scales: {
              yAxes: [
                {
                  ticks: {
                    beginAtZero: false
                  },
                  display: false
                }
              ],
              xAxes: [
                {
                  display: false
                }
              ]
            },
            legend: {
              display: false
            }
          }
        });

        const ctx2 = document.getElementById('ticketsChart');
        const ticketsChart = new Chart(ctx2, {
          type: 'line',
          data: {
            labels: ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'],
            datasets: [
              {
                data: [8, 5, 9, 7, 12, 8, 9],
                backgroundColor: 'rgba(252, 110, 81, .08)',
                borderColor: '#fc6e51',
                borderWidth: 2.5,
                pointRadius: 0
              }
            ]
          },
          options: {
            scales: {
              yAxes: [
                {
                  ticks: {
                    beginAtZero: false
                  },
                  display: false
                }
              ],
              xAxes: [
                {
                  display: false
                }
              ]
            },
            legend: {
              display: false
            }
          }
        });

        const ctx3 = document.getElementById('postsChart');
        const postsChart = new Chart(ctx3, {
          type: 'line',
          data: {
            labels: ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'],
            datasets: [
              {
                data: [8, 5, 9, 7, 12, 8, 9],
                backgroundColor: 'rgba(246, 187, 66, .08)',
                borderColor: '#F6BB42',
                borderWidth: 2.5,
                pointRadius: 0
              }
            ]
          },
          options: {
            scales: {
              yAxes: [
                {
                  ticks: {
                    beginAtZero: false
                  },
                  display: false
                }
              ],
              xAxes: [
                {
                  display: false
                }
              ]
            },
            legend: {
              display: false
            }
          }
        });

        const ctx = document.getElementById('myChart');
        const myChart = new Chart(ctx, {
          type: 'line',
          data: {
            labels: ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'],
            datasets: [
              {
                data: [39, 45, 83, 53, 89, 72, 99],
                borderColor: '#1976d2',
                backgroundColor: 'transparent',
                borderWidth: 2.5
              }
            ]
          },
          options: {
            scales: {
              yAxes: [
                {
                  ticks: {
                    fontColor: '#1976d2',
                    beginAtZero: true,
                    maxTicksLimit: 10,
                    padding: 20
                  },
                  gridLines: {
                    drawTicks: false,
                    display: true
                  },
                  display: true
                }
              ],
              xAxes: [
                {
                  ticks: {
                    fontColor: '#1976d2',
                    beginAtZero: true,
                    maxTicksLimit: 10,
                    padding: 20
                  },
                  gridLines: {
                    drawTicks: false,
                    display: false
                  },
                  display: true
                }
              ]
            },
            legend: {
              display: false
            }
          }
        });
      }
    });
  })
);
