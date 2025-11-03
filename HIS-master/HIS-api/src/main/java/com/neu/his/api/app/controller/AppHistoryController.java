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
 * Controller for APP Registration History Management
 * Handles API endpoints for querying historical registration information, fees, and test results
 */
@Controller
@Api(tags = "AppHistoryController", description = "APP 挂号历史信息管理")
@RequestMapping("/appRegHistory")
@CrossOrigin(allowCredentials = "true")
public class AppHistoryController {

    // Service for handling registration-related operations
    @Autowired
    private DmsRegistrationService dmsRegistrationService;

    // Service for querying fee-related information
    @Autowired
    private BmsFeeQueryService bmsFeeQueryService;

    // Service for handling non-drug item records (examination and test results)
    @Autowired
    private DmsNonDrugItemRecordService dmsNonDrugItemRecordService;

    // Service for retrieving department descriptions
    @Autowired
    private SmsDescriptionService smsDescriptionService;

    /**
     * 描述:查询历史挂号信息列表
     * Description: Query the list of historical registration information
     * <p>author: ma
     */
    @ApiOperation("查询历史挂号信息")
    @RequestMapping(value = "/listAllRegistration", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<DmsRegHistoryResult>> listAllRegistration(@RequestParam("identificationNo") String identificationNo){
        // Retrieve all registration records by patient identification number
        List<DmsRegHistoryResult> list = dmsRegistrationService.listRegHistory(identificationNo);
        return CommonResult.success(list);
    }

    /**
     * 描述:查看某次挂号费用大项
     * Description: View the main fee items for a specific registration
     * <p>author: ma
     */
    @ApiOperation("查询某次挂号费用（大项）")
    @RequestMapping(value = "/listFee", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<AppHistoryRegResult>> listFee(@RequestParam("registrationId") Long registrationId){
        // Query main fee items (prescriptions) for a specific registration
        List<AppHistoryRegResult> feeResultList = bmsFeeQueryService.listFeePrescription(registrationId);
        return CommonResult.success(feeResultList);
    }

    /**
     * 描述:查看某次挂号费用小项
     * Description: View the detailed fee items for a specific registration
     * <p>author: ma
     */
    @ApiOperation("查询某次挂号费用（小项）")
    @RequestMapping(value = "/listDetail", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<AppDrugItemResult>> listFeeDetail(@RequestParam("prescriptionId") Long prescriptionId ,
                                                               @RequestParam("type") int type){
        // Query detailed fee items for a specific prescription by type
        List<AppDrugItemResult> itemResultList = bmsFeeQueryService.listFeeItem(prescriptionId,type);
        return CommonResult.success(itemResultList);
    }

    /**
     * 描述:查看检查检验结果
     * Description: View the examination and test results
     * <p>author: ma
     */
    @ApiOperation("查询检查检验结果")
    @RequestMapping(value = "/getResult", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<AppCheckTestResult> getResult(@RequestParam("id") Long id){
        // Retrieve examination and test result by record ID
        AppCheckTestResult result = dmsNonDrugItemRecordService.appGetResult(id);
        return CommonResult.success(result);
    }

    /**
     * 描述:查询所有科室描述
     * Description: Query all department descriptions
     * <p>author: ma
     */
    @ApiOperation("查询所有科室描述")
    @RequestMapping(value = "/getDeptDescription", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<AppDeptDescriptionResult>> getDeptDescription(){
        // Retrieve descriptions of all departments
        return CommonResult.success(smsDescriptionService.getDeptDescription());
    }

}
