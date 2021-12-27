package com.zy.environment.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/*
 * 文件存储
 * */
public class FileStorage {

    /*
    * 创建文件夹
    * */
    public static void creatDir(String path){
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
    }


    /*
     * 删除文件
     * */
    public static boolean delete(String delFile) {
        File file = new File(delFile);
        if (!file.exists()) {//文件不存在
            return true;
        } else {
            if (file.isFile())
                return deleteSingleFile(delFile);
            else
                return deleteDirectory(delFile);
        }
    }

    /** 删除单个文件
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /** 删除目录及目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private static boolean deleteDirectory(String filePath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        File dirFile = new File(filePath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            return true;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : files) {
        // 删除子文件
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (file.isDirectory()) {
                flag = deleteDirectory(file.getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /*
    *文件保存方法
    Environment.getExternalStorageDirectory().getAbsolutePath() 获取系统根目录
    * path 保存文件的路径
    * fileName 文件名
    * content 内容
    * */
    public static void saveToFile(String path, String fileName, String content){
        //生成文件夹之后，再生成文件，不然会出错
        String strFilePath = path + "/" + fileName;
        File file = new File(strFilePath);
        creatDir(path);
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 获取本地文件内容
     * path 保存文件的路径
     * fileName 文件名
     * */
    public static String getFileText(String path, String fileName) {
        String strFilePath = path + "/" + fileName;
        File file = new File(strFilePath);
        String content = "";
        if(!file.exists()){
            return content;
        }
        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)
            InputStream instream = null;
            try {
                instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line = "";
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content += line +"\n";
                    }
                    instream.close();//关闭输入流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }


    /*
     * 获取本地文件内容
     * inputStream 文件流
     * */
    public static String getFileText(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line,content = "";
        //分行读取
        while (( line = bufferedReader.readLine()) != null) {
            content += line +"\n";
        }
        inputStream.close();
        Log.e("FileStorage","content==="+content);
        return content;
    }


    /*
    * 将assets文件拷贝至sd卡
    * assetDir  assets文件路径 例：apiclient_key.pem
    * dirpath  需要拷贝的sd卡路径 例：  Environment.getExternalStorageDirectory().getAbsolutePath()+"/sesxh/PrivateKey"
    *
    * */
    private static void copyassToSD(Context context, String assetDir, String dirpath) throws IOException {
        //读取安卓assets绝对文件路径
        InputStream inputStream = context.getResources().getAssets().open(assetDir);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line, content = "";
        //分行读取
        while ((line = bufferedReader.readLine()) != null) {
            content += line + "\n";
        }
        inputStream.close();

        File file = new File(dirpath+"/"+assetDir);
        File dir = new File(dirpath);
        if (!dir.exists()) {
            dir.mkdirs();
            file.createNewFile();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(content.getBytes());
        outputStream.close();
    }



    public static void releaseAssets(Context context, String assetsDir, String releaseDir) {
//		Log.d(TAG, "context: " + context + ", " + assetsDir);
        if (TextUtils.isEmpty(releaseDir)) {
            return;
        } else if (releaseDir.endsWith("/")) {
            releaseDir = releaseDir.substring(0, releaseDir.length() - 1);
        }

        if (TextUtils.isEmpty(assetsDir) || assetsDir.equals("/")) {
            assetsDir = "";
        } else if (assetsDir.endsWith("/")) {
            assetsDir = assetsDir.substring(0, assetsDir.length() - 1);
        }

        AssetManager assets = context.getAssets();
        try {
            String[] fileNames = assets.list(assetsDir);//只能获取到文件(夹)名,所以还得判断是文件夹还是文件
            if (fileNames.length > 0) {// is dir
                for (String name : fileNames) {
                    if (!TextUtils.isEmpty(assetsDir)) {
                        name = assetsDir + File.separator + name;//补全assets资源路径
                    }
//                    Log.i(, brian name= + name);
                    String[] childNames = assets.list(name);//判断是文件还是文件夹
                    if (!TextUtils.isEmpty(name) && childNames.length > 0) {
                        checkFolderExists(releaseDir + File.separator + name);
                        releaseAssets(context, name, releaseDir);//递归, 因为资源都是带着全路径,
                        //所以不需要在递归是设置目标文件夹的路径
                    } else {
                        InputStream is = assets.open(name);
//                        FileUtil.writeFile(releaseDir + File.separator + name, is);
                        writeFile(releaseDir + File.separator + name, is);
                    }
                }
            } else {// is file
                InputStream is = assets.open(assetsDir);
                // 写入文件前, 需要提前级联创建好路径, 下面有代码贴出
//                FileUtil.writeFile(releaseDir + File.separator + assetsDir, is);
                writeFile(releaseDir + File.separator + assetsDir, is);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean writeFile(String fileName, InputStream in) throws IOException
    {
        boolean bRet = true;
        try {
            OutputStream os = new FileOutputStream(fileName);
            byte[] buffer = new byte[4112];
            int read;
            while((read = in.read(buffer)) != -1)
            {
                os.write(buffer, 0, read);
            }
            in.close();
            in = null;
            os.flush();
            os.close();
            os = null;
//			Log.v(TAG, "copyed file: " + fileName);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            bRet = false;
        }
        return bRet;
    }

    private static void checkFolderExists(String path)
    {
        File file = new File(path);
        if((file.exists() && !file.isDirectory()) || !file.exists())
        {
            file.mkdirs();
        }
    }


}
