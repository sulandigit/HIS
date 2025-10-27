package com.neu.his.cloud.service.dms.controller;


import com.neu.his.cloud.service.dms.common.CommonResult;
import com.neu.his.cloud.service.dms.constant.RedisKeyConstants;
import com.neu.his.cloud.service.dms.dto.dms.DmsCaseHistoryParam;
import com.neu.his.cloud.service.dms.dto.dms.DmsDrugRedisParam;
import com.neu.his.cloud.service.dms.dto.dms.DmsNonDrugRedisParam;
import com.neu.his.cloud.service.dms.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@Api(tags = "DmsRedisSaveController", description = "redis暂存管理")
@RequestMapping("/redisSave")
@CrossOrigin(allowCredentials = "true")
public class DmsRedisSaveController {
    private static Logger LOGGER = LoggerFactory.getLogger(DmsRedisSaveController.class);

    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation(value = "暂存病历首页（根据挂号号）")
    @RequestMapping(value = "/saveCasePage", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult saveCasePage(@RequestBody DmsCaseHistoryParam dmsCaseHistoryParam,
                                     @RequestParam("registrationId") Long registrationId){
        String cacheKey = RedisKeyConstants.CaseDraft.getKey(registrationId);
        redisUtil.setObj(cacheKey, dmsCaseHistoryParam, 
            RedisKeyConstants.ExpireTime.TEMP_DRAFT, TimeUnit.SECONDS);
        LOGGER.info("redis暂存成功，key: {}, data: {}", cacheKey, dmsCaseHistoryParam.toString());
        return CommonResult.success("暂存成功");
    }

    @ApiOperation(value = "取出病历首页（根据挂号号）")
    @RequestMapping(value = "/getCasePage", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<DmsCaseHistoryParam> getCasePage(@RequestParam("registrationId") Long registrationId){
        String cacheKey = RedisKeyConstants.CaseDraft.getKey(registrationId);
        DmsCaseHistoryParam param = (DmsCaseHistoryParam)redisUtil.getObj(cacheKey);
        if(param == null){
            LOGGER.info("redis取出失败，key: {}", cacheKey);
            return CommonResult.success(null);
        }
        LOGGER.info("redis取出成功，key: {}, data: {}", cacheKey, param.toString());
        return CommonResult.success(param);
    }



    @ApiOperation(value = "暂存药方（根据挂号号和类型（4成药、5草药））")
    @RequestMapping(value = "/saveDrugPrescription", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult saveDrugPrescription(@RequestBody List<DmsDrugRedisParam> dmsDrugRedisParam,
                                             @RequestParam("registrationId") Long registrationId,
                                             @RequestParam("type") int type){
        String cacheKey = RedisKeyConstants.Prescription.getKey(registrationId, type);
        redisUtil.setObj(cacheKey, dmsDrugRedisParam, 
            RedisKeyConstants.ExpireTime.TEMP_DRAFT, TimeUnit.SECONDS);
        LOGGER.info("redis暂存成功，key: {}, data: {}", cacheKey, dmsDrugRedisParam.toString());
        return CommonResult.success("暂存成功");
    }

    @ApiOperation(value = "取出药方（根据挂号号和类型（4成药、5草药））")
    @RequestMapping(value = "/getDrugPrescription", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<DmsDrugRedisParam>> getDrugPrescription(@RequestParam("registrationId") Long registrationId,
                                                                     @RequestParam("type") int type){
        String cacheKey = RedisKeyConstants.Prescription.getKey(registrationId, type);
        List<DmsDrugRedisParam> paramList = (List<DmsDrugRedisParam>)redisUtil.getObj(cacheKey);
        if(paramList == null){
            LOGGER.info("redis取出失败，key: {}", cacheKey);
            return CommonResult.success(null);
        }
        LOGGER.info("redis取出成功，key: {}, data: {}", cacheKey, paramList.toString());
        return CommonResult.success(paramList);
    }




    @ApiOperation(value = "暂存开立非药品项目（根据挂号号和类型（0检查、1检验、2处置））")
    @RequestMapping(value = "/saveNonDrug", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult saveNonDrug(@RequestBody DmsNonDrugRedisParam dmsNonDrugRedisParam,
                                    @RequestParam("registrationId") Long registrationId,
                                    @RequestParam("type") int type){
        String cacheKey = RedisKeyConstants.NonDrugTemp.getKey(registrationId, type);
        redisUtil.setObj(cacheKey, dmsNonDrugRedisParam, 
            RedisKeyConstants.ExpireTime.TEMP_DRAFT, TimeUnit.SECONDS);
        LOGGER.info("redis暂存成功，key: {}, data: {}", cacheKey, dmsNonDrugRedisParam.toString());
        return CommonResult.success("暂存成功");
    }

    @ApiOperation(value = "取出开立非药品项目（根据挂号号和类型（0检查、1检验、2处置））")
    @RequestMapping(value = "/getNonDrug", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<DmsNonDrugRedisParam> getNonDrug(@RequestParam("registrationId") Long registrationId,
                                                         @RequestParam("type") int type){
        String cacheKey = RedisKeyConstants.NonDrugTemp.getKey(registrationId, type);
        DmsNonDrugRedisParam paramList = (DmsNonDrugRedisParam)redisUtil.getObj(cacheKey);
        if(paramList == null){
            LOGGER.info("redis取出失败，key: {}", cacheKey);
            return CommonResult.success(null);
        }
        LOGGER.info("redis取出成功，key: {}, data: {}", cacheKey, paramList.toString());
        return CommonResult.success(paramList);
    }
}
