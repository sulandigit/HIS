package com.neu.his.cloud.service.dms.component;

import com.neu.his.cloud.service.dms.service.DmsFeeRabbitMQService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ订单消息接收器
 * 用于监听并处理订单取消队列中的超时消息
 * 监听队列：his.order.cancel
 */
@Component
@RabbitListener(queues = "his.order.cancel")
public class RabbitMQOrderReceiver {
    /** 日志记录器 */
    private static Logger LOGGER = LoggerFactory.getLogger(RabbitMQOrderReceiver.class);

    /** 药品管理系统费用相关的RabbitMQ服务 */
    @Autowired
    private DmsFeeRabbitMQService dmsFeeRabbitMQService;

    /**
     * 处理订单超时消息
     * 消息格式：id&type
     * 
     * @param msg 消息内容，格式为"订单ID&类型"，例如："123&4"
     *            类型说明：
     *            - 4: 中药处方单
     *            - 5: 西药处方单
     *            - 其他: 非药品项目
     */
    @RabbitHandler
    public void handle(String msg){
        LOGGER.info("receive order message msg:{}",msg);
        // 解析消息，按&分割
        String[] temp = msg.split("&");
        // 验证消息格式是否正确
        if(temp.length != 2){
            LOGGER.warn("处方单超时处理失败： msg:{}",msg);
            return;
        }
        // 提取订单ID和类型
        Long id = new Long(temp[0]);
        int type = Integer.parseInt(temp[1]);
        int handleCount = 0;
        // 根据不同类型处理超时订单
        if(type == 4){
            // 处理中药处方单超时
            handleCount = dmsFeeRabbitMQService.herbalOutOfTime(id);
        }else if(type == 5){
            // 处理西药处方单超时
            handleCount = dmsFeeRabbitMQService.medicineOutOfTime(id);
        }else{
            // 处理非药品项目超时
            handleCount = dmsFeeRabbitMQService.nonDrugOutOfTime(id);
        }
        System.out.println("处理结果:" + handleCount);
    }

}
