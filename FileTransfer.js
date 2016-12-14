/**
 * @providesModule FileTransfer
 */

'use strict';

var {
  NativeModules,
  DeviceEventEmitter
} = require('react-native');

const noop = () => {};
let _progressCB = noop;


DeviceEventEmitter.addListener('upload_progress', function(evt) {
  _progressCB(evt.progress);
});

class FileTransfer {
  constructor() {
  }

  static upload(opts, callback, progressCB) {
    _progressCB = progressCB;
    NativeModules.FileTransfer.upload(opts, (...args) => {
      _progressCB = noop;
      callback.apply(null, args);
    });
  }
}

module.exports = FileTransfer;
