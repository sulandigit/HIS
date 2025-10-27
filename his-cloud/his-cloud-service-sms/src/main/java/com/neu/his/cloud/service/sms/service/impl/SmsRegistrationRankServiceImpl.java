package com.neu.his.cloud.service.sms.service.impl;

import cn.hutool.core.collection.CollectionUtil;

import com.neu.his.cloud.service.sms.constant.RedisKeyConstants;
import com.neu.his.cloud.service.sms.dto.sms.SmsRegistrationRankParam;
import com.neu.his.cloud.service.sms.dto.sms.SmsRegistrationRankResult;
import com.neu.his.cloud.service.sms.mapper.SmsRegistrationRankMapper;
import com.neu.his.cloud.service.sms.model.SmsRegistrationRank;
import com.neu.his.cloud.service.sms.model.SmsRegistrationRankExample;
import com.neu.his.cloud.service.sms.service.SmsRegistrationRankService;
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
 * 挂号级别
 */
@Service
public class SmsRegistrationRankServiceImpl implements SmsRegistrationRankService {

    private static final Logger logger = LoggerFactory.getLogger(SmsRegistrationRankServiceImpl.class);

    @Autowired
    private SmsRegistrationRankMapper smsRegistrationRankMapper;
    @Autowired
    private RedisUtil redisUtil;


    @Override
    public int create(SmsRegistrationRankParam smsRegistrationRankParam){
        SmsRegistrationRank smsRegistrationRank = new SmsRegistrationRank();
        BeanUtils.copyProperties(smsRegistrationRankParam, smsRegistrationRank);
        smsRegistrationRank.setStatus(1);
        //查询是否有状态非0且名字相同的挂号级别
        SmsRegistrationRankExample example = new SmsRegistrationRankExample();
        example.createCriteria().andNameEqualTo(smsRegistrationRank.getName()).andStatusNotEqualTo(0);
        List<SmsRegistrationRank> lists = smsRegistrationRankMapper.selectByExample(example);
        if (lists.size() > 0) {
            return 0;
        }

        //没有则插入数据
        int result = smsRegistrationRankMapper.insert(smsRegistrationRank);
        
        //插入成功，删除缓存以触发重建
        if (result > 0) {
            redisUtil.delete(RedisKeyConstants.RegistrationRank.ALL);
            logger.info("挂号级别新增成功，已删除缓存: {}", RedisKeyConstants.RegistrationRank.ALL);
        }
        
        return result;
    }

    @Override
    public int delete(List<Long> ids){
        SmsRegistrationRank smsRegistrationRank = new SmsRegistrationRank();
        smsRegistrationRank.setStatus(0);
        SmsRegistrationRankExample example = new SmsRegistrationRankExample();
        example.createCriteria().andIdIn(ids);

        int result = smsRegistrationRankMapper.updateByExampleSelective(smsRegistrationRank, example);
        
        //删除成功，删除缓存以触发重建
        if (result > 0) {
            redisUtil.delete(RedisKeyConstants.RegistrationRank.ALL);
            logger.info("挂号级别删除成功，已删除缓存: {}", RedisKeyConstants.RegistrationRank.ALL);
        }
        
        return result;
    }

    @Override
    public int update(Long id,SmsRegistrationRankParam smsRegistrationRankParam){
        SmsRegistrationRank smsRegistrationRank = new SmsRegistrationRank();
        BeanUtils.copyProperties(smsRegistrationRankParam, smsRegistrationRank);
        smsRegistrationRank.setId(id);

        int result = smsRegistrationRankMapper.updateByPrimaryKeySelective(smsRegistrationRank);
        
        //更新成功，删除缓存以触发重建
        if (result > 0) {
            redisUtil.delete(RedisKeyConstants.RegistrationRank.ALL);
            logger.info("挂号级别更新成功，已删除缓存: {}", RedisKeyConstants.RegistrationRank.ALL);
        }
        
        return result;
    }

    @Override
    public List<SmsRegistrationRankResult> select(SmsRegistrationRankParam smsRegistrationRankParam){
        SmsRegistrationRankExample example = new SmsRegistrationRankExample();
        SmsRegistrationRankExample.Criteria criteria = example.createCriteria();
        //如果没有指明state，返回不为0的
        if(smsRegistrationRankParam.getStatus() == null){
            criteria.andStatusNotEqualTo(0);
        }
        //是否按编码、名称、价格、显示顺序号查询

        if(!StringUtils.isEmpty(smsRegistrationRankParam.getCode())){
            criteria.andCodeLike("%" + smsRegistrationRankParam.getCode() + "%");
        }
        if(!StringUtils.isEmpty(smsRegistrationRankParam.getName())){
            criteria.andNameLike("%" + smsRegistrationRankParam.getName() + "%");
        }
        if(smsRegistrationRankParam.getPrice() != null){
            criteria.andPriceEqualTo(smsRegistrationRankParam.getPrice());
        }
        if(smsRegistrationRankParam.getSeqNo() != null){
            criteria.andSeqNoEqualTo(smsRegistrationRankParam.getSeqNo());
        }
        //返回数据包装成Result
        example.setOrderByClause("id desc");
        List<SmsRegistrationRank> smsRegistrationRankResults=smsRegistrationRankMapper.selectByExample(example);
        List<SmsRegistrationRankResult> returnList = new ArrayList<>();
        for (SmsRegistrationRank s : smsRegistrationRankResults) {
            SmsRegistrationRankResult r = new SmsRegistrationRankResult();
            BeanUtils.copyProperties(s, r);
            returnList.add(r);
        }
        return returnList;
    }

    @Override
    public List<SmsRegistrationRankResult> selectAll(){
        //先从redis中查询
        List<SmsRegistrationRankResult> resultList = (List<SmsRegistrationRankResult>)redisUtil.getObj(RedisKeyConstants.RegistrationRank.ALL);
        if(resultList != null && !resultList.isEmpty()){
            logger.info("从Redis缓存中获取全部挂号级别数据，key: {}", RedisKeyConstants.RegistrationRank.ALL);
            return resultList;
        }

        logger.info("缓存未命中，从数据库查询全部挂号级别数据");
        // 缓存未命中，在数据库中查找
        SmsRegistrationRankExample example = new SmsRegistrationRankExample();
        example.createCriteria().andStatusNotEqualTo(0);
        List<SmsRegistrationRank> smsRegistrationRank = smsRegistrationRankMapper.selectByExample(example);
        List<SmsRegistrationRankResult> returnList = new ArrayList<>();
        for (SmsRegistrationRank s : smsRegistrationRank) {
            SmsRegistrationRankResult r = new SmsRegistrationRankResult();
            BeanUtils.copyProperties(s, r);
            returnList.add(r);
        }

        //写入缓存，设置24小时过期时间
        if (!returnList.isEmpty()) {
            redisUtil.setObj(RedisKeyConstants.RegistrationRank.ALL, returnList, 
                RedisKeyConstants.ExpireTime.BASE_DATA, TimeUnit.SECONDS);
            logger.info("已将挂号级别数据写入缓存，key: {}, 过期时间: {}秒", 
                RedisKeyConstants.RegistrationRank.ALL, RedisKeyConstants.ExpireTime.BASE_DATA);
        }

        return returnList;
    }

}
