package com.nju.ecg.utils;

/*
 * @(#)GlobalExceptionHandler.java 11-7-17 下午3:15 CopyRight 2011. All rights
 * reserved
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Properties;
import java.util.StringTokenizer;

import android.content.Context;
import android.os.Build;

import com.nju.ecg.service.EcgApp;

/**
 * 全局异常处理
 * 
 * @author zhuhf
 * @version 1.0
 */
public final class GlobalExceptionHandler implements
    Thread.UncaughtExceptionHandler
{
    private static final boolean SEND_MAIL = true;

    private static final String EMAIL_FROM_ONE = "ecg001@yahoo.cn";
    private static final String EMAIL_FROM_TWO = "ecg002@yahoo.cn";
    private static final String EMAIL_FROM_THREE = "ecg003@yahoo.cn";
    private static final String EMAIL_FROM_FOUR = "ecg004@yahoo.cn";
    private static final String EMAIL_FROM_FIVE = "ecg005@yahoo.cn";
    private static final String SMTP_YAHOO_COM_CN = "smtp.mail.yahoo.com.cn";
    private static final String EMAIL_PASSWORD = "0123456789";
    private static final String LINE_SEPARATOR = System
        .getProperty("line.separator");
    private static final String TAG = "GlobalExceptionHandler";
    private static final String[] EMAIL_TO_LIST = new String[] {"514126671@qq.com"};

    private Thread.UncaughtExceptionHandler defaultHandler;
    private boolean caughtException = false;
    private String[] emailAddrs = {EMAIL_FROM_ONE, EMAIL_FROM_TWO,
        EMAIL_FROM_THREE, EMAIL_FROM_FOUR, EMAIL_FROM_FIVE };
    private String clientInfo;

    /**
     * 全局错误处理构造函数
     */
    public GlobalExceptionHandler()
    {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 收集客户端信息
        this.clientInfo = collectClientInfo();
    }
    
    /**
     * 捕获到异常
     * 
     * @param thread 异常线程
     * @param throwable 异常信息
     */
    @Override
    public void uncaughtException(final Thread thread, final Throwable throwable)
    {
        LogUtil.e(TAG,
            String.format("Caught Global Exception, threadName:%s, threadId:%s",thread.getName(), thread.getId()) );
        if (caughtException)
        {
            defaultHandler.uncaughtException(thread,
                throwable);
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        throwable.printStackTrace(ps);
        final String errorMsg = new String(baos.toByteArray());
        LogUtil.e(TAG,
            "uncaughtException >> " + errorMsg);
        final String mailContent = errorMsg + LINE_SEPARATOR + LINE_SEPARATOR
            + clientInfo;

        if (SEND_MAIL)
        {
            // 跑线程, 将报错信息发送到指定的邮箱
            Thread sendMailThread = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String subject = "Ecg Crash Report"
                            + " ("
                            + "AppVersion: "
                            + EcgApp.getInstance()
                                .getContext()
                                    .getPackageManager()
                                    .getPackageInfo(EcgApp.getInstance()
                                        .getContext()
                                            .getPackageName(),
                                        Context.MODE_PRIVATE).versionName
                            + ")";
                        Properties props = new Properties();
                        props.put("mail.smtp.host",
                            SMTP_YAHOO_COM_CN);
                        props.put("mail.smtp.auth",
                            "true");
                        MailUtil.MailInfo mailInfo = new MailUtil.MailInfo();
                        mailInfo.setFrom(emailAddrs[(int) (Math.floor(Math
                            .random() * emailAddrs.length))]);
                        mailInfo.setPassword(EMAIL_PASSWORD);
                        mailInfo.setSmtpHost(SMTP_YAHOO_COM_CN);
                        mailInfo.setNeedAuth(true);
                        mailInfo.setToList(EMAIL_TO_LIST);
                        mailInfo.setSubject(subject);
                        mailInfo.setContent(mailContent);
                        try
                        {
                            MailUtil.sendMail(mailInfo);
                            LogUtil.d(TAG,
                                "Send mail successful");
                        }
                        catch (Exception e)
                        {
                            LogUtil.w(TAG,
                                "Send mail failed");
                        }
                    }
                    catch (Exception e)
                    {
                        LogUtil.e(TAG,
                            "Get app info failed");
                    }
                    finally 
                    {
                        if (!handleException() && defaultHandler != null)
                        {
                            defaultHandler.uncaughtException(thread, throwable);
                        }
                    }
                }
            };
            sendMailThread.start();
        }
    }
    
    /** 
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 
     *  
     * @return true:如果处理了该异常信息;否则返回false. 
     */  
    private boolean handleException() 
    {  
        //使用Toast来显示异常信息  
        try 
        {
            LogUtil.d(TAG, "handleException()");
            android.os.Process.killProcess(android.os.Process.myPid());  
            System.exit(1);
            return true;
        } 
        catch (Exception e) 
        {
            LogUtil.e(TAG, e);
            return false;
        }
    }  

    @SuppressWarnings("unused")
    private void exit()
    {
        // Log.debug(TAG, "Exit On Global Exception");
        String packageName = EcgApp.getInstance().getContext().getPackageName();
        String processId = "";
        try
        {
            Runtime r = Runtime.getRuntime();
            Process p = r.exec("ps");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String inline;
            while ((inline = br.readLine()) != null)
            {
                if (inline.endsWith(packageName))
                {
                    break;
                }
            }
            br.close();
            StringTokenizer processInfoTokenizer = new StringTokenizer(inline);
            int count = 0;
            while (processInfoTokenizer.hasMoreTokens())
            {
                count++;
                processId = processInfoTokenizer.nextToken();
                if (count == 2)
                {
                    break;
                }
            }
            r.exec("kill -15 " + processId);
        }
        catch (IOException ex)
        {
            LogUtil.w(TAG,
                "Kill process failed");
        }
    }

    private String collectClientInfo()
    {
        StringBuilder systemInfo = new StringBuilder();
        systemInfo.append("CLIENT-INFO");
        systemInfo.append(LINE_SEPARATOR);

        systemInfo.append("Id: ");
        systemInfo.append(Build.ID);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Display: ");
        systemInfo.append(Build.DISPLAY);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Product: ");
        systemInfo.append(Build.PRODUCT);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Device: ");
        systemInfo.append(Build.DEVICE);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Board: ");
        systemInfo.append(Build.BOARD);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("CpuAbility: ");
        systemInfo.append(Build.CPU_ABI);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Manufacturer: ");
        systemInfo.append(Build.MANUFACTURER);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Brand: ");
        systemInfo.append(Build.BRAND);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Model: ");
        systemInfo.append(Build.MODEL);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Type: ");
        systemInfo.append(Build.TYPE);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Tags: ");
        systemInfo.append(Build.TAGS);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("FingerPrint: ");
        systemInfo.append(Build.FINGERPRINT);
        systemInfo.append(LINE_SEPARATOR);

        systemInfo.append("Version.Incremental: ");
        systemInfo.append(Build.VERSION.INCREMENTAL);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Version.Release: ");
        systemInfo.append(Build.VERSION.RELEASE);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("SDK: ");
        systemInfo.append(Build.VERSION.SDK);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("SDKInt: ");
        systemInfo.append(Build.VERSION.SDK_INT);
        systemInfo.append(LINE_SEPARATOR);
        systemInfo.append("Version.CodeName: ");
        systemInfo.append(Build.VERSION.CODENAME);
        systemInfo.append(LINE_SEPARATOR);
        String clientInfomation = systemInfo.toString();
        systemInfo.delete(0,
            systemInfo.length());
        return clientInfomation;
    }

}
