package com.fengpb.conductor.dao;

import com.fengpb.conductor.core.event.queue.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MySQLQueueMapper {


    @Insert("INSERT INTO queue_message (deliver_on" +
            ", queue_name, message_id" +
            " VALUES (TIMESTAMPADD(SECOND,null,CURRENT_TIMESTAMP), #{queueName}, #{messageId})" +
            " ON DUPLICATE KEY UPDATE payload=VALUES(payload)" +
            ", deliver_on=VALUES(deliver_on)")
    void push(String queueName, String messageId);

    @Delete("DELETE FROM queue_message WHERE queue_name = #{queueName} AND message_id = #{messageId}")
    boolean removeMessage(String queueName, String messageId);

    @Select("SELECT message_id as id, payload FROM queue_message use index(combo_queue_message) WHERE queue_name = #{queueName} AND popped = false AND deliver_on <= TIMESTAMPADD(MICROSECOND, 1000, CURRENT_TIMESTAMP) ORDER BY priority DESC, deliver_on, created_on LIMIT #{count}")
    List<Message> peekMessages(String queueName, int count);

    int popMessage(String queueName, List<String> ids);
}
