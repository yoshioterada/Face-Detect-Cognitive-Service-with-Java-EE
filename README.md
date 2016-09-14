Face API and Emotional API of Cognitive Service with Java EE
====
## How to try this code.

If you woulld like to try to this sample, please modify following?

[PhotoUploader.java](https://github.com/yoshioterada/Face-Detect-Cognitive-Service-with-Java-EE/blob/master/src/main/java/com/yoshio3/backingBean/PhotoUploader.java)

* __EMOTIONAL_API_SUBSCRIPTION__ 
: Please get from [Emotion API](https://www.microsoft.com/cognitive-services/en-us/emotion-api)
* __FACE_API_SUBSCRIPTION__
: Please get from [Face API](https://www.microsoft.com/cognitive-services/en-us/face-api)

You need to get the Subscription ID both Emotion and Face API from the following site. After you got it, please replace the string?

* __AZURE_BLOG_UPLOAD_URL__
You need to create Blob Storage on Microsoft Azure. All photos you took will place into the Blob strage. In this parameter, please specify the URL of blob storage?
You can get the URL from Azure portal screen. The actual URL look like : https://yoshiofileup.blob.core.windows.net
![Azure Portal](https://c1.staticflickr.com/9/8769/29045443503_ea033d6cbb.jpg)
![Azure Portal](https://c1.staticflickr.com/9/8555/29559009892_858aaae47d.jpg)

[StorageService.java](https://github.com/yoshioterada/Face-Detect-Cognitive-Service-with-Java-EE/blob/master/src/main/java/com/yoshio3/services/StorageService.java)
* __AccountName__
* __AccountKey__

In order to store the data on Azure Storage, you need to get the AccessKey from Azure portal. After you got them, please replace the "AccountName" and "AccessKey"?
![Azure Portal](https://c1.staticflickr.com/9/8198/29634584106_92aa5fcda2.jpg)
![Azure Portal](https://c1.staticflickr.com/9/8560/29634584276_cc8f967778.jpg)

