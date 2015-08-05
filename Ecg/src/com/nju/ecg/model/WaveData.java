package com.nju.ecg.model;
/**
 * 心率数据Model
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-13]
 */
public class WaveData
{
    /** 唯一标识*/
    private int _id;
    /** 保存的数据文件完整路径*/
    private String filePath;
    /** 导联系统*/
    private int leadSystem;
    /** 心率参数*/
    private String heartPara;
    /** 异常信息*/
    private String abnormalValue;
    /** 诊断结果*/
    private String diagnoseResult;
    /** 开始时间*/
    private long startTime; 
    /** 采集时间(实际就是startTime格式化之后的)*/
    private String collectFormatedTime;
    /** 其他信息*/
    private String desc;
    /** 结束时间*/
    private long endTime;
    /** 是否是用户手动增加的*/
    private boolean isCustom;
    
    /**
     * @return the _id
     */
    public int get_id()
    {
        return _id;
    }
    /**
     * @param _id the _id to set
     */
    public void set_id(int _id)
    {
        this._id = _id;
    }
    /**
     * @return the leadSystem
     */
    public int getLeadSystem()
    {
        return leadSystem;
    }
    /**
     * @param leadSystem the leadSystem to set
     */
    public void setLeadSystem(int leadSystem)
    {
        this.leadSystem = leadSystem;
    }
    /**
     * @return the heartPara
     */
    public String getHeartPara()
    {
        return heartPara;
    }
    /**
     * @param heartPara the heartPara to set
     */
    public void setHeartPara(String heartPara)
    {
        this.heartPara = heartPara;
    }
    /**
     * @return the abnormalValue
     */
    public String getAbnormalValue()
    {
        return abnormalValue;
    }
    /**
     * @param abnormalValue the abnormalValue to set
     */
    public void setAbnormalValue(String abnormalValue)
    {
        this.abnormalValue = abnormalValue;
    }
    /**
     * @return the diagnoseResult
     */
    public String getDiagnoseResult()
    {
        return diagnoseResult;
    }
    /**
     * @param diagnoseResult the diagnoseResult to set
     */
    public void setDiagnoseResult(String diagnoseResult)
    {
        this.diagnoseResult = diagnoseResult;
    }
    /**
     * @return the desc
     */
    public String getDesc()
    {
        return desc;
    }
    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc)
    {
        this.desc = desc;
    }
    /**
     * @return the filePath
     */
    public String getFilePath()
    {
        return filePath;
    }
    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }
    /**
     * @return the startTime
     */
    public long getStartTime()
    {
        return startTime;
    }
    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }
    /**
     * @return the endTime
     */
    public long getEndTime()
    {
        return endTime;
    }
    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }
    /**
     * @return the collectFormatedTime
     */
    public String getCollectFormatedTime()
    {
        return collectFormatedTime;
    }
    /**
     * @param collectFormatedTime the collectFormatedTime to set
     */
    public void setCollectFormatedTime(String collectFormatedTime)
    {
        this.collectFormatedTime = collectFormatedTime;
    }
    /**
     * @return the isCustom
     */
    public boolean isCustom()
    {
        return isCustom;
    }
    /**
     * @param isCustom the isCustom to set
     */
    public void setCustom(boolean isCustom)
    {
        this.isCustom = isCustom;
    }
    
}
