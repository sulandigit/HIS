package com.neu.his.api.controller.dms;

import com.neu.his.common.api.CommonResult;
import com.neu.his.common.dto.dms.DmsCaseHistoryParam;
import com.neu.his.common.dto.dms.DmsCaseHistoryResult;
import com.neu.his.dms.service.DmsCaseHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 病历管理控制器
 * 提供病历的创建、查询、确诊和诊毕等功能
 *
 * @author HIS Team
 */
@Slf4j
@RestController
@Api(tags = "DmsCaseHistoryController", description = "病历管理")
@RequestMapping("/caseHistory")
@CrossOrigin(allowCredentials = "true")
public class DmsCaseHistoryController {

    /**
     * 病历状态常量
     */
    private static final int STATUS_PRELIMINARY = 1; // 初诊状态
    private static final int STATUS_CONFIRMED = 2;   // 确诊状态
    private static final int STATUS_ENDED = 3;       // 已结束状态
    private static final int OPERATION_SUCCESS = 1;  // 操作成功标识

    @Autowired
    private DmsCaseHistoryService dmsCaseHistoryService;

    /**
     * 提交初诊信息
     *
     * @param dmsCaseHistoryParam 病历参数，包含初诊相关信息
     * @return 操作结果
     */
    @ApiOperation(value = "提交初诊信息")
    @PostMapping("/submitPreliminaryDiagnosis")
    public CommonResult<Integer> submitPreliminaryDiagnosis(@RequestBody DmsCaseHistoryParam dmsCaseHistoryParam) {
        log.info("提交初诊信息，挂号ID: {}", dmsCaseHistoryParam.getRegistrationId());
        try {
            int result = dmsCaseHistoryService.insertPriliminaryDise(dmsCaseHistoryParam);
            return handleOperationResult(result, "提交初诊信息");
        } catch (Exception e) {
            log.error("提交初诊信息失败，挂号ID: {}", dmsCaseHistoryParam.getRegistrationId(), e);
            return CommonResult.failed("提交初诊信息失败: " + e.getMessage());
        }
    }

    /**
     * 提交初诊信息（兼容旧版本接口）
     * @deprecated 请使用 submitPreliminaryDiagnosis 接口
     */
    @Deprecated
    @ApiOperation(value = "提交初诊信息（已废弃，请使用submitPreliminaryDiagnosis）")
    @PostMapping("/submitPriliminaryDise")
    public CommonResult<Integer> submitPriliminaryDise(@RequestBody DmsCaseHistoryParam dmsCaseHistoryParam) {
        return submitPreliminaryDiagnosis(dmsCaseHistoryParam);
    }
    /**
     * 根据挂号ID查询未结束就诊的历史病历
     * 优先查询初诊状态的病历，若无则查询确诊状态的病历
     *
     * @param registrationId 挂号ID
     * @return 病历查询结果
     */
    @ApiOperation(value = "根据挂号ID查询未结束就诊的历史病历（含初诊和确诊信息）")
    @GetMapping("/selectNotEndCaseHistoryByReg/{registrationId}")
    public CommonResult<DmsCaseHistoryResult> selectNotEndCaseHistoryByReg(@PathVariable("registrationId") Long registrationId) {
        log.info("查询未结束病历，挂号ID: {}", registrationId);
        try {
            // 先查询初诊状态的病历
            DmsCaseHistoryResult result = dmsCaseHistoryService.selectCaseHistoryByReg(registrationId, STATUS_PRELIMINARY);
            
            // 若初诊病历为空，则查询确诊状态的病历
            if (result == null || CollectionUtils.isEmpty(result.getDmsCaseHistoryList())) {
                log.debug("初诊病历为空，尝试查询确诊状态病历，挂号ID: {}", registrationId);
                result = dmsCaseHistoryService.selectCaseHistoryByReg(registrationId, STATUS_CONFIRMED);
            }
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("查询未结束病历失败，挂号ID: {}", registrationId, e);
            return CommonResult.failed("查询病历失败: " + e.getMessage());
        }
    }


    /**
     * 根据挂号ID查询已结束就诊的历史病历
     * 用于病历首页显示完整的诊疗信息
     *
     * @param registrationId 挂号ID
     * @return 病历查询结果
     * @author 赵煜
     */
    @ApiOperation(value = "根据挂号ID查询已结束就诊的历史病历（病历首页显示完整诊疗信息）")
    @GetMapping("/selectEndCaseHistoryByReg/{registrationId}")
    public CommonResult<DmsCaseHistoryResult> selectEndCaseHistoryByReg(@PathVariable("registrationId") Long registrationId) {
        log.info("查询已结束病历，挂号ID: {}", registrationId);
        try {
            DmsCaseHistoryResult result = dmsCaseHistoryService.selectCaseHistoryByReg(registrationId, STATUS_ENDED);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("查询已结束病历失败，挂号ID: {}", registrationId, e);
            return CommonResult.failed("查询病历失败: " + e.getMessage());
        }
    }


    /**
     * 提交门诊确诊信息
     *
     * @param dmsCaseHistoryParam 病历参数，包含确诊诊断等信息
     * @return 操作结果
     */
    @ApiOperation(value = "提交门诊确诊信息")
    @PostMapping("/submitDefiniteDiagnosis")
    public CommonResult<Integer> submitDefiniteDiagnosis(@RequestBody DmsCaseHistoryParam dmsCaseHistoryParam) {
        log.info("提交确诊信息，挂号ID: {}", dmsCaseHistoryParam.getRegistrationId());
        try {
            int result = dmsCaseHistoryService.submitDefiniteDise(dmsCaseHistoryParam);
            return handleOperationResult(result, "提交确诊信息");
        } catch (Exception e) {
            log.error("提交确诊信息失败，挂号ID: {}", dmsCaseHistoryParam.getRegistrationId(), e);
            return CommonResult.failed("提交确诊信息失败: " + e.getMessage());
        }
    }

    /**
     * 提交门诊确诊信息（兼容旧版本接口）
     * @deprecated 请使用 submitDefiniteDiagnosis 接口
     */
    @Deprecated
    @ApiOperation(value = "提交门诊确诊信息（已废弃，请使用submitDefiniteDiagnosis）")
    @PostMapping("/submitDefiniteDise")
    public CommonResult<Integer> submitDefiniteDise(@RequestBody DmsCaseHistoryParam dmsCaseHistoryParam) {
        return submitDefiniteDiagnosis(dmsCaseHistoryParam);
    }
    /**
     * 诊毕操作
     * 标记患者本次就诊结束
     *
     * @param dmsCaseHistoryParam 病历参数
     * @return 操作结果
     */
    @ApiOperation(value = "诊毕操作")
    @PostMapping("/endDiagnosis")
    public CommonResult<Integer> endDiagnosis(@RequestBody DmsCaseHistoryParam dmsCaseHistoryParam) {
        log.info("诊毕操作，挂号ID: {}", dmsCaseHistoryParam.getRegistrationId());
        try {
            int result = dmsCaseHistoryService.endDiagnosis(dmsCaseHistoryParam);
            return handleOperationResult(result, "诊毕操作");
        } catch (Exception e) {
            log.error("诊毕操作失败，挂号ID: {}", dmsCaseHistoryParam.getRegistrationId(), e);
            return CommonResult.failed("诊毕操作失败: " + e.getMessage());
        }
    }

    /**
     * 处理操作结果的通用方法
     *
     * @param result 操作返回结果
     * @param operationName 操作名称
     * @return 统一响应结果
     */
    private CommonResult<Integer> handleOperationResult(int result, String operationName) {
        if (result == OPERATION_SUCCESS) {
            log.info("{}成功", operationName);
            return CommonResult.success(result, operationName + "成功");
        } else {
            log.warn("{}失败，返回码: {}", operationName, result);
            return CommonResult.failed(operationName + "失败");
        }
    }
}
