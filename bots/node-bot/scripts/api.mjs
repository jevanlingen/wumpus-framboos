import { request } from "undici";
import { config, getHeaders } from '../config/config.mjs';

export async function getGameIds() {
  let responseData = await request(`${config.apiUrl}/games/ids`, { headers: { ...getHeaders() } });
  return await responseData.body.json();
}

export async function doGameAction(gameId, action){
  let responseData = await request(`${config.apiUrl}/games/${gameId}/action/${action}`, { method: 'POST', headers: { ...getHeaders() } });
  return await responseData.body.json();
}

export async function getCompetitionIds() {
  let responseData = await request(`${config.apiUrl}/competitions/ids`, { headers: { ...getHeaders() } });
  return await responseData.body.json();
}

export async function getCompetition(id) {
  let responseData = await request(`${config.apiUrl}/competitions/${id}`, { headers: { ...getHeaders() } });
  return await responseData.body.json();
}

export async function register() {
  try {
    let responseData = await request(config.apiUrl + '/create-account', {
      method: 'POST',
      body: JSON.stringify({
        name: config.username,
        password: config.password
      }),
      headers: {
        ...config.contentTypeHeaders
      }
    });

    if (responseData.statusCode === 201) {
      return true;
    }
    else {
      console.error(responseData.statusCode, responseData);
      return false;
    }

  } catch (e) {
    console.error('Failed to register', e);
    return false;
  }
}
