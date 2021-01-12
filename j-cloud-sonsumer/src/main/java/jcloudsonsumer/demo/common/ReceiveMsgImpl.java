package jcloudsonsumer.demo.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

    /**
     *
     * @ClassName:：ReceiveMsgImpl
     * @author ：zyc
     * @date ：
     */
    @Component
    @EnableBinding(value = { Sink.class })
    public class ReceiveMsgImpl implements ReceiveMsg {

        /**
         * 引入日志，注意都是"org.slf4j"包下
         */
        private final static Logger logger = LoggerFactory.getLogger(ReceiveMsgImpl.class);

        @Override
        @StreamListener(Sink.INPUT)
        public void receiveTime(Message<String> message) {
            logger.info("接收消息" + message.getPayload().toString());
        }

    }

