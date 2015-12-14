/**
 * @providesModule FileTransfer
 */

'use strict';

var { NativeModules } = require('react-native');

class FileTransfer {
  constructor() {
  }

  static upload(opts, callback) {
    NativeModules.FileTransfer.upload(opts, callback);
  }
}

module.exports = FileTransfer;
