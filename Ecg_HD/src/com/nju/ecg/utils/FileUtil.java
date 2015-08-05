package com.nju.ecg.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * 文件操作工具类
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-19]
 */
public class FileUtil
{
    private static final String TAG = "FileUtil";
    /**
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    public static boolean isExist(String filePath)
    {
        return new File(filePath).exists();
    }
    
    /**
     * 
     * 解析txt文件内容
     * 
     * @param is 文件流
     * @return txt文件内容
     */
    public static String getTxtFileContent(InputStream is)
    {
        String str = "";
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        DataInputStream fis = null;
        DataInputStream dis = null;

        try
        {
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            fis = new DataInputStream(is);
            dis = new DataInputStream(fis);

            byte[] buffer = new byte[1024];
            int length = dis.read(buffer);
            while (length != -1)
            {
                dos.write(buffer,
                    0,
                    length);
                length = dis.read(buffer);
            }
            byte[] data = baos.toByteArray();

            String charset = getCharset(is);
            str = new String(data,
                "UTF-8");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (baos != null)
                {
                    baos.close();
                }

                if (dos != null)
                {
                    dos.close();
                }

                if (fis != null)
                {
                    fis.close();
                }

                if (dis != null)
                {
                    dis.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return str;
    }
    
    /**
     * 
     * 解析txt文件内容
     * 
     * @param path 文件路径
     * @return txt文件内容
     */
    public static String getTxtFileContent(String path)
    {
        String txt = "";
        FileInputStream fis = null;
        File file = new File(path);
        try
        {
            fis = new FileInputStream(file);
            txt = getTxtFileContent(fis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (fis != null)
                {
                    fis.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return txt;
    }
    
    /**
     * 
     * 获取文件的unicode编码
     * 
     * @param is 文件流
     * @return 文件的unicode编码
     */
    private static String getCharset(InputStream is)
    {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        BufferedInputStream bis = null;
        try
        {
            bis = new BufferedInputStream(is);
            bis.mark(0);
            int read = bis.read(first3Bytes,
                0,
                3);
            if (read == -1)
            {
                return charset;
            }

            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE)
            {
                charset = "UTF-16LE";
            }
            else if (first3Bytes[0] == (byte) 0xFE
                && first3Bytes[1] == (byte) 0xFF)
            {
                charset = "UTF-16BE";
            }
            else if (first3Bytes[0] == (byte) 0xEF
                && first3Bytes[1] == (byte) 0xBB
                && first3Bytes[2] == (byte) 0xBF)
            {
                charset = "UTF-8";
            }
            /*******************************************************************
             * bis.reset(); if (!checked) { int loc = 0; while ((read =
             * bis.read()) != -1) { loc++; if (read >= 0xF0) { break; } if (0x80
             * <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK { break; } if (0xC0
             * <= read && read <= 0xDF) { read = bis.read(); if (0x80 <= read &&
             * read <= 0xBF)// 双字节 (0xC0 - 0xDF) { // (0x80 - 0xBF),也可能在GB编码内
             * continue; } else { break; } } else if (0xE0 <= read && read <=
             * 0xEF) { // 也有可能出错，但是几率较小 read = bis.read(); if (0x80 <= read &&
             * read <= 0xBF) { read = bis.read(); if (0x80 <= read && read <=
             * 0xBF) { charset = "UTF-8"; break; } else { break; } } else {
             * break; } } } System.out.println(loc + " " +
             * Integer.toHexString(read)); }
             ******************************************************************/
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        LogUtil.d(TAG,
            "getCharset>>>>>>>>>>charset==" + charset);

        return charset;
    }
    
}
