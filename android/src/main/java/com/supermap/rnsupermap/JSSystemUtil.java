package com.supermap.rnsupermap;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipException;

public class JSSystemUtil extends ReactContextBaseJavaModule {
    public static final String REACT_CLASS = "JSSystemUtil";
    private final String homeDirectory = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

    public JSSystemUtil(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void getHomeDirectory(Promise promise) {
        try {
            WritableMap map = Arguments.createMap();
            map.putString("homeDirectory", homeDirectory);
            promise.resolve(map);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getDirectoryContent(String path, Promise promise) {
        try {
            File flist = new File(path);
            String[] mFileList = flist.list();

            WritableArray arr = Arguments.createArray();
            for (String str : mFileList) {
                String type = "";
                if (new File(path + "/" + str).isDirectory()) {
                    type = "directory";
                } else {
                    type = "file";
                }
                WritableMap map = Arguments.createMap();
                map.putString("name", str);
                map.putString("type", type);

                arr.pushMap(map);
            }

            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void fileIsExist(String path, Promise promise) {
        try {
            Boolean isExist = false;
            File file = new File(path);

            if (file.exists()) {
                isExist = true;
            }

            WritableMap map = Arguments.createMap();
            map.putBoolean("isExist", isExist);
            promise.resolve(map);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void fileIsExistInHomeDirectory(String path, Promise promise) {
        try {
            Boolean isExist = false;
            File file = new File(homeDirectory + "/" + path);

            if (file.exists()) {
                isExist = true;
            }

            WritableMap map = Arguments.createMap();
            map.putBoolean("isExist", isExist);
            promise.resolve(map);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 创建文件目录
     *
     * @param path    - 绝对路径
     * @param promise
     */
    @ReactMethod
    public void createDirectory(String path, Promise promise) {
        try {
            boolean result = false;
            File file = new File(path);

            if (!file.exists()) {
                result = file.mkdirs();
            } else {
                result = true;
            }

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void isDirectory(String path, Promise promise) {
        try {
            File file = new File(path);
            boolean isDirectory = file.isDirectory();
            promise.resolve(isDirectory);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getPathList(String path, Promise promise) {
        try {
            File file = new File(path);

            File[] files = file.listFiles();
            WritableArray array = Arguments.createArray();
            for (int i = 0; i < files.length; i++) {
                String p = files[i].getAbsolutePath().replace(homeDirectory, "");
                String n = files[i].getName();
                boolean isDirectory = files[i].isDirectory();
                WritableMap map = Arguments.createMap();
                map.putString("path", p);
                map.putString("name", n);
                map.putBoolean("isDirectory", isDirectory);
                array.pushMap(map);
            }
            promise.resolve(array);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getPathListByFilter(String path, ReadableMap filter, Promise promise) {
        try {
            File file = new File(path);

            File[] files = file.listFiles();
            WritableArray array = Arguments.createArray();
            for (int i = 0; i < files.length; i++) {
                String p = files[i].getAbsolutePath().replace(homeDirectory, "");
                String n = files[i].getName();
                int lastDot = n.lastIndexOf(".");
                String name, type = "";
                if (lastDot > 0) {
                    name = n.substring(0, lastDot).toLowerCase();
                    type = n.substring(lastDot + 1).toLowerCase();
                } else {
                    name = n;
                }
                boolean isDirectory = files[i].isDirectory();

                if (!filter.toHashMap().containsKey("name")) {
                    String filterName = filter.getString("name").toLowerCase().trim();
                    // 判断文件名
                    if (isDirectory || filterName.equals("") || !name.contains(filterName)) {
                        continue;
                    }
                }

                boolean isExist = false;
                if (filter.toHashMap().containsKey("type")) {
                    String filterType = filter.getString("type").toLowerCase();
                    String[] types = filterType.split(",");
                    for (int j = 0; j < types.length; j++) {
                        String mType = types[j].trim();
                        // 判断文件类型
                        if (isDirectory || !isDirectory && !type.equals("") && type.contains(mType)) {
                            isExist = true;
                            break;
                        } else {
                            isExist = false;
                        }
                    }
                }
                if (!isExist) {
                    continue;
                }

                WritableMap map = Arguments.createMap();
                map.putString("path", p);
                map.putString("name", n);
                map.putBoolean("isDirectory", isDirectory);
                array.pushMap(map);
            }
            promise.resolve(array);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 拷贝文件到app目录下
     *
     * @param fileName
     * @param path
     * @param promise
     */
    @ReactMethod
    public void assetsDataToSD(String fileName, String path, Promise promise) {
        try {
            InputStream myInput;
            OutputStream myOutput = new FileOutputStream(fileName);
            myInput = getReactApplicationContext().getAssets().open("myfile.zip");
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    //    /**
//     * 文件解压
//     * @param
//     * @param
//     */
//    @ReactMethod
//    public static void UnZipFolder(String zipFile, String targetDir, Promise promise){
//        boolean isUnZiped = false;
//        java.util.zip.ZipInputStream inZip;
//        try {
//
//            inZip = new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFile));
//
//            java.util.zip.ZipEntry zipEntry;
//            String szName = "";
//
//            while ((zipEntry = inZip.getNextEntry()) != null) {
//                szName = zipEntry.getName();
//
//                if (zipEntry.isDirectory()) {
//
//                    java.io.File folder = new java.io.File(targetDir + java.io.File.separator + szName);
//                    folder.mkdirs();
//
//                } else {
//
//                    java.io.File file = new java.io.File(targetDir + java.io.File.separator + szName);
//                    file.createNewFile();
//                    // get the output stream of the file
//                    java.io.FileOutputStream out = new java.io.FileOutputStream(file);
//                    int len;
//                    byte[] buffer = new byte[1024];
//                    while ((len = inZip.read(buffer)) != -1) {
//                        out.write(buffer, 0, len);
//                        out.flush();
//                    }
//                    out.close();
//                }
//            }
//            inZip.close();
//            isUnZiped = true;
//            WritableMap map = Arguments.createMap();
//            map.putBoolean("isUnZiped", isUnZiped);
//            promise.resolve(map);
////            File file = new File(zipFile);
////            file.delete();
//        } catch (FileNotFoundException e) {
//            promise.reject(e);
//        } catch (IOException e) {
//            promise.reject(e);
//        }
//    }
    @ReactMethod
    public static void unZipFile(String archive, String decompressDir, Promise promise) throws IOException, FileNotFoundException, ZipException {
        Log.e("++++++++++++", "zipzipzipzipzipzipzipzipzipzipzipzip" );
        try {

            boolean isUnZiped = false;
            BufferedInputStream bi;
            Log.e("++++++++++++", "GBKGBKGBKGBKGBKGBKGBKGBKGBKGBKGBKGBK" );
            ZipFile zf = new ZipFile(archive, "GBK");
            Log.e("++++++++++++", "okokokokokokokokokokokokokokokokokok" );
            Enumeration e = zf.getEntries();
            while (e.hasMoreElements()) {
                ZipEntry ze2 = (ZipEntry) e.nextElement();
                String entryName = ze2.getName();
                String path = decompressDir + "/" + entryName;
                if (ze2.isDirectory()) {
                    System.out.println("正在创建解压目录 - " + entryName);
                    File decompressDirFile = new File(path);
                    if (!decompressDirFile.exists()) {
                        decompressDirFile.mkdirs();
                    }
                } else {
                    System.out.println("正在创建解压文件 - " + entryName);
                    String fileDir = path.substring(0, path.lastIndexOf("/"));
                    File fileDirFile = new File(fileDir);
                    if (!fileDirFile.exists()) {
                        fileDirFile.mkdirs();
                    }
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(decompressDir + "/" + entryName));
                    bi = new BufferedInputStream(zf.getInputStream(ze2));
                    byte[] readContent = new byte[1024];
                    int readCount = bi.read(readContent);
                    while (readCount != -1) {
                        bos.write(readContent, 0, readCount);
                        readCount = bi.read(readContent);
                    }
                    bos.close();
                }
            }
            zf.close();
            isUnZiped = true;
//            WritableMap map = Arguments.createMap();
//            map.putBoolean("isUnZiped", isUnZiped);
            promise.resolve(isUnZiped);
        } catch (Exception e) {
            promise.reject(e);
        }

    }

    @ReactMethod
    public static void deleteFile(String zippath, Promise promise) {
        try {
            File file = new File(zippath);
            boolean result = false;
            if (file.exists()) {
                result = file.delete();
            }
            promise.resolve(result);
        }catch (Exception e){
            promise.reject(e);
        }

    }

}

