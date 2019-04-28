package com.leyou.page.mq;

import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YuTian
 * @date 2019/4/22 8:27
 */
@Component
public class ItemListener {

    @Autowired
    private PageService pageService;

    @RabbitListener(bindings = @QueueBinding(
                 value = @Queue(name = "page.item.insert.queue",durable = "true"),
                 exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
                   key = {"item.insert","item.update"}
            )
    )
    public void ItemInsertOrUpdate(Long id){
        if (id == null) {
            return;
        }
        // 创建页面
        pageService.createHtml(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.delete.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    )
    )
    public void ItemDelete(Long id){
        if (id == null) {
            return;
        }
        // 创建页面
        pageService.deleteHtml(id);
    }


}
