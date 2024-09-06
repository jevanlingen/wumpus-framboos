export const config = {
  username: 'Mike Hunt',
  password: 'bacon1234',
  apiUrl: 'http://localhost',
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
