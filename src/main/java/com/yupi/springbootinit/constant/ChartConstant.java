package com.yupi.springbootinit.constant;

import cn.hutool.core.io.FileUtil;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.ThrowUtils;

public interface ChartConstant {

    /**
     * 图表状态
     */
    String CHART_STATUS_WAIT = "wait";
    String CHART_STATUS_RUNNING = "running";
    String CHART_STATUS_SUCCEED = "succeed";
    String CHART_STATUS_FAILED = "failed";

    /**
     * 文件上传的最大容量
     */
    long ONE_MB = 1024 * 1024;

    /**
     * 系统支持的文件格式
     */
    String[] SUPPORT_FILE_TYPE = new String[]{"xlsx", "xls", "csv"};


    //根据传入的文件名,判断该文件是否符合文件上传要求
    public static boolean isSupportFileType(String OriginalFilename) {
        ThrowUtils.throwIf(OriginalFilename == null, ErrorCode.PARAMS_ERROR,"文件为null");
        //获取文件后缀
        String suffix = FileUtil.getSuffix(OriginalFilename);
        //判断文件是否在支持的文件类型中;是就返回true 否则返回false
        for (String type : SUPPORT_FILE_TYPE) {
            if (type.equals(suffix)) {
                return true;
            }
        }
        return false;
    }
}
