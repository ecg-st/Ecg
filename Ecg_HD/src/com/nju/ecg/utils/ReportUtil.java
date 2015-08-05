package com.nju.ecg.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.PorterDuff.Mode;

import com.nju.ecg.framework.db.WaveDataDBHelper;
import com.nju.ecg.model.WaveData;
import com.nju.ecg.wave.EcgDrawView;
import com.nju.ecg.wave.WaveScreen;

/**
 * 检测报告生成类
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-19]
 */
public class ReportUtil
{
    private static final String TAG = "ReportUtil";
    private static ReportUtil reportUtil;
    /** 绘制散点图宽度*/
    private static final int DOT_GRAPH_WRIDTH = 800;
    /** 绘制散点图高度*/
    private static final int DOT_GRAPH_HEIGHT = 500;
    private ReportUtil()
    {
    }
    
    public static ReportUtil getInstance()
    {
        if (reportUtil == null)
        {
            reportUtil = new ReportUtil();
        }
        return reportUtil;
    }
    
    /**
     * 组装生成检测报告
     */
    public void packageReport(String filePath)
    {
        WaveData data = WaveDataDBHelper.getInstance().query(filePath);
        
        // for test
//        data = new WaveData();
//        data.setFilePath(filePath);
//        data.setDesc("饭前");
//        data.setHeartPara("100");
//        data.setStartTime(System.currentTimeMillis());
//        data.setEndTime(System.currentTimeMillis() + 60 * 1000);
//        WaveScreen.prValueList.add(10);
//        WaveScreen.qrsValueList.add(10);
//        WaveScreen.qtValueList.add(10);
//        WaveScreen.stValueList.add(10.0);
//        WaveScreen.abnormalParameterMap.put("心动过缓", 10);
//        WaveScreen.abnormalParameterMap.put("心动过慢", 10);
//        WaveScreen.abnormalParameterMap.put("窦性停搏", 10);
//        WaveScreen.abnormalParameterMap.put("室性早搏和室速", 10);
//        WaveScreen.abnormalParameterMap.put("间外性期外收缩", 10);
        // for test
        
        if (data != null)
        {
            String dataPath = data.getFilePath();
            String dataDir = dataPath.substring(0,
                dataPath.lastIndexOf("/"));
            final String dataName = dataPath.substring(dataPath
                .lastIndexOf("/") + 1,
                dataPath.lastIndexOf(EcgConst.FILE_END_NAME));
            File reportDir = new File(dataDir + "/" + dataName + "_report");
            BufferedWriter bw = null;
            try
            {
                if (!reportDir.exists())
                {
                    reportDir.mkdirs();
                }
                File reportFile = new File(reportDir.getAbsolutePath(), dataName + ".html");
                if (!reportFile.exists())
                {
                    reportFile.createNewFile();
                }
                bw = new BufferedWriter(new FileWriter(reportFile));
                // 检测报告
                String report = new ReportPackage(data).doPackage();
                bw.write(report);
                bw.flush();
            }
            catch (Exception e)
            {
                LogUtil.e(TAG, e);
            }
        }
        else
        {
            LogUtil.d(TAG, "数据库无此记录！filePath << " + filePath + ">>");
        }
    }
    
    /**
     * 绘制心率视图
     * @return
     */
    public Bitmap drawWaveScreen(int[] ch1Data, int[] ch2Data, int updateIndex, boolean switchScreen, int infoHeight)
    {
        Bitmap image = Bitmap.createBitmap(EcgConst.DISPLAY_WIDTH, EcgConst.DISPLAY_HEIGH - infoHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(image);
        new WaveDrawer(ch1Data, ch2Data, updateIndex, switchScreen).onDraw(canvas);
        return image;
    }
    
    /**
     * 绘制散点图
     * @return
     */
    public Bitmap drawDotGraph(String filePath, long collectingTime)
    {
        Bitmap image = Bitmap.createBitmap(DOT_GRAPH_WRIDTH, DOT_GRAPH_HEIGHT, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(image);
        new DotGraphDrawer(filePath, collectingTime).onDraw(canvas);
        return image;
    }
    
    /**
     * 报告生成
     * @author zhuhf
     * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-24]
     */
    private class ReportPackage
    {
        private WaveData data;
        
        public ReportPackage(WaveData data)
        {
            this.data = data;
        }
        
        /**
         * 执行报告组装
         */
        public String doPackage()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
            sb.append("<title>心律检测报告</title>\n");
            sb.append("<style type=\"text/css\">\n");
            sb.append("table td {\n");
            sb.append(" font-size: 12px;\n");
            sb.append(" font-weight: bold;\n");
            sb.append("}\n\n");
            sb.append("body {\n");
            sb.append(" margin: 0;\n");
            sb.append(" padding: 0;\n");
            sb.append(" text-align: center;\n");
            sb.append("}\n");
            sb.append("</style>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");
            sb.append(packageInfoArea());
            sb.append(packageWaveArea());
            sb.append(packageParaArea());
            sb.append(packageDotArea());
            sb.append(packageResultArea());
            sb.append("</body>\n");
            sb.append("</html>\n");
            return sb.toString();
        }
        
        public String packageInfoArea()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(" <div style=\"font-size: 16px;margin-top: 20px;font-weight: bold;\">心律检测报告</div>\n");
            sb.append(" <div align=\"center\" style=\"margin-top: 10px\">\n");
            sb.append("     <table width=\"800px\" cellpadding=\"0\" style=\"border-collapse: collapse;\">\n");
            sb.append("         <tr>\n");
            sb.append("             <td align=\"left\" width=\"200px\" height=\"30px\" style=\"border: 1px solid black;padding-left: 3px\">姓名：</td>\n");
            sb.append("             <td align=\"left\" width=\"200px\" style=\"border: 1px solid black;padding-left: 3px\">性别：</td>\n");
            sb.append("             <td align=\"left\" width=\"200px\" style=\"border: 1px solid black;padding-left: 3px\">年龄：</td>\n");
            sb.append("             <td align=\"left\" width=\"200px\" style=\"border: 1px solid black;padding-left: 3px\">日期：");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            final String time = calendar.get(Calendar.YEAR) + "-"
                + (calendar.get(Calendar.MONTH) + 1) + "-"
                + calendar.get(Calendar.DAY_OF_MONTH);
            sb.append(time);
            sb.append("</td>\n");
            sb.append("         </tr>\n");
            sb.append("         <tr>\n");
            sb.append("             <td colspan=\"4\" align=\"left\">备注：");
            sb.append(StringUtil.isNullOrEmpty(data.getDesc()) ? "" : data.getDesc());
            sb.append("</td>\n");
            sb.append("         </tr>\n");
            sb.append("     </table>\n");
            sb.append(" </div>\n");
            return sb.toString();
        }
        
        public String packageWaveArea()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(" <div style=\"font-size: 12px;margin-top: 20px;font-weight: bold;\">心电波形");
            sb.append("</div>\n");
            sb.append(" <div style=\"margin-top: 10px\">\n");
            
            String dataPath = data.getFilePath();
            String dataDir = dataPath.substring(0,
                dataPath.lastIndexOf("/"));
            final String dataName = dataPath.substring(dataPath
                .lastIndexOf("/") + 1,
                dataPath.lastIndexOf(EcgConst.FILE_END_NAME));
            File reportDir = new File(dataDir + "/" + dataName + "_report");
            File[] waveFiles = reportDir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    return pathname.getAbsolutePath().endsWith("_wave" + ".png");
                }
            });
            if (waveFiles != null)
            {
                for (File file : waveFiles)
                {
                    sb.append("     <img alt=\"\" src=\"");
                    sb.append(file.getName());
                    sb.append("\" width=\"800px\">\n");
                }
            }
            sb.append(" </div>\n");
            return sb.toString();
        }
        
        public String packageParaArea()
        {
            DecimalFormat df1 = new DecimalFormat("0.00");
            StringBuilder sb = new StringBuilder();
            sb.append(" <div style=\"font-size: 12px;margin-top: 20px;font-weight: bold;\">参数(均值)</div>\n");
            sb.append(" <div align=\"center\" style=\"margin-top: 10px\">\n");
            sb.append("     <table width=\"800px\" cellpadding=\"0\" style=\"border-collapse: collapse;\">\n");
            sb.append("         <tr>\n");
            sb.append("             <td align=\"left\" width=\"200px\" height=\"30px\" style=\"border: 1px solid black;padding-left: 3px\">平均心率(50<正常值<120):  ");
            sb.append(StringUtil.isNullOrEmpty(data.getHeartPara()) ? "未知" : data.getHeartPara());
            sb.append("</td>\n");
            sb.append("             <td align=\"left\" width=\"200px\" style=\"border: 1px solid black;padding-left: 3px\">P波(正常值<110ms):  </td>\n");
            sb.append("             <td align=\"left\" width=\"200px\" style=\"border: 1px solid black;padding-left: 3px\">QRS(正常值<120ms):  ");
            if (WaveScreen.qrsValueList.size() > 0)
            {
                sb.append(df1.format((double)sumInt(WaveScreen.qrsValueList)/WaveScreen.qrsValueList.size())).append("ms");
            }
            else
            {
                sb.append("未知");
            }
            sb.append("</td>\n");
            sb.append("         </tr>\n");
            sb.append("         <tr>\n");
            sb.append("             <td align=\"left\" width=\"200px\" height=\"30px\" style=\"border: 1px solid black;padding-left: 3px\">T波状态(正常值>0.1R):  </td>\n");
            sb.append("             <td align=\"left\" width=\"200px\" style=\"border: 1px solid black;padding-left: 3px\">PR(正常值120-200ms):  ");
            if (WaveScreen.prValueList.size() > 0)
            {
                sb.append(df1.format((double)sumInt(WaveScreen.prValueList)/WaveScreen.prValueList.size())).append("ms");
            }
            else
            {
                sb.append("未知");
            }
            sb.append("</td>\n");
            sb.append("             <td align=\"left\" width=\"200px\" style=\"border: 1px solid black;padding-left: 3px\">R波:  </td>\n");
            sb.append("         </tr>\n");
            sb.append("         <tr>\n");
            sb.append("             <td align=\"left\" width=\"200px\" height=\"30px\" style=\"border: 1px solid black;padding-left: 3px\">ST段(正常值<0.1mV):  ");
            if (WaveScreen.stValueList.size() > 0)
            {
                sb.append(df1.format((double)sumDouble(WaveScreen.stValueList)/WaveScreen.stValueList.size())).append("mV");
            }
            else
            {
                sb.append("未知");
            }
            sb.append("</td>\n");
            sb.append("             <td align=\"left\" width=\"200px\" style=\"border: 1px solid black;padding-left: 3px\">QT(参考值360-440ms):  ");
            if (WaveScreen.qtValueList.size() > 0)
            {
                sb.append(df1.format((double)sumInt(WaveScreen.qtValueList)/WaveScreen.qtValueList.size())).append("ms");
            }
            else
            {
                sb.append("未知");
            }
            sb.append("</td>\n");
            sb.append("             <td align=\"left\" width=\"200px\" style=\"border: 1px solid black;padding-left: 3px\">Q波(正常值<40ms,且<0.25R):  </td>\n");
            sb.append("         </tr>\n");
            sb.append("     </table>\n");
            sb.append(" </div>\n");
            return sb.toString();
        }
        
        public String packageDotArea()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(" <div style=\"font-size: 12px;margin-top: 50px;font-weight: bold;\">散点图</div>\n");
            sb.append(" <div align=\"center\" style=\"margin-top: 10px\">\n");
            String dataPath = data.getFilePath();
            String dataDir = dataPath.substring(0,
                dataPath.lastIndexOf("/"));
            final String dataName = dataPath.substring(dataPath
                .lastIndexOf("/") + 1,
                dataPath.lastIndexOf(EcgConst.FILE_END_NAME));
            File reportDir = new File(dataDir + "/" + dataName + "_report");
            File[] dotFiles = reportDir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    return pathname.getAbsolutePath().endsWith("_dot" + ".png");
                }
            });
            if (dotFiles != null && dotFiles.length > 0)
            {
                sb.append("     <img alt=\"\" src=\"");
                sb.append(dotFiles[0].getName());
                sb.append("\">\n");
            }
            sb.append(" </div>\n");
            return sb.toString();
        }
        
        public String packageResultArea()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(" <div align=\"center\" style=\"margin-top: 30px\">\n");
            sb.append("     <table width=\"800px\" cellpadding=\"0\" style=\"border-collapse: collapse;border: 1px solid black\">\n");
            sb.append("         <tr>\n");
            sb.append("             <td align=\"left\" valign=\"top\" width=\"400px\" height=\"30px\" style=\"border: 1px solid black;padding-left: 3px\">检测结果（供参考）：<br><br>");
            StringBuilder abSb = new StringBuilder(100);
            Set<String> keys = WaveScreen.abnormalParameterMap.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext())
            {
                String abnomalStr = it.next();
                Integer value =  WaveScreen.abnormalParameterMap.get(abnomalStr);
                if (abnomalStr.equals("心动过缓") || abnomalStr.equals("心动过速"))
                {
                    abSb.append(abnomalStr).append(":").append(value * 10).append("s\n");
                }
                else
                {
                    abSb.append(abnomalStr).append(":").append(value).append("次\n");
                }
            }
            if (StringUtil.isNullOrEmpty(abSb.toString()))
            {
                sb.append("<br>&nbsp;<br>&nbsp;<br>&nbsp;<br>&nbsp;<br>&nbsp;<br>&nbsp;");
            }
            else
            {
                sb.append("<br>");
                sb.append(abSb.toString());
            }
            sb.append("             </td>\n");
            sb.append("             <td align=\"left\" valign=\"top\" width=\"400px\" style=\"border: 1px solid black;padding-left: 3px\">医生诊断结论：<br>&nbsp;<br>&nbsp;<br>&nbsp;<br>&nbsp;<br>&nbsp;<br>签名:</td>\n");
            sb.append("         </tr>\n");
            sb.append("     </table>\n");
            sb.append(" </div>\n");
            sb.append(" <br><br><br><br><br><br>\n");
            return sb.toString();
        }
        
        private int sumInt(List<Integer> nums)
        {
            int n = 0;
            for (Integer i : nums)
            {
                n += i;
            }
            return n;
        }
        
        private double sumDouble(List<Double> nums)
        {
            double n = 0;
            for (Double i : nums)
            {
                n += i;
            }
            return n;
        }
        
    }
    
    /**
     * 散点图绘制
     * @author zhuhf
     * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-20]
     */
    private class DotGraphDrawer
    {
        private final static String TAG = "DotGraphDrawer";
        private String filePath;
        private long collectingTime;
        private int rrLength = 0;
        private int[] rrX = null;
        private int[] rrY = null;
        private int[] heartX = null;
        private int[] heartY = null;
        
        public DotGraphDrawer(String filePath, long collectingTime)
        {
            this.filePath = filePath;
            this.collectingTime = collectingTime;
            init();
        }
        
        /**
         * 初始化数据
         */
        private void init()
        {
            BufferedReader br = null;
            try
            {
                br = new BufferedReader(new FileReader(new File(filePath)));
                StringBuilder rrBuild = new StringBuilder(100);
                String str;
                while ((str = br.readLine()) != null)
                {
                    rrBuild.append(str);
                }
                String rrStr = rrBuild.toString();
                rrStr = rrStr.substring(1, rrStr.length());
                String[] rrs = rrStr.split(",");
                int[] rr = new int[rrs.length];
                for (int i = 0;i < rrs.length; i++)
                {
                    rr[i] = Integer.parseInt(rrs[i].trim());
                }
                rrLength = rrs.length;
                // 初始化坐标
                initRRPoint(rr);
                initHeartPoint(rr);
            }
            catch (Exception e)
            {
                LogUtil.e(TAG, e);
            }
            finally
            {
                try
                {
                    if (br != null)
                    {
                        br.close();
                    }
                }
                catch (Exception e2)
                {
                    LogUtil.e(TAG, e2);
                }
            }
        }
        
        /**
         * 初始化RR间期散点图坐标
         * @param rr
         */
        private void initRRPoint(int[] rr)
        {
            rrX = rr;
            rrY = new int[rrLength - 1];
            for (int i = 0; i < rrLength - 1; i++)
            {
                rrY[i] = rr[i + 1];
            }
        }
        
        /**
         * 初始化心率变异散点图坐标
         * @param rr
         */
        private void initHeartPoint(int[] rr)
        {
            heartX = new int[rrLength - 1];
            for (int i = 0; i < rrLength - 1; i++)
            {
                heartX[i] = rr[i + 1] - rr[i];
            }
            heartY = new int[rrLength - 2];
            for (int i = 0; i < rrLength - 2; i++)
            {
                heartY[i] = heartX[i + 1];
            }
        }
        
        public void onDraw(Canvas canvas)
        {
            // 擦背景
            canvas.drawColor(Color.WHITE);
            onDrawRRDot(canvas);
            // x轴平移
            canvas.translate(DOT_GRAPH_WRIDTH / 2f, 0);
            onDrawHeartDot(canvas);
        }
        
        /**
         * 绘制RR间期散点图
         * @param canvas
         */
        private void onDrawRRDot(Canvas canvas)
        {
            new RRDotDrawer(rrX, rrY, collectingTime).onDraw(canvas);
        }
        
        /**
         * 绘制心率变异散点图
         * @param canvas
         */
        private void onDrawHeartDot(Canvas canvas)
        {
            new HeartDotDrawer(heartX, heartY, collectingTime).onDraw(canvas);
        }
        
        
        /**
         * RR间期散点图绘制类
         * @author zhuhf
         * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-21]
         */
        private class RRDotDrawer
        {
            /** 上下左右间距*/
            private static final int MARGIN = 50;
            /** 坐标*/
            private int[] x;
            private int[] y;
            /** 采集时间*/
            private long collectingTime;
            /** 点画笔*/
            private Paint pointPaint;
            /** 线画笔*/
            private Paint linePaint;
            /** 文字画笔*/
            private Paint textPaint;
            /** 屏幕宽度和高度*/
            private int viewWidth;
            private int viewHeight;
            /** 视图边长*/
            private int viewSide;
            /** 正方形边长*/
            private int squareSide;
            
            public RRDotDrawer(int[] x, int[] y, long collectingTime)
            {
                this.x = x;
                this.y = y;
                this.collectingTime = collectingTime;
                init();
            }
            
            /**
             * 初始化
             */
            private void init()
            {
                pointPaint = new Paint();
                pointPaint.setStyle(Style.STROKE);
                pointPaint.setStrokeWidth(3);
                pointPaint.setColor(Color.RED);
                linePaint = new Paint();
                linePaint.setColor(Color.DKGRAY);
                linePaint.setStyle(Style.STROKE);
                textPaint = new Paint();
                textPaint.setColor(Color.BLUE);
                viewWidth = new Float(DOT_GRAPH_WRIDTH / 2f).intValue();
                viewHeight = DOT_GRAPH_HEIGHT;
                viewSide = Math.min(viewWidth, viewHeight);
                squareSide = viewSide - MARGIN * 2;
            }
            
            
            private void onDraw(Canvas canvas)
            {
                // 绘制背景
                drawBackground(canvas);
                if (x != null && y != null)
                {
                    // 绘制散点
                    drawDotGraph(canvas);
                }
            }
            
            /**
             * 绘制背景
             * @param canvas
             */
            private void drawBackground(Canvas canvas)
            {
                // 外框
                canvas.drawRect(MARGIN - 5, MARGIN - 5, viewSide - MARGIN + 5, viewSide - MARGIN + 5, linePaint);
                // 内框
                canvas.drawRect(MARGIN, MARGIN, viewSide - MARGIN, viewSide - MARGIN, linePaint);
                // 绘制横线
                for (int i = 0; i < 9; i++)
                {
                    canvas.drawLine(MARGIN, MARGIN + (squareSide / 10f) * (i + 1), squareSide + MARGIN, MARGIN + (squareSide / 10f) * (i + 1), linePaint);
                }
                // 绘制竖线
                for (int i = 0; i < 9; i++)
                {
                    canvas.drawLine(MARGIN + (squareSide / 10f) * (i + 1), MARGIN, MARGIN + (squareSide / 10f) * (i + 1), squareSide + MARGIN, linePaint);
                }
                // 绘制交叉线
                canvas.drawLine(MARGIN, squareSide + MARGIN, squareSide + MARGIN, MARGIN, linePaint);
                // 绘制斜线
                for (int i = 0; i < 19; i++)
                {
                   if (i <= 9)
                   {
                       if (i == 5)
                       {
                           linePaint.setStyle(Style.FILL);
                           linePaint.setPathEffect(null);
                           linePaint.setColor(Color.RED);
                       }
                       else if (i == 9)
                       {
                           linePaint.setStyle(Style.FILL);
                           linePaint.setPathEffect(null);
                           linePaint.setColor(Color.RED);
                       } 
                       else
                       {
                           PathEffect mEffects= new DashPathEffect(new float[] {15, 5, 15, 5 },1);
                           linePaint.setStyle(Style.STROKE);
                           linePaint.setPathEffect(mEffects);
                           linePaint.setColor(Color.GREEN);
                       }
                       canvas.drawLine(MARGIN, squareSide + MARGIN - (squareSide / 10f) * (i + 1), MARGIN + (squareSide / 10f) * (i + 1), squareSide + MARGIN, linePaint);
                   }
                   else
                   {
                       PathEffect mEffects= new DashPathEffect(new float[] {15, 5, 15, 5 },1);
                       linePaint.setStyle(Style.STROKE);
                       linePaint.setPathEffect(mEffects);
                       linePaint.setColor(Color.GREEN);
                       canvas.drawLine(MARGIN + (squareSide / 10f) * (i - 10 + 1), MARGIN, squareSide + MARGIN, squareSide + MARGIN - (squareSide / 10f) * (i - 10 + 1), linePaint);
                   }
                }
                PathEffect mEffects= new DashPathEffect(new float[] {2, 3, 2, 3 },1);
                linePaint.setStyle(Style.STROKE);
                linePaint.setPathEffect(mEffects);
                linePaint.setColor(Color.GRAY);
                // 绘制红线之间的虚线(横向)
                for (int i = 0; i < 10; i++)
                {
                    if (i < 4)
                    {
                        canvas.drawLine(MARGIN, MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), linePaint);
                    }
                    else
                    {
                        canvas.drawLine(MARGIN + (squareSide / 20f) * ( 2 * (i - 4) + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), linePaint);
                    }
                }
                // 绘制红线之间的虚线(竖向)
                for (int i = 0; i < 10; i++)
                {
                    if (i < 6)
                    {
                        canvas.drawLine(MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 10f) * 4 + (squareSide / 20f) * ( 2 * i + 1), linePaint);
                    }
                    else
                    {
                        canvas.drawLine(MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), MARGIN + (squareSide / 20f) * ( 2 * i + 1), squareSide + MARGIN, linePaint);
                    }
                }
                // 绘制x,y坐标轴刻度
                canvas.drawText("0", MARGIN - 10 - textPaint.measureText("0"), squareSide + MARGIN + 20, textPaint);
                canvas.drawText("(ms)", MARGIN - 10 - textPaint.measureText("(ms)"), squareSide + MARGIN + 5 - squareSide / 20f, textPaint);
                String[] str1 = {"400", "800", "1200", "1600", "2000"};
                for (int i = 0; i < str1.length; i++)
                {
                    canvas.drawText(str1[i], MARGIN + (squareSide / 10f) * (i + 1) * 2 - textPaint.measureText(str1[i]) / 2f, squareSide + MARGIN + 20, textPaint);
                }
                for (int i = 0; i < str1.length; i++)
                {
                    canvas.drawText(str1[i], MARGIN - 10 - textPaint.measureText(str1[i]), squareSide + MARGIN - (squareSide / 10f) * (i + 1) * 2, textPaint);
                }
                // 绘制心率标尺刻度
                String[] str2 = {"600", "300", "200", "150", "120", "100", "86", "75", "67", "60", "55", "50", "46", "43", "40", "38", "35", "33", "32"};
                for (int i = 0; i < str2.length; i++)
                {
                    canvas.drawText(str2[i], MARGIN + (squareSide / 20f) * (i + 1), squareSide + MARGIN - (squareSide / 20f) * (i + 1) + textPaint.measureText(str2[2]) / 2, textPaint);
                }
                // 绘制"心率标尺"文字
                String[] str3 = {"心", "率", "标", "尺"};
                textPaint.setTextSize(20);
                for (int i = 0; i < str3.length; i++)
                {
                    canvas.drawText(str3[i], squareSide / 2f + MARGIN + (squareSide / 10f) * (i + 1) - textPaint.measureText(str3[i]), squareSide / 2f + MARGIN - (squareSide / 10f) * (i + 1), textPaint);
                }
                // 绘制"心动过速界限"
                canvas.save();
                canvas.rotate(45, MARGIN, MARGIN + (squareSide / 10f) * 4);
                String[] str4 = {"心", "动", "过"};
                for (int i = 0; i < str4.length; i++)
                {
                    canvas.drawText(str4[i], MARGIN + (squareSide / 10f) * (i + 1) - textPaint.measureText(str4[i]), MARGIN + (squareSide / 10f) * 4, textPaint);
                }
                String[] str5 = {"速", "界", "线"};
                for (int i = 0; i < str5.length; i++)
                {
                    canvas.drawText(str5[i], MARGIN + (squareSide / 10f) * 5 + (squareSide / 10f) * (i + 1) - textPaint.measureText(str5[i]), MARGIN + (squareSide / 10f) * 4, textPaint);
                }
                canvas.restore();
                canvas.save();
                // 绘制"心动过缓界限"
                canvas.rotate(45, MARGIN, MARGIN);
                String[] str6 = {"心", "动", "过"};
                for (int i = 0; i < str6.length; i++)
                {
                    canvas.drawText(str6[i], MARGIN + (squareSide / 10f) * (i + 1) * 2 - textPaint.measureText(str6[i]), MARGIN, textPaint);
                }
                String[] str7 = {"缓", "界", "线"};
                for (int i = 0; i < str7.length; i++)
                {
                    canvas.drawText(str7[i], MARGIN + (squareSide / 10f) * 7 + (squareSide / 10f) * (i + 1) * 2 - textPaint.measureText(str7[i]), MARGIN, textPaint);
                }
                canvas.restore();
                // 绘制标题
                textPaint.setTextSize(30);
                canvas.drawText("RR间期散点图", (float)squareSide / 2 + MARGIN + 10 - textPaint.measureText("RR间期散点图") / 2, MARGIN + 40, textPaint);
                textPaint.setTextSize(15);
                textPaint.setColor(Color.BLACK);
                canvas.drawText("采集时间：" + collectingTime / 1000f + "s", MARGIN + (float)squareSide / 2 + 10 - textPaint.measureText("采集时间：" + collectingTime / 1000f + "s") / 2, squareSide + MARGIN + 60, textPaint);
            }
            
            /**
             * 绘制散点
             * @param canvas
             */
            private void drawDotGraph(Canvas canvas)
            {
                for (int i = 0;i< x.length && i < y.length; i++)
                {
                    canvas.drawPoint(MARGIN + (x[i] * squareSide) / 2000f, squareSide + MARGIN - (y[i] * squareSide) / 2000f, pointPaint);
                }
            }
        }
        
        /**
         * 心率变异绘制类
         * @author zhuhf
         * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-21]
         */
        private class HeartDotDrawer
        {
            /** 上下左右间距*/
            private static final int MARGIN = 50;
            /** 坐标*/
            private int[] x;
            private int[] y;
            /** 采集时间*/
            private long collectingTime;
            /** 点画笔*/
            private Paint pointPaint;
            /** 线画笔*/
            private Paint linePaint;
            /** 文字画笔*/
            private Paint textPaint;
            /** 屏幕宽度和高度*/
            private int viewWidth;
            private int viewHeight;
            /** 视图边长*/
            private int viewSide;
            /** 正方形边长*/
            private int squareSide;
            
            public HeartDotDrawer(int[] x, int[] y, long collectingTime)
            {
                this.x = x;
                this.y = y;
                this.collectingTime = collectingTime;
                init();
            }
            
            /**
             * 初始化
             */
            private void init()
            {
                pointPaint = new Paint();
                pointPaint.setStyle(Style.STROKE);
                pointPaint.setStrokeWidth(3);
                pointPaint.setColor(Color.RED);
                linePaint = new Paint();
                linePaint.setColor(Color.DKGRAY);
                linePaint.setStyle(Style.STROKE);
                textPaint = new Paint();
                textPaint.setColor(Color.BLUE);
                viewWidth = new Float(DOT_GRAPH_WRIDTH / 2f).intValue();
                viewHeight = DOT_GRAPH_HEIGHT;
                viewSide = Math.min(viewWidth, viewHeight);
                squareSide = viewSide - MARGIN * 2;
            }
            
            private void onDraw(Canvas canvas)
            {
                // 绘制背景
                drawBackground(canvas);
                if (x != null && y != null)
                {
                    // 绘制散点
                    drawDotGraph(canvas);
                }
            }
            
            /**
             * 绘制背景
             * @param canvas
             */
            private void drawBackground(Canvas canvas)
            {
                // 外框
                canvas.drawRect(MARGIN - 5, MARGIN - 5, viewSide - MARGIN + 5, viewSide - MARGIN + 5, linePaint);
                // 内框
                canvas.drawRect(MARGIN, MARGIN, viewSide - MARGIN, viewSide - MARGIN, linePaint);
                canvas.drawLine(MARGIN, (float)squareSide / 2 + MARGIN, squareSide + MARGIN, (float)squareSide / 2 + MARGIN, linePaint);
                canvas.drawLine((float)squareSide / 2 + MARGIN, MARGIN, (float)squareSide / 2 + MARGIN, squareSide + MARGIN, linePaint);
                PathEffect mEffects= new DashPathEffect(new float[] {2, 5, 2, 5 },1);
                linePaint.setPathEffect(mEffects);
                linePaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle((float)squareSide / 2 + MARGIN, (float)squareSide / 2 + MARGIN, (float)squareSide / 6, linePaint);
                canvas.drawCircle((float)squareSide / 2 + MARGIN, (float)squareSide / 2 + MARGIN, (float)squareSide / 3, linePaint);
                canvas.drawCircle((float)squareSide / 2 + MARGIN, (float)squareSide / 2 + MARGIN, (float)squareSide / 2, linePaint);
                canvas.drawText("-200", (float)squareSide / 6 + MARGIN - textPaint.measureText("-200") / 2, (float)squareSide / 2 + MARGIN, textPaint);
                canvas.drawText("200(ms)", ((float)squareSide / 6) * 5 + MARGIN - textPaint.measureText("200(ms)") / 2, (float)squareSide / 2 + MARGIN, textPaint);
                canvas.drawText("200(ms)", (float)squareSide / 2 + MARGIN - textPaint.measureText("200(ms)") / 2, (float)squareSide / 6 + MARGIN, textPaint);
                canvas.drawText("-200", (float)squareSide / 2 + MARGIN - textPaint.measureText("-200") / 2, ((float)squareSide / 6) * 5 + MARGIN, textPaint);
                textPaint.setTextSize(30);
                canvas.drawText("心率变异散点图", (float)squareSide / 2 + MARGIN - textPaint.measureText("心率变异散点图") / 2, MARGIN + 30, textPaint);
                textPaint.setTextSize(15);
                textPaint.setColor(Color.BLACK);
                canvas.drawText("采集时间：" + collectingTime / 1000f + "s", MARGIN + (float)squareSide / 2 - textPaint.measureText("采集时间：" + collectingTime / 1000f + "s") / 2, squareSide + MARGIN + 30, textPaint);
            }
            
            /**
             * 绘制散点
             * @param canvas
             */
            private void drawDotGraph(Canvas canvas)
            {
                for (int i = 0;i< x.length && i < y.length; i++)
                {
                    canvas.drawPoint((float)squareSide / 2 + MARGIN + (x[i] * squareSide) / 600f, (float)squareSide / 2 + MARGIN + (-(y[i] * squareSide) / 600f), pointPaint);
                }
            }
        }
    }
    
    /**
     * 心率绘图
     * @author zhuhf
     * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-12-20]
     */
    private class WaveDrawer
    {
        private int[] displayDataCh1;
        private int[] displayDataCh2;
        private int updateCh1DataIndex;
        private boolean switchScreen;
        /** 波形Y轴基偏移量 */
        private static final int CENTER_Y_CH = 50;
        /** 文字X轴偏移量 */
        private static final int TEXT_X_OFFSET = 40;
        private Paint ch0_paint = new Paint();
        private Paint ch1_paint = new Paint();
        private Paint ch2_paint = new Paint();
        private Paint textPaint0 = new Paint();
        private Paint textPaint1 = new Paint();
        private Paint textPaint2 = new Paint();
        /** 背景画笔 */
        private Paint bg_paint = new Paint();
        /** 字体画笔*/
        private Paint txt_paint = new Paint();
        
        public WaveDrawer(int[] ch1Data, int[] ch2Data, int updateIndex, boolean switchScreen)
        {
            displayDataCh1 = ch1Data;
            displayDataCh2 = ch2Data;
            updateCh1DataIndex = updateIndex;
            this.switchScreen = switchScreen;
        }
        /**
         * 绘制心率图
         * @param canvas
         */
        public void onDraw(Canvas canvas)
        {
            // 擦背景
            canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
            // 绘制背景
            drawBackground(canvas);
            // 绘制心率
            drawWave(canvas);
        }
        
        /**
         * 绘制背景图
         * @param canvas
         */
        private void drawBackground(Canvas canvas)
        {
            canvas.drawColor(Color.WHITE);
            // 绘制字体
            txt_paint.setColor(Color.BLACK);
            txt_paint.setTextSize(20);
            canvas.drawText("10 mm/mV", 10, 20 , txt_paint);
            canvas.drawText("25 mm/s", EcgConst.GRID_WIDTH * 5, 20 , txt_paint);
            // 绘制背景
            PathEffect mEffects1= new DashPathEffect(new float[] {5, 5, 5, 5 },
                1);
            PathEffect mEffects2= new DashPathEffect(new float[] {2, 5, 2, 5 },
                1);
            int displayWidth = EcgConst.DISPLAY_WIDTH;
            int displayHeight = EcgConst.DISPLAY_HEIGH;
            int gridWidth = EcgConst.GRID_WIDTH; 
            bg_paint.setColor(Color.RED);

            for (int i = 0; (i * gridWidth) / 10f < displayHeight; i++)
            {
                bg_paint.setStrokeWidth(1);
                bg_paint.setStyle(Paint.Style.STROKE);
                bg_paint.setPathEffect(mEffects1);
                canvas.drawRect(-1,
                    (i * 2 + 1) * gridWidth,
                    displayWidth,
                    (i + 1) * 2 * gridWidth,
                    bg_paint);
                
                bg_paint.setStrokeWidth(0);
                bg_paint.setPathEffect(mEffects2);
                canvas.drawLine(0,
                    (i + 1) * gridWidth - gridWidth / 2,
                    displayWidth,
                    (i + 1) * gridWidth - gridWidth / 2,
                    bg_paint);
            }
            for (int i = 0; (i * gridWidth) / 10f < displayWidth; i++)
            {
                bg_paint.setStrokeWidth(1);
                bg_paint.setStyle(Paint.Style.STROKE);
                bg_paint.setPathEffect(mEffects1);
                canvas.drawRect((i * 2 + 1) * gridWidth,
                    -1,
                    (i + 1) * 2 * gridWidth,
                    displayHeight,
                    bg_paint);
                
                bg_paint.setStrokeWidth(0);
                bg_paint.setPathEffect(mEffects2);
                canvas.drawLine((i + 1) * gridWidth - gridWidth / 2,
                    0,
                    (i + 1) * gridWidth - gridWidth / 2,
                    displayHeight,
                    bg_paint);
            }
            
            // 绘制点
            bg_paint.setStrokeWidth(1);
            for (int i = 0; (i * gridWidth) / 10f < displayWidth; i++)
            {
                for (int j = 0; (j * gridWidth) / 10f < displayHeight; j++)
                {
                    if (!((((i + 1) * (gridWidth / 10f)) % (gridWidth / 2f) == 0) || ((j + 1) * (gridWidth / 10f)) % (gridWidth / 2f) == 0))
                    {
                        canvas.drawPoint((i + 1) * (gridWidth / 10f), (j + 1) * (gridWidth / 10f), bg_paint);
                    }
                }
            }
        }
        
        /**
         * 绘制心率
         * @param canvas
         */
        private void drawWave(Canvas canvas)
        {
            switch (EcgDrawView.mCurentLead)
            {
                case EcgConst.LIMB_LEAD:
                    drawLimbLead(canvas);
                    break;
                case EcgConst.MOCK_LIMB_LEAD:
                    drawMockLimbLead(canvas);
                    break;
                case EcgConst.MOCK_CHEST_LEAD:
                    drawChestLead(canvas);
                    break;
                case EcgConst.SIMPLE_LIMB_LEAD:
                    drawSimpleLimbLead(canvas);
                    break;
                default:
                    break;
            }
        }
        
        /**
         * 绘制肢体导联
         * 
         * @param canvas 画布
         */
        private void drawLimbLead(Canvas canvas)
        {
            int oldX, oldY, newY;

            if (!switchScreen)
            {
                // Draw I
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = (CENTER_Y_CH * 2 - (displayDataCh2[0] - displayDataCh1[0]));
                newY = 0;
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("Ⅰ", TEXT_X_OFFSET, CENTER_Y_CH, textPaint0);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (CENTER_Y_CH * 2 - (displayDataCh2[i] - displayDataCh1[i]));
                    
                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 2;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 2 - EcgConst.GRID_WIDTH;
                    }
                    
                    if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                    {
                        newY = -1;
                    }
                    
                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch0_paint);

                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch0_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }

                // Draw II
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = (CENTER_Y_CH * 6 - displayDataCh2[0]);
                newY = 0;
                if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("Ⅱ", TEXT_X_OFFSET, CENTER_Y_CH * 5, textPaint1);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (CENTER_Y_CH * 10 - displayDataCh2[i]);
                    
                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 6;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 6 - EcgConst.GRID_WIDTH;
                    }
                    
                    if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh2[i] == -1))
                    {
                        newY = -1;
                    }
                    
                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch1_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch1_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }

                // Draw III
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = (CENTER_Y_CH * 10 - displayDataCh1[0]);
                newY = 0;
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("Ⅲ", TEXT_X_OFFSET, CENTER_Y_CH * 9, textPaint2);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (CENTER_Y_CH * 14 - displayDataCh1[i]);

                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 10;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 10 - EcgConst.GRID_WIDTH;
                    }
                    
                    if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                    {
                        newY = -1;
                    }
                    
                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch2_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch2_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }
            }
            else
            {
                // Draw AVR
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = (CENTER_Y_CH * 2 - (displayDataCh1[0] - 2 * displayDataCh2[0]) / 2);
                newY = 0;
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("aVR", TEXT_X_OFFSET, CENTER_Y_CH, textPaint0);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (- (displayDataCh1[i] - 2 * displayDataCh2[i]) / 2);

                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 2;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 2 - EcgConst.GRID_WIDTH;
                    }
                    
                    if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                    {
                        newY = -1;
                    }
                    
                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch0_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch0_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }
                // Draw AVL
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = (CENTER_Y_CH * 6 - (displayDataCh2[0] - 2 * displayDataCh1[0]) / 2);
                newY = 0;
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("aVL", TEXT_X_OFFSET, CENTER_Y_CH * 5, textPaint1);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (CENTER_Y_CH * 4 - (displayDataCh2[i] - 2 * displayDataCh1[i]) / 2);

                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 6;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 6 - EcgConst.GRID_WIDTH;
                    }
                    
                    if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                    {
                        newY = -1;
                    }
                    
                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch1_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch1_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }
                // Draw AVF
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = CENTER_Y_CH * 10 - (displayDataCh1[0] + displayDataCh2[0]) / 2;
                newY = 0;
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("aVF", TEXT_X_OFFSET, CENTER_Y_CH * 9, textPaint2);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (CENTER_Y_CH * 14 - (displayDataCh1[i] + displayDataCh2[i]) / 2);

                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 10;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 10 - EcgConst.GRID_WIDTH;
                    }
                    
                    if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1 || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                    {
                        newY = -1;
                    }
                    
                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1 || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch2_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch2_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }
            }
        }
        
        /**
         * 绘制模拟肢体导联
         * 
         * @param canvas 画布
         */
        private void drawMockLimbLead(Canvas canvas)
        {
            int oldX, oldY, newY;

            if (!switchScreen)
            {
                // Draw MI
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = (CENTER_Y_CH * 2 - (displayDataCh2[0] - displayDataCh1[0]));
                newY = 0;
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("MⅠ",
                        TEXT_X_OFFSET,
                        CENTER_Y_CH,
                        textPaint0);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (CENTER_Y_CH * 2 - (displayDataCh2[i] - displayDataCh1[i]));

                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 2;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 2 - EcgConst.GRID_WIDTH;
                    }

                    if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                        || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                    {
                        newY = -1;
                    }

                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                        || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch0_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch0_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }

                // Draw MII
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = (CENTER_Y_CH * 6 - displayDataCh2[0]);
                newY = 0;
                if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("MⅡ",
                        TEXT_X_OFFSET,
                        CENTER_Y_CH * 5,
                        textPaint1);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (CENTER_Y_CH * 10 - displayDataCh2[i]);

                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 6;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 6 - EcgConst.GRID_WIDTH;
                    }

                    if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] == -1
                        || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh2[i] == -1))
                    {
                        newY = -1;
                    }

                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                        || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch1_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch1_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }

                // Draw MIII
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = (CENTER_Y_CH * 10 - displayDataCh1[0]);
                newY = 0;
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("MⅢ",
                        TEXT_X_OFFSET,
                        CENTER_Y_CH * 9,
                        textPaint2);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (CENTER_Y_CH * 14 - displayDataCh1[i]);

                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 10;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 10 - EcgConst.GRID_WIDTH;
                    }

                    if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                        || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                    {
                        newY = -1;
                    }

                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                        || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch2_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch2_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }
            }
            else
            {
                // Draw MaVR
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = (CENTER_Y_CH * 2 - (displayDataCh1[0] - 2 * displayDataCh2[0]) / 2);
                newY = 0;
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("MaVR",
                        TEXT_X_OFFSET,
                        CENTER_Y_CH,
                        textPaint0);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = ( - (displayDataCh1[i] - 2 * displayDataCh2[i]) / 2);

                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 2;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 2 - EcgConst.GRID_WIDTH;
                    }

                    if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                        || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                    {
                        newY = -1;
                    }

                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                        || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch0_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch0_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }
                // Draw MaVL
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = (CENTER_Y_CH * 6 - (displayDataCh2[0] - 2 * displayDataCh1[0]) / 2);
                newY = 0;
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("MaVL",
                        TEXT_X_OFFSET,
                        CENTER_Y_CH * 5,
                        textPaint1);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (CENTER_Y_CH * 4 - (displayDataCh2[i] - 2 * displayDataCh1[i]) / 2);

                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 6;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 6 - EcgConst.GRID_WIDTH;
                    }

                    if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                        || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                    {
                        newY = -1;
                    }

                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                        || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch1_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch1_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }
                // Draw MaVF
                oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
                oldY = CENTER_Y_CH * 10 - (displayDataCh1[0] + displayDataCh2[0])
                    / 2;
                newY = 0;
                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
                {
                    canvas.drawText("MaVF",
                        TEXT_X_OFFSET,
                        CENTER_Y_CH * 9,
                        textPaint2);
                }
                for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
                {
                    newY = (CENTER_Y_CH * 14 - (displayDataCh1[i] + displayDataCh2[i]) / 2);

                    if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                        || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                    {
                        newY = CENTER_Y_CH * 10;
                    }
                    else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                        && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        newY = CENTER_Y_CH * 10 - EcgConst.GRID_WIDTH;
                    }

                    if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                        || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                    {
                        newY = -1;
                    }

                    if (newY == -1)
                    {
                        // do nothing
                    }
                    else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                        || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX,
                            newY,
                            ch2_paint);
                    }
                    else
                    {
                        canvas.drawLine(oldX,
                            oldY,
                            oldX + 1,
                            newY,
                            ch2_paint);
                    }
                    oldX = oldX + 1;
                    oldY = newY;
                }
            }
        }

        /**
         * 绘制模拟胸导联
         * 
         * @param canvas 画布
         */
        private void drawChestLead(Canvas canvas)
        {
            int oldX, oldY, newY;

            // Draw MV1
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 4 - displayDataCh1[0]);
            newY = 0;
            if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MV1",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 3,
                    textPaint1);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 8 - displayDataCh1[i]);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 4;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 4 - EcgConst.GRID_WIDTH;
                }

                if (displayDataCh1[EcgConst.WAVE_DEVIATION_VALUE] == -1
                    || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh1[i] == -1))
                {
                    newY = -1;
                }

                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                    || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch1_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch1_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }

            // Draw MV5
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 8 - displayDataCh2[0]);
            newY = 0;
            if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] != -1)
            {
                canvas.drawText("MV5",
                    TEXT_X_OFFSET,
                    CENTER_Y_CH * 7,
                    textPaint2);
            }
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 12 - displayDataCh2[i]);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 8;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 8 - EcgConst.GRID_WIDTH;
                }

                if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] == -1
                    || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh2[i] == -1))
                {
                    newY = -1;
                }

                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                    || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch2_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch2_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }
        }
        
        /**
         * 绘简易肢体导联
         * 
         * @param canvas 画布
         */
        private void drawSimpleLimbLead(Canvas canvas)
        {
            int oldX, oldY, newY;

            // Draw II
            oldX = EcgConst.DISPLAY_LEFT_LEAD_NUMBER_WIDTH;
            oldY = (CENTER_Y_CH * 6 - displayDataCh2[0]);
            newY = 0;
            for (int i = 0; i < updateCh1DataIndex + EcgConst.WAVE_DEVIATION_VALUE; i++)
            {
                newY = (CENTER_Y_CH * 10 - displayDataCh2[i]);

                if ((i >= 0 && i < EcgConst.WAVE_DEVIATION_VALUE_PART1)
                    || (i >= EcgConst.WAVE_DEVIATION_VALUE_PART2 && i < EcgConst.WAVE_DEVIATION_VALUE))
                {
                    newY = CENTER_Y_CH * 6;
                }
                else if (i >= EcgConst.WAVE_DEVIATION_VALUE_PART1
                    && i <= EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    newY = CENTER_Y_CH * 6 - EcgConst.GRID_WIDTH;
                }

                if (displayDataCh2[EcgConst.WAVE_DEVIATION_VALUE] == -1
                    || (i > EcgConst.WAVE_DEVIATION_VALUE && displayDataCh2[i] == -1))
                {
                    newY = -1;
                }

                if (newY == -1)
                {
                    // do nothing
                }
                else if (i == EcgConst.WAVE_DEVIATION_VALUE_PART1
                    || i == EcgConst.WAVE_DEVIATION_VALUE_PART2)
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX,
                        newY,
                        ch1_paint);
                }
                else
                {
                    canvas.drawLine(oldX,
                        oldY,
                        oldX + 1,
                        newY,
                        ch1_paint);
                }
                oldX = oldX + 1;
                oldY = newY;
            }
        }
    }
    
    
}
