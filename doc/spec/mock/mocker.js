/* Usage:
 * $ npm i -g open-api-mocker
 * $ NODE_PATH=/usr/local/lib/node_modules node mocker.js 
 * 
 * See: https://github.com/jormaechea/open-api-mocker/blob/master/docs/README.md#custom-server */

async function start() {
  const OpenApiMocker = require('open-api-mocker')
  const options = { schema: '../api.yml' }
  const mocker = new OpenApiMocker(options)
  await mocker.validate()
  await mocker.mock()
}

start()
