package com.neu.his.api.app.controller;

import com.neu.his.bms.service.BmsFeeQueryService;
import com.neu.his.common.api.CommonResult;
import com.neu.his.common.dto.app.AppCheckTestResult;
import com.neu.his.common.dto.app.AppDeptDescriptionResult;
import com.neu.his.common.dto.app.AppDrugItemResult;
import com.neu.his.common.dto.app.AppHistoryRegResult;
import com.neu.his.common.dto.dms.DmsRegHistoryResult;
import com.neu.his.dms.service.DmsNonDrugItemRecordService;
import com.neu.his.dms.service.DmsRegistrationService;
import com.neu.his.sms.SmsDescriptionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * APP挂号历史信息管理控制器
 * 
 * 功能说明：
 * 1. 提供APP端查询患者历史挂号信息的接口
 * 2. 提供查询挂号相关费用明细的接口
 * 3. 提供查询检查检验结果的接口
 * 4. 提供查询科室描述信息的接口
 * 
 * @author ma
 * @version 1.0
 */
@Controller
@Api(tags = "AppHistoryController", description = "APP 挂号历史信息管理")
@RequestMapping("/appRegHistory")
@CrossOrigin(allowCredentials = "true")
public class AppHistoryController {

    /**
     * 挂号服务接口
     * 用于处理挂号相关的业务逻辑
     */
    @Autowired
    private DmsRegistrationService dmsRegistrationService;

    /**
     * 费用查询服务接口
     * 用于查询处方费用、药品费用等信息
     */
    @Autowired
    private BmsFeeQueryService bmsFeeQueryService;

    /**
     * 非药品项目记录服务接口
     * 用于处理检查检验等非药品项目相关业务
     */
    @Autowired
    private DmsNonDrugItemRecordService dmsNonDrugItemRecordService;

    /**
     * 描述信息服务接口
     * 用于获取科室描述等信息
     */
    @Autowired
    private SmsDescriptionService smsDescriptionService;

    /**
     * 查询历史挂号信息列表
     * 
     * 功能描述：
     * 根据患者身份证号查询该患者的所有历史挂号记录
     * 
     * @param identificationNo 患者身份证号，必填参数
     * @return 包含历史挂号信息列表的通用结果对象
     *         - 成功时返回挂号历史记录列表
     *         - 列表中包含挂号ID、挂号时间、科室、医生等信息
     * @author ma
     */
    @ApiOperation("查询历史挂号信息")
    @RequestMapping(value = "/listAllRegistration", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<DmsRegHistoryResult>> listAllRegistration(@RequestParam("identificationNo") String identificationNo){
        List<DmsRegHistoryResult> list = dmsRegistrationService.listRegHistory(identificationNo);
        return CommonResult.success(list);
    }

    /**
     * 查看某次挂号费用大项
     * 
     * 功能描述：
     * 根据挂号ID查询该次挂号产生的所有费用大项（处方级别）
     * 大项包括：药品处方、检查处方、检验处方等
     * 
     * @param registrationId 挂号ID，必填参数
     * @return 包含费用大项列表的通用结果对象
     *         - 成功时返回处方级别的费用信息
     *         - 列表中包含处方ID、处方类型、总金额等信息
     * @author ma
     */
    @ApiOperation("查询某次挂号费用（大项）")
    @RequestMapping(value = "/listFee", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<AppHistoryRegResult>> listFee(@RequestParam("registrationId") Long registrationId){
        List<AppHistoryRegResult> feeResultList = bmsFeeQueryService.listFeePrescription(registrationId);
        return CommonResult.success(feeResultList);
    }

    /**
     * 查看某次挂号费用小项
     * 
     * 功能描述：
     * 根据处方ID和类型查询该处方下的具体费用明细（项目级别）
     * 小项包括：具体的药品、检查项目、检验项目等明细信息
     * 
     * @param prescriptionId 处方ID，必填参数，指定要查询的处方
     * @param type 处方类型，必填参数
     *             - 1: 药品处方
     *             - 2: 检查处方
     *             - 3: 检验处方
     * @return 包含费用明细列表的通用结果对象
     *         - 成功时返回具体项目的费用详情
     *         - 列表中包含项目名称、单价、数量、金额等信息
     * @author ma
     */
    @ApiOperation("查询某次挂号费用（小项）")
    @RequestMapping(value = "/listDetail", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<AppDrugItemResult>> listFeeDetail(@RequestParam("prescriptionId") Long prescriptionId ,
                                                               @RequestParam("type") int type){
        List<AppDrugItemResult> itemResultList = bmsFeeQueryService.listFeeItem(prescriptionId,type);
        return CommonResult.success(itemResultList);
    }

    /**
     * 查看检查检验结果
     * 
     * 功能描述：
     * 根据非药品项目记录ID查询检查或检验的详细结果
     * 适用于患者查看自己的检查报告、检验报告等
     * 
     * @param id 非药品项目记录ID，必填参数
     *           - 对应检查或检验项目的记录ID
     * @return 包含检查检验结果的通用结果对象
     *         - 成功时返回检查检验的详细结果信息
     *         - 包含项目名称、检查结果、参考值等
     * @author ma
     */
    @ApiOperation("查询检查检验结果")
    @RequestMapping(value = "/getResult", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<AppCheckTestResult> getResult(@RequestParam("id") Long id){
        AppCheckTestResult result = dmsNonDrugItemRecordService.appGetResult(id);
        return CommonResult.success(result);
    }

    /**
     * 查询所有科室描述
     * 
     * 功能描述：
     * 获取医院所有科室的描述信息
     * 用于APP端展示科室介绍、科室特色、诊疗范围等信息
     * 
     * @return 包含科室描述信息列表的通用结果对象
     *         - 成功时返回所有科室的描述信息
     *         - 列表中包含科室名称、科室简介、科室特色等
     * @author ma
     */
    @ApiOperation("查询所有科室描述")
    @RequestMapping(value = "/getDeptDescription", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<AppDeptDescriptionResult>> getDeptDescription(){
        return CommonResult.success(smsDescriptionService.getDeptDescription());
    }

}
