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

let files = [
      {
        name: 'testName',
        fileName: 'testFileName.jpg',
        uri: filePath, // filePath only support uri
        mimeType: 'image/jpg',
      },
      {
        name: 'testName2',
        fileName: 'testFileName2.jpg',
        uri: filePath, // filePath only support uri
        mimeType: 'image/jpg',
      },
    ]
FileTransfer.upload({
        url: 'uploadURL',
        files: files,
        data: {
          context: 'wow',
          isposis: '222', // only support string
        }
      }, (err, res) => {
        if(err) {
          console.error(err);
        } else {
          console.log(res);
        }
      });
```
This module do not support header.
header is always ("Accept", "application/json")
#### Options
* url - uri of the file in the android filesystem
* uploadUrl - endpoint to upload the file to (uses POST)
* name - fieldName of file
* fileName - name of the file
* mimeType - file type
* data - any extra data you want sent to the server // only support string. not int, boolean. 
