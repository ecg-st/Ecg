package com.nju.ecg.utils;
/**
 * 
 * [一句话功能简述]<BR>
 * [功能详细描述]
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-14]
 */
public class StringUtil
{
    /**
     * 字符为空判断
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str)
    {
        if (null == str)
        {
            return true;
        }
        else if (str.trim().equals(""))
        {
            return true;
        }
        return false;
    }
}
