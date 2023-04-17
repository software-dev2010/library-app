export const oktaConfig = {
    clientId: '0oa8n5jdlbNKocm6Y5d7',
    issuer: 'https://dev-24059684.okta.com/oauth2/default',
    redirectUri: 'https://localhost:3000/login/callback',
    scopes: ['openid', 'profile', 'email'],
    pkce: true,
    disableHttpsCheck: true,
}