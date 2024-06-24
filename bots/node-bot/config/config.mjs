export const config = {
  username: 'BaconBot4',
  password: 'bacon1234',
  apiUrl: 'http://localhost:8080',
  contentTypeHeaders: {
    "content-type": "application/json"
  },
}

export function getAuthorizationHeaders(){
  return {
    "Authorization": `Basic ${btoa(config.username + ':' + config.password)}`
  }
}

export function getHeaders() {
  return {
    ...getAuthorizationHeaders(),
    ...config.contentTypeHeaders
  };
}
