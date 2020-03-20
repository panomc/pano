const NETWORK_ERROR = "NETWORK_ERROR";

const config = {
  headers: {
    'Access-Control-Allow-Origin': '*',
    'Content-Type': 'application/json',
  }
};

axios.interceptors.response.use(
  function (response) {
    return response;
  },
  error => {
    if (error.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      console.log(error.response.data);
      console.log(error.response.status);
      console.log(error.response.headers);
    } else if (error.request) {
      // The request was made but no response was received
      // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
      // http.ClientRequest in node.js
      console.log(error.request);
    } else {
      // Something happened in setting up the request that triggered an Error
      console.log("Error", error.message);
    }

    console.log(error.config);
  }
);

const ApiUtil = {

  init() {
    axios.defaults.headers.post['Content-Type'] = 'application/json;charset=utf-8';
    axios.defaults.headers.post['Access-Control-Allow-Origin'] = '*';
  },

  get(resource, config = this.config) {
    return axios.get(resource, config)
  },

  post(resource, data, config = this.config) {
    return axios.post(resource, data, config)
  },

  put(resource, data, config = this.config) {
    return axios.put(resource, data, config)
  },

  delete(resource, config = this.config) {
    return axios.delete(resource, config)
  },

  /**
   * Perform a custom Axios request.
   *
   * data is an object containing the following properties:
   *  - method
   *  - url
   *  - data ... request payload
   *  - auth (optional)
   *    - username
   *    - password
   **/
  customRequest(data) {
    return axios(data)
  }
};
