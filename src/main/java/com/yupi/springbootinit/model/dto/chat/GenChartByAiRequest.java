/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2024/4/22
 * Time: 17:18
 */
package com.yupi.springbootinit.model.dto.chat;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenChartByAiRequest implements Serializable {

    private static final long serialVersionUID = -618542542103746627L;
    //图表名称
    private String name;
    //目标
    private  String goal;
    //图表类型
    private  String chartType;


}
