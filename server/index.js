const Koa = require('koa');
const Router = require('koa-router');
const bodyParser = require('koa-bodyparser');
const cors = require('@koa/cors');

const qs = require('querystring');
const rp = require('request-promise-native');

const SCOPE = "email_r listings_r transactions_r";

const RETURN_ORIGIN = (process.env.URL_RETURN || "http://localhost:3449");

const oauthCallbackURI = (process.env.URL_AUTH || "http://localhost:3000") +
    "/oauth-callback";
const oauthReturnURI = RETURN_ORIGIN + "/oauth.html";

const URLS = {
    api: "https://openapi.etsy.com/v2",
    request: "https://openapi.etsy.com/v2/oauth/request_token",
    token: "https://openapi.etsy.com/v2/oauth/access_token",
};

const baseOauth = {
    consumer_key: process.env.ETSY_CLIENT,
    consumer_secret: process.env.ETSY_SECRET,
    callback: oauthCallbackURI,
};

function withAuth(requestData) {
    const oauth = { ...baseOauth };

    if (requestData.token) oauth.token = requestData.token;
    if (requestData.tokenSecret) oauth.token_secret = requestData.tokenSecret;
    if (requestData.verifier) oauth.verifier = requestData.verifier;

    return rp({
        url: requestData.url,
        method: requestData.method || 'POST',
        oauth: oauth,
        form: requestData.form,
    });
}

const app = new Koa();
const router = new Router();

const corsMiddleware = cors({
    origin: ctx => {
        const origin = ctx.get('Origin');
        if (origin === RETURN_ORIGIN) {
            return RETURN_ORIGIN;
        }
    },
    allowMethods: "POST",
});

// this is garbage
const tempTokenSecrets = {};

router.get('/', async ctx => {
    ctx.body = 'Hello World';
});

router.get('/oauth', async ctx => {
    let resp;
    try {
        resp = await withAuth({
            url: URLS.request,
            form: {scope: SCOPE},
        });
    } catch (e) {
        const errorInfo = qs.parse(decodeURIComponent(e.error));
        console.warn(errorInfo);
        ctx.status = 500;
        return;
    }

    const info = qs.parse(resp);
    if (!info.login_url) {
        console.log(info);
        ctx.status = 400;
        return;
    }

    tempTokenSecrets[info.oauth_token] = info.oauth_token_secret;
    ctx.redirect(info.login_url);
});

router.get('/oauth-callback', async ctx => {
    if (!ctx.query.oauth_token) {
        ctx.status = 400;
        return;
    }

    const resp = await withAuth({
        url: URLS.token,
        token: ctx.query.oauth_token,
        tokenSecret: tempTokenSecrets[ctx.query.oauth_token],
        verifier: ctx.query.oauth_verifier,
    });

    delete tempTokenSecrets[ctx.query.oauth_token];

    const info = qs.parse(resp);
    ctx.redirect(oauthReturnURI + "?" + qs.stringify(info));
});

router.post('/proxy', corsMiddleware, bodyParser(), async ctx => {
    const { body } = ctx.request;

    const resp = await withAuth({
        method: body.method,
        url: URLS.api + body.url,
        token: body['oauth-token'],
        tokenSecret: body['oauth-secret'],
    });

    ctx.response.type = 'application/json';
    ctx.body = resp;
});

app.use(corsMiddleware)
    .use(router.routes())
    .use(router.allowedMethods())
    .listen(3000, () => {
        console.log("Ready on 3000");
    });
