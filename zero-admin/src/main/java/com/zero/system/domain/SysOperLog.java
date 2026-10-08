package com.zero.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zero.common.annotation.Excel;
import com.zero.common.annotation.Excel.ColumnType;
import com.zero.common.core.domain.BaseEntity;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 操作日志记录表 oper_log
 */
public class SysOperLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 日志主键
     */
    @Excel(name = "操作序号", cellType = ColumnType.NUMERIC)
    private Long operId;

    /**
     * 操作模块
     */
    @Excel(name = "操作模块")
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    @Excel(name = "业务类型", readConverterExp = "0=其它,1=新增,2=修改,3=删除,4=授权,5=导出,6=导入,7=强退,8=生成代码,9=清空数据")
    private Integer businessType;

    /**
     * 业务类型数组
     */
    private Integer[] businessTypes;

    /**
     * 请求方法
     */
    @Excel(name = "请求方法")
    private String method;

    /**
     * 请求方式
     */
    @Excel(name = "请求方式")
    private String requestMethod;

    /**
     * 操作类别（0其它 1后台用户 2手机端用户）
     */
    @Excel(name = "操作类别", readConverterExp = "0=其它,1=后台用户,2=手机端用户")
    private Integer operatorType;

    /**
     * 操作人员
     */
    @Excel(name = "操作人员")
    private String operName;

    /**
     * 部门名称
     */
    @Excel(name = "部门名称")
    private String deptName;

    /**
     * 请求url
     */
    @Excel(name = "请求地址")
    private String operUrl;

    /**
     * 操作地址
     */
    @Excel(name = "操作地址")
    private String operIp;

    /**
     * 操作地点
     */
    @Excel(name = "操作地点")
    private String operLocation;

    /**
     * 请求参数
     */
    @Excel(name = "请求参数")
    private String operParam;

    /**
     * 返回参数
     */
    @Excel(name = "返回参数")
    private String jsonResult;

    /**
     * 操作状态（0正常 1异常）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
    private Integer status;

    /**
     * 错误消息
     */
    @Excel(name = "错误消息")
    private String errorMsg;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operTime;

    /**
     * 消耗时间
     */
    @Excel(name = "消耗时间", suffix = "毫秒")
    private Long costTime;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取操作ID
     * @return
     * 返回值: Long
     * 返回值说明：获取操作ID
     */

    public Long getOperId()
    {
        return operId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置操作ID
     * @param operId 操作ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置操作ID
     */

    public void setOperId(Long operId)
    {
        this.operId = operId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取标题
     * @return
     * 返回值: String
     * 返回值说明：获取标题
     */

    public String getTitle()
    {
        return title;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置标题
     * @param title 标题参数
     * @return
     * 返回值: void
     * 返回值说明：设置标题
     */

    public void setTitle(String title)
    {
        this.title = title;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Business类型
     * @return
     * 返回值: Integer
     * 返回值说明：获取Business类型
     */

    public Integer getBusinessType()
    {
        return businessType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Business类型
     * @param businessType business类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置Business类型
     */

    public void setBusinessType(Integer businessType)
    {
        this.businessType = businessType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取BusinessTypes
     * @return
     * 返回值: Integer[]
     * 返回值说明：获取BusinessTypes
     */

    public Integer[] getBusinessTypes()
    {
        return businessTypes;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置BusinessTypes
     * @param businessTypes businessTypes参数
     * @return
     * 返回值: void
     * 返回值说明：设置BusinessTypes
     */

    public void setBusinessTypes(Integer[] businessTypes)
    {
        this.businessTypes = businessTypes;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取方法
     * @return
     * 返回值: String
     * 返回值说明：获取方法
     */

    public String getMethod()
    {
        return method;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置方法
     * @param method 方法参数
     * @return
     * 返回值: void
     * 返回值说明：设置方法
     */

    public void setMethod(String method)
    {
        this.method = method;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取请求方法
     * @return
     * 返回值: String
     * 返回值说明：获取请求方法
     */

    public String getRequestMethod()
    {
        return requestMethod;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置请求方法
     * @param requestMethod 请求方法参数
     * @return
     * 返回值: void
     * 返回值说明：设置请求方法
     */

    public void setRequestMethod(String requestMethod)
    {
        this.requestMethod = requestMethod;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Operator类型
     * @return
     * 返回值: Integer
     * 返回值说明：获取Operator类型
     */

    public Integer getOperatorType()
    {
        return operatorType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Operator类型
     * @param operatorType operator类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置Operator类型
     */

    public void setOperatorType(Integer operatorType)
    {
        this.operatorType = operatorType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取操作名称
     * @return
     * 返回值: String
     * 返回值说明：获取操作名称
     */

    public String getOperName()
    {
        return operName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置操作名称
     * @param operName 操作名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置操作名称
     */

    public void setOperName(String operName)
    {
        this.operName = operName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取部门名称
     * @return
     * 返回值: String
     * 返回值说明：获取部门名称
     */

    public String getDeptName()
    {
        return deptName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置部门名称
     * @param deptName 部门名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置部门名称
     */

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取操作地址
     * @return
     * 返回值: String
     * 返回值说明：获取操作地址
     */

    public String getOperUrl()
    {
        return operUrl;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置操作地址
     * @param operUrl 操作地址参数
     * @return
     * 返回值: void
     * 返回值说明：设置操作地址
     */

    public void setOperUrl(String operUrl)
    {
        this.operUrl = operUrl;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取操作IP
     * @return
     * 返回值: String
     * 返回值说明：获取操作IP
     */

    public String getOperIp()
    {
        return operIp;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置操作IP
     * @param operIp 操作IP参数
     * @return
     * 返回值: void
     * 返回值说明：设置操作IP
     */

    public void setOperIp(String operIp)
    {
        this.operIp = operIp;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取操作Location
     * @return
     * 返回值: String
     * 返回值说明：获取操作Location
     */

    public String getOperLocation()
    {
        return operLocation;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置操作Location
     * @param operLocation 操作Location参数
     * @return
     * 返回值: void
     * 返回值说明：设置操作Location
     */

    public void setOperLocation(String operLocation)
    {
        this.operLocation = operLocation;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取操作参数
     * @return
     * 返回值: String
     * 返回值说明：获取操作参数
     */

    public String getOperParam()
    {
        return operParam;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置操作参数
     * @param operParam 操作参数参数
     * @return
     * 返回值: void
     * 返回值说明：设置操作参数
     */

    public void setOperParam(String operParam)
    {
        this.operParam = operParam;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取JSON结果
     * @return
     * 返回值: String
     * 返回值说明：获取JSON结果
     */

    public String getJsonResult()
    {
        return jsonResult;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置JSON结果
     * @param jsonResult JSON结果参数
     * @return
     * 返回值: void
     * 返回值说明：设置JSON结果
     */

    public void setJsonResult(String jsonResult)
    {
        this.jsonResult = jsonResult;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取状态
     * @return
     * 返回值: Integer
     * 返回值说明：获取状态
     */

    public Integer getStatus()
    {
        return status;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置状态
     * @param status 状态参数
     * @return
     * 返回值: void
     * 返回值说明：设置状态
     */

    public void setStatus(Integer status)
    {
        this.status = status;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取ErrorMsg
     * @return
     * 返回值: String
     * 返回值说明：获取ErrorMsg
     */

    public String getErrorMsg()
    {
        return errorMsg;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置ErrorMsg
     * @param errorMsg errorMsg参数
     * @return
     * 返回值: void
     * 返回值说明：设置ErrorMsg
     */

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取操作时间
     * @return
     * 返回值: Date
     * 返回值说明：获取操作时间
     */

    public Date getOperTime()
    {
        return operTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置操作时间
     * @param operTime 操作时间参数
     * @return
     * 返回值: void
     * 返回值说明：设置操作时间
     */

    public void setOperTime(Date operTime)
    {
        this.operTime = operTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Cost时间
     * @return
     * 返回值: Long
     * 返回值说明：获取Cost时间
     */

    public Long getCostTime()
    {
        return costTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Cost时间
     * @param costTime cost时间参数
     * @return
     * 返回值: void
     * 返回值说明：设置Cost时间
     */

    public void setCostTime(Long costTime)
    {
        this.costTime = costTime;
    }
}
