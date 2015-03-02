/*
 * Copyright (C) 2009 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 */

#include <string.h>
#include <jni.h>
#include <sys/types.h>
#include <assert.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include "zip.h"

#define  LOG_TAG    "ReadAssets"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


/*******************************************************************************
* Function Name  : java_com_fontlose_ReadAssets_readFromAssets
* Description    : 定义：public native void  readFromAssets(AssetManager ass,String filename);
* Input          : AssetManager对象 filename:资源名
* Output         : None
* Return         : None
*******************************************************************************/  
void  Java_com_fontlose_ReadAssets_readFromAssets(JNIEnv* env,jclass tis,jobject assetManager,jstring filename)
{
   LOGI("ReadAssets");  
   AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
   if(mgr==NULL)
   {
      LOGI(" %s","AAssetManager==NULL"); 
      return ; 
   }
  
    /*获取文件名并打开*/
   jboolean iscopy;
   const char *mfile = (*env)->GetStringUTFChars(env, filename, &iscopy);
   AAsset* asset = AAssetManager_open(mgr, mfile,AASSET_MODE_UNKNOWN);
   (*env)->ReleaseStringUTFChars(env, filename, mfile);
   if(asset==NULL)
   {
      LOGI(" %s","asset==NULL"); 
      return ; 
   }
   /*获取文件大小*/
   off_t bufferSize = AAsset_getLength(asset);
   LOGI("file size         : %d\n",bufferSize);
   char *buffer=(char *)malloc(bufferSize+1);
   buffer[bufferSize]=0;
   int numBytesRead = AAsset_read(asset, buffer, bufferSize);
   LOGI(": %s",buffer);
   free(buffer);
    /*关闭文件*/
   AAsset_close(asset);
}

/*******************************************************************************
* Function Name  : java_com_fontlose_ReadAssets_readFromAssetsLibzip
* Description    : 定义：public native void  readFromAssetsLibzip(String apkpath,String filename);
* Input          : apkpath路径 filename 资源名
* Output         : None
* Return         : None
*******************************************************************************/  
jstring  Java_com_fontlose_ReadAssets_readFromAssetsLibzip(JNIEnv* env,jclass tis,jstring assetpath,jstring filename)
{
   LOGI("ReadAssets");  
  int i=0;
   jboolean iscopy;
   jstring resultStr;
   const char *mpath = (*env)->GetStringUTFChars(env, assetpath, &iscopy);
   struct zip* apkArchive=zip_open(mpath, 0, NULL);;
   (*env)->ReleaseStringUTFChars(env, filename, mpath);

   struct zip_stat fstat;
   zip_stat_init(&fstat);

   int numFiles = zip_get_num_files(apkArchive);
   LOGI("File numFiles %i \n",numFiles);
   for (i=0; i<numFiles; i++) {
     const char* name = zip_get_name(apkArchive, i, 0);
     
     if (name == NULL) {
      LOGE("Error reading zip file name at index %i : %s", zip_strerror(apkArchive));
      return 0;
    }
    
    zip_stat(apkArchive,name,0,&fstat);
    LOGI("File %i:%s Size1: %d Size2: %d", i,fstat.name,fstat.size ,fstat.comp_size)  ;
   }

   const char *fname = (*env)->GetStringUTFChars(env, filename, &iscopy);
   struct zip_file* file = zip_fopen(apkArchive, fname, 0);
  
   if (!file) {
     LOGE("Error opening %s from APK", fname);
     return 0;
    }

   zip_stat(apkArchive,fname,0,&fstat);
   (*env)->ReleaseStringUTFChars(env, filename, fname);
   char *buffer=(char *)malloc(fstat.size+1);
   buffer[fstat.size]=0;
   int numBytesRead =  zip_fread(file, buffer,fstat.size);;
   LOGI(": %s\n",buffer);
   resultStr = env->NewStringUTF(buffer);
   free(buffer);
   zip_fclose(file);
   zip_close(apkArchive);
   return resultStr;
}
