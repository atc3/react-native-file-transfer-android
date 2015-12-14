#react-native-image-upload-android

Upload images or files through a multipart/form-data request

###Installation

```
npm install react-native-file-transfer-android
```

Add to your settings.gradle:
```
include ':RNFileTransfer', ':app'
project(':RNFileTransfer').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-file-transfer-android/android')
```

Add to your android/build.gradle:
```
dependencies {
  ...
  compile project(':RNFileTransfer')
}
```

Add to MainActivity.java
```
import com.burlap.filetransfer.FileTransferPackage;
...
mReactInstanceManager = ReactInstanceManager.builder()
        .setApplication(getApplication())
        .setBundleAssetName("index.android.bundle")
        .setJSMainModuleName("index.android")
        .addPackage(new MainReactPackage())
        .addPackage(new FileTransferPackage())
```

###Usage:

```javascript
var FileTransfer = require('react-native-file-transfer-android');
FileTransfer.upload({
  uri: uri,
  uploadUrl: 'http://example.com/upload',
  fileName: 'temp.jpg',
  mimeType: 'image/jpg',
  headers: {
    'Accept': 'application/json'
  },
  data: {
    
  }
}, (err, res) => {
  if(err) {
    console.error(err);
  } else {
    console.log(res);
  }
});
```

#### Options
* uri - uri of the file in the android filesystem
* uploadUrl - endpoint to upload the file to (uses POST)
* fileName - name of the file
* mimeType - file type
* headers - map of headers
* data - any extra data you want sent to the server
