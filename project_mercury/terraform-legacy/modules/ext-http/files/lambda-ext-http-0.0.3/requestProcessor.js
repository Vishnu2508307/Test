const request = require('request-promise-native');
const clone = require('stringify-clone');
const fs = require('fs');

const userAgent = 'SPR-aero/1.0';
const tmpFilename = '/tmp/' + Date.now() + '.tmp';


function getRemoteData(options) {
    // stomp on some of the options.
    let _options = {
        ...options,
        headers: {
            ...options.headers,
            'User-Agent': userAgent
        }
    };

    return new Promise((resolve, reject) => {
      request(_options)
      .on('response', (res) => {
        if (res.statusCode !== 200) {
          reject(false);
        }
      })
      .on('error', (err) => {
        reject(false);
      })
      .pipe(fs.createWriteStream(tmpFilename))
      .on('finish', () => {
        resolve(true);
      })
      .on('error', (err) => {
        reject(false);
      });
    });
  }

async function processRequest(options) {
    // stomp on some of the options.
    let _options = {
        ...options,
        headers: {
            ...options.headers,
            'User-Agent': userAgent
        },
        simple: false, // we want to disable non-2xx StatusCodeErrors being raised.
        time: true // add timing profiling to the responses
    };

    let requestResponseLog = [];

    try {
        // if external data source required (eg, for POST, PUT), fetch it now (optional)
        if (_options.remoteDataOpts) {
            await getRemoteData(_options.remoteDataOpts); // fetches and temporarily saves external data

            if (_options.formData) { // if there is POST formData, pass in the tmp file data
                _options.formData[_options.remoteDataOpts.formDataField] = fs.createReadStream(tmpFilename);
            }
            // future: if other senarios are required besides formData, handle them here

            delete _options.remoteDataOpts; // delete from _options to not confuse the next request
        }

        // main request
        await request(_options)
            .on('request', function(req) {
                let data = {
                    operation: 'request',
                    uri: this.uri.href,
                    method: this.method,
                    headers: clone(this.headers)
                };
                if (this.body) {
                    data.body = this.body.toString('utf8')
                }
                requestResponseLog.push(data);
            })
            .on('complete', function(res, body) {
                requestResponseLog.push({
                        operation: 'response',
                        uri: this.uri.href,
                        headers: clone(res.headers),
                        statusCode: res.statusCode,
                        body: res.body,
                        time: {
                            timingStart: res.timingStart,
                            timings: res.timings,
                            timingPhases: res.timingPhases
                        }
                });
            })
            .on('redirect', function() {
                requestResponseLog.push({
                        operation: 'redirect',
                        statusCode: this.response.statusCode,
                        headers: clone(this.response.headers),
                        uri: this.uri.href
                });
            });
    } catch(err) {
        requestResponseLog.push({
            operation: 'error',
            body: err.message
        });
        throw new Error(JSON.stringify(requestResponseLog));
    }

    return requestResponseLog;
}

module.exports = processRequest;
