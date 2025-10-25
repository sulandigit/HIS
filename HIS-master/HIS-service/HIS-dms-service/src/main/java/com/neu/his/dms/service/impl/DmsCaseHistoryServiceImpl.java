package com.neu.his.dms.service.impl;

import com.neu.his.common.dto.dms.DmsCaseHistoryParam;
import com.neu.his.common.dto.dms.DmsCaseHistoryResult;
import com.neu.his.dms.service.DmsCaseHistoryService;
import com.neu.his.mbg.mapper.*;
import com.neu.his.mbg.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DmsCaseHistoryServiceImpl implements DmsCaseHistoryService {
    
    private static final int STATUS_PRELIMINARY = 1;  // 初诊结束
    private static final int STATUS_DEFINITE = 2;     // 确诊结束
    private static final int STATUS_FINISHED = 3;     // 诊毕
    
    private static final int TYPE_CHECK = 0;          // 检查
    private static final int TYPE_TEST = 1;           // 检验
    private static final int TYPE_DISPOSITION = 2;    // 处置
    
    private final DmsCaseHistoryMapper dmsCaseHistoryMapper;
    private final DmsRegistrationMapper dmsRegistrationMapper;
    private final DmsNonDrugItemRecordMapper dmsNonDrugItemRecordMapper;
    private final DmsNonDrugMapper dmsNonDrugMapper;
    private final DmsHerbalPrescriptionRecordMapper dmsHerbalPrescriptionRecordMapper;
    private final DmsHerbalItemRecordMapper dmsHerbalItemRecordMapper;
    private final DmsMedicinePrescriptionRecordMapper dmsMedicinePrescriptionRecordMapper;
    private final DmsMedicineItemRecordMapper dmsMedicineItemRecordMapper;
    private final DmsDrugMapper dmsDrugMapper;

    /**
     * 初诊
     * @param dmsCaseHistoryParam 初诊参数
     * @return 执行结果
     */
    @Override
    public int insertPriliminaryDise(DmsCaseHistoryParam dmsCaseHistoryParam) {
        if (dmsCaseHistoryParam == null) {
            return 0;
        }
        
        DmsCaseHistory dmsCaseHistory = new DmsCaseHistory();
        BeanUtils.copyProperties(dmsCaseHistoryParam, dmsCaseHistory);
        
        Long registrationId = dmsCaseHistoryParam.getRegistrationId();
        DmsRegistration registration = dmsRegistrationMapper.selectByPrimaryKey(registrationId);
        
        dmsCaseHistory.setRegistrationId(registrationId);
        dmsCaseHistory.setPatientId(registration.getPatientId());
        dmsCaseHistory.setStatus(STATUS_PRELIMINARY);
        dmsCaseHistory.setCreateTime(new Date());
        
        dmsCaseHistoryMapper.insertSelective(dmsCaseHistory);
        return 1;
    }
    /**
     * 根据挂号ID和状态查询病历
     * @param registrationId 挂号ID
     * @param status 状态 (1-初诊结束, 2-确诊结束, 3-诊毕)
     * @return 病历结果
     */
    @Override
    public DmsCaseHistoryResult selectCaseHistoryByReg(Long registrationId, Integer status) {
        DmsRegistration dmsRegistration = dmsRegistrationMapper.selectByPrimaryKey(registrationId);
        if (dmsRegistration == null) {
            return null;
        }

        DmsCaseHistoryExample example = new DmsCaseHistoryExample();
        example.createCriteria()
               .andPatientIdEqualTo(dmsRegistration.getPatientId())
               .andStatusEqualTo(status);
        example.setOrderByClause("create_time desc");
        
        List<DmsCaseHistory> caseHistoryList = dmsCaseHistoryMapper.selectByExample(example);
        
        DmsCaseHistoryResult result = new DmsCaseHistoryResult();
        result.setDmsCaseHistoryList(caseHistoryList);
        return result;
    }
    /**
     * 确诊
     * @param dmsCaseHistoryParam 确诊参数
     * @return 执行结果
     */
    @Override
    public int submitDefiniteDise(DmsCaseHistoryParam dmsCaseHistoryParam) {
        if (dmsCaseHistoryParam == null) {
            return 0;
        }
        
        Long registrationId = dmsCaseHistoryParam.getRegistrationId();
        DmsCaseHistoryExample example = new DmsCaseHistoryExample();
        example.createCriteria().andRegistrationIdEqualTo(registrationId);
        
        DmsCaseHistory dmsCaseHistory = new DmsCaseHistory();
        dmsCaseHistory.setCheckResult(dmsCaseHistoryParam.getCheckResult());
        dmsCaseHistory.setTestResult(dmsCaseHistoryParam.getTestResult());
        dmsCaseHistory.setDefiniteDiseStrList(dmsCaseHistoryParam.getDefiniteDiseStrList());
        dmsCaseHistory.setCheckStrList(resolveNonDrugItemRecord(registrationId, TYPE_CHECK));
        dmsCaseHistory.setTestStrList(resolveNonDrugItemRecord(registrationId, TYPE_TEST));
        dmsCaseHistory.setStatus(STATUS_DEFINITE);
        
        dmsCaseHistoryMapper.updateByExampleSelective(dmsCaseHistory, example);
        return 1;
    }
    /**
     * 诊毕
     * @param dmsCaseHistoryParam 诊毕参数
     * @return 执行结果
     */
    @Override
    public int endDiagnosis(DmsCaseHistoryParam dmsCaseHistoryParam) {
        if (dmsCaseHistoryParam == null) {
            return 0;
        }
        
        Long registrationId = dmsCaseHistoryParam.getRegistrationId();
        
        // 更新病历记录
        DmsCaseHistoryExample example = new DmsCaseHistoryExample();
        example.createCriteria().andRegistrationIdEqualTo(registrationId);
        
        DmsCaseHistory dmsCaseHistory = new DmsCaseHistory();
        dmsCaseHistory.setDispositionStrList(resolveNonDrugItemRecord(registrationId, TYPE_DISPOSITION));
        dmsCaseHistory.setHerbalPrescriptionStrList(resolveHerbalPrescription(registrationId));
        dmsCaseHistory.setMedicinePrescriptionStrList(resolveMedicinePrescription(registrationId));
        dmsCaseHistory.setStatus(STATUS_FINISHED);
        
        dmsCaseHistoryMapper.updateByExampleSelective(dmsCaseHistory, example);

        // 更新挂号表状态为诊毕
        DmsRegistration registration = new DmsRegistration();
        registration.setId(registrationId);
        registration.setStatus(STATUS_FINISHED);
        dmsRegistrationMapper.updateByPrimaryKeySelective(registration);

        return 1;
    }


    /**
     * 解析非药品项目记录
     * @param registrationId 挂号ID
     * @param type 类型 (0-检查, 1-检验, 2-处置)
     * @return 格式化后的字符串
     */
    private String resolveNonDrugItemRecord(Long registrationId, Integer type) {
        DmsNonDrugItemRecordExample example = new DmsNonDrugItemRecordExample();
        example.createCriteria().andRegistrationIdEqualTo(registrationId).andTypeEqualTo(type);
        List<DmsNonDrugItemRecord> recordList = dmsNonDrugItemRecordMapper.selectByExample(example);
        
        StringBuilder result = new StringBuilder();
        for (DmsNonDrugItemRecord record : recordList) {
            DmsNonDrug nonDrug = dmsNonDrugMapper.selectByPrimaryKey(record.getNoDrugId());
            String nonDrugName = nonDrug.getName();
            
            if (type == TYPE_CHECK || type == TYPE_TEST) {
                // 检查、检验
                result.append(nonDrugName).append("<>")
                      .append(record.getCheckParts()).append("<>")
                      .append(record.getCheckResult()).append("<>")
                      .append(record.getResultImgUrlList()).append("><");
            } else if (type == TYPE_DISPOSITION) {
                // 处置
                result.append(nonDrugName).append("><");
            }
        }
        return result.toString();
    }

    /**
     * 解析草药处方记录
     * @param registrationId 挂号ID
     * @return 格式化后的字符串
     */
    private String resolveHerbalPrescription(Long registrationId) {
        DmsHerbalPrescriptionRecordExample example = new DmsHerbalPrescriptionRecordExample();
        example.createCriteria().andRegistrationIdEqualTo(registrationId);
        List<DmsHerbalPrescriptionRecord> prescriptionList = dmsHerbalPrescriptionRecordMapper.selectByExample(example);
        
        StringBuilder result = new StringBuilder();
        for (DmsHerbalPrescriptionRecord prescription : prescriptionList) {
            result.append(prescription.getName()).append("[");
            
            DmsHerbalItemRecordExample itemExample = new DmsHerbalItemRecordExample();
            itemExample.createCriteria().andPrescriptionIdEqualTo(prescription.getId());
            List<DmsHerbalItemRecord> itemList = dmsHerbalItemRecordMapper.selectByExample(itemExample);
            
            for (DmsHerbalItemRecord item : itemList) {
                DmsDrug drug = dmsDrugMapper.selectByPrimaryKey(item.getDrugId());
                result.append(drug.getName()).append("<<>>")
                      .append(item.getTotalNum()).append("><");
            }
            
            result.append("]<>");
        }
        return result.toString();
    }

    /**
     * 解析成药处方记录
     * @param registrationId 挂号ID
     * @return 格式化后的字符串
     */
    private String resolveMedicinePrescription(Long registrationId) {
        DmsMedicinePrescriptionRecordExample example = new DmsMedicinePrescriptionRecordExample();
        example.createCriteria().andRegistrationIdEqualTo(registrationId);
        List<DmsMedicinePrescriptionRecord> prescriptionList = dmsMedicinePrescriptionRecordMapper.selectByExample(example);
        
        StringBuilder result = new StringBuilder();
        for (DmsMedicinePrescriptionRecord prescription : prescriptionList) {
            result.append(prescription.getName()).append("[");
            
            DmsMedicineItemRecordExample itemExample = new DmsMedicineItemRecordExample();
            itemExample.createCriteria().andPrescriptionIdEqualTo(prescription.getId());
            List<DmsMedicineItemRecord> itemList = dmsMedicineItemRecordMapper.selectByExample(itemExample);
            
            for (DmsMedicineItemRecord item : itemList) {
                DmsDrug drug = dmsDrugMapper.selectByPrimaryKey(item.getDrugId());
                result.append(drug.getName()).append("<<>>")
                      .append(item.getNum()).append("><");
            }
            
            result.append("]<>");
        }
        return result.toString();
    }
}
