package com.neu.his.cloud.service.sms.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.neu.his.cloud.service.sms.constant.RedisKeyConstants;
import com.neu.his.cloud.service.sms.dto.sms.SmsDeptParam;
import com.neu.his.cloud.service.sms.dto.sms.SmsDeptResult;
import com.neu.his.cloud.service.sms.mapper.SmsDeptMapper;
import com.neu.his.cloud.service.sms.model.SmsDept;
import com.neu.his.cloud.service.sms.model.SmsDeptExample;
import com.neu.his.cloud.service.sms.service.SmsDeptService;
import com.neu.his.cloud.service.sms.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *  科室
 */
@Service
public class SmsDeptServiceImpl implements SmsDeptService {

    private static final Logger logger = LoggerFactory.getLogger(SmsDeptServiceImpl.class);

    @Autowired
    private SmsDeptMapper smsDeptMapper;
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 描述:1.调用SmsDeptDao根据code查询科室是否存在
     * 2.1.如果不存在则向SmsDeptDao插入则插入数据，并返回1
     * 2.2.如果存在则返回值0
     * <p>author: ma
     * <p>author: 赵煜 修改科室新增的问题（不能新增同名科室）
     * <p>优化: 采用Cache Aside模式，插入成功后直接删除缓存
     */
    @Override
    public int create(SmsDeptParam smsDeptParam){
        SmsDept smsDept = new SmsDept();
        BeanUtils.copyProperties(smsDeptParam, smsDept);
        smsDept.setStatus(1);
        //查询是否有同code科室
        SmsDeptExample example = new SmsDeptExample();
        example.createCriteria().andNameEqualTo(smsDept.getName()).andStatusNotEqualTo(0);
        List<SmsDept> SmsDeptList = smsDeptMapper.selectByExample(example);
        if (SmsDeptList.size() > 0) {
            return 0;
        }

        //没有则插入数据
        int result = smsDeptMapper.insert(smsDept);
        
        //插入成功，删除缓存以触发重建
        if (result > 0) {
            redisUtil.delete(RedisKeyConstants.Dept.ALL);
            logger.info("科室新增成功，已删除缓存: {}", RedisKeyConstants.Dept.ALL);
        }
        
        return result;
    }

    @Override
    public int delete(List<Long> ids){
        SmsDept smsDept = new SmsDept();
        smsDept.setStatus(0);
        SmsDeptExample example = new SmsDeptExample();
        example.createCriteria().andIdIn(ids);

        int result = smsDeptMapper.updateByExampleSelective(smsDept, example);
        
        //删除成功，删除缓存以触发重建
        if (result > 0) {
            redisUtil.delete(RedisKeyConstants.Dept.ALL);
            logger.info("科室删除成功，已删除缓存: {}", RedisKeyConstants.Dept.ALL);
        }
        
        return result;
    }

    @Override
    public int update(Long id,SmsDeptParam smsDeptParam){
        SmsDept smsDept = new SmsDept();
        BeanUtils.copyProperties(smsDeptParam, smsDept);
        smsDept.setId(id);

        int result = smsDeptMapper.updateByPrimaryKeySelective(smsDept);
        
        //修改成功，删除缓存以触发重建
        if (result > 0) {
            redisUtil.delete(RedisKeyConstants.Dept.ALL);
            logger.info("科室更新成功，已删除缓存: {}", RedisKeyConstants.Dept.ALL);
        }
        
        return result;
    }

    @Override
    public List<SmsDeptResult> select(SmsDeptParam smsDeptQueryParam){

        SmsDeptExample example = new SmsDeptExample();
        SmsDeptExample.Criteria criteria = example.createCriteria();
        //如果没有指明state，返回不为0的
        if(smsDeptQueryParam.getStatus() == null){
            criteria.andStatusNotEqualTo(0);
        }
        //是否按编码、名称、分类、类型查询
        if(!StringUtils.isEmpty(smsDeptQueryParam.getCode())){
            criteria.andCodeLike("%" + smsDeptQueryParam.getCode() + "%");
        }
        if(!StringUtils.isEmpty(smsDeptQueryParam.getName())){
            criteria.andNameLike("%" + smsDeptQueryParam.getName() + "%");
        }
        if(smsDeptQueryParam.getCatId() != null){
            criteria.andCatIdEqualTo(smsDeptQueryParam.getCatId());
        }
        if(smsDeptQueryParam.getType() != null){
            criteria.andTypeEqualTo(smsDeptQueryParam.getType());
        }
        //返回数据包装成Result
        example.setOrderByClause("id desc");
        List<SmsDept> smsDepts = smsDeptMapper.selectByExample(example);
        List<SmsDeptResult> smsDeptResults = new ArrayList<>();
        for (SmsDept s : smsDepts) {
            SmsDeptResult r = new SmsDeptResult();
            BeanUtils.copyProperties(s, r);
            smsDeptResults.add(r);
        }
        return smsDeptResults;
    }

    @Override
    public List<SmsDeptResult> selectAll(){
        //先从redis中查询
        List<SmsDeptResult> resultList = (List<SmsDeptResult>)redisUtil.getObj(RedisKeyConstants.Dept.ALL);
        if(resultList != null && !resultList.isEmpty()){
            logger.info("从Redis缓存中获取全部科室数据，key: {}", RedisKeyConstants.Dept.ALL);
            return resultList;
        }

        logger.info("缓存未命中，从数据库查询全部科室数据");
        //缓存未命中，从数据库查询
        SmsDeptExample example = new SmsDeptExample();
        example.createCriteria().andStatusNotEqualTo(0);
        List<SmsDept> smsDepts = smsDeptMapper.selectByExample(example);
        List<SmsDeptResult> smsDeptResults = new ArrayList<>();
        for (SmsDept s : smsDepts) {
            SmsDeptResult r = new SmsDeptResult();
            BeanUtils.copyProperties(s, r);
            smsDeptResults.add(r);
        }

        //写入缓存，设置24小时过期时间
        if (!smsDeptResults.isEmpty()) {
            redisUtil.setObj(RedisKeyConstants.Dept.ALL, smsDeptResults, 
                RedisKeyConstants.ExpireTime.BASE_DATA, TimeUnit.SECONDS);
            logger.info("已将科室数据写入缓存，key: {}, 过期时间: {}秒", 
                RedisKeyConstants.Dept.ALL, RedisKeyConstants.ExpireTime.BASE_DATA);
        }

        return smsDeptResults;
    }

}
