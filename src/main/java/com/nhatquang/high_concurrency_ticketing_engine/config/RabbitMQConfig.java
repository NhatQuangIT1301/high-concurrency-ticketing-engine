package com.nhatquang.high_concurrency_ticketing_engine.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class RabbitMQConfig {
    
    // Khai báo cho Main Queue
    public static final String ORDER_QUEUE = "order.queue";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_ROUTING_KEY = "order.routing.key";

    // Khai báo cho Dead Letter Queue
    public static final String ORDER_DLQ = "order.dlq";
    public static final String ORDER_DLX = "order.dlx";
    public static final String ORDER_DLQ_ROUTING_KEY = "order.dlq.routing.key";

    // Khai báo cho Order Delay Queue
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay.routing.key";

    // --- CẤU HÌNH DEAD LETTER QUEUE (HÀNG ĐỢI CHỨA LỖI) ---

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(ORDER_DLQ, true);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(ORDER_DLX);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(ORDER_DLQ_ROUTING_KEY);
    }

    // --- CẤU HÌNH MAIN QUEUE ---

    @Bean 
    public Queue orderQueue() {
        // Tham số 'true' để cấu hình durable (sống sót khi RabbitMQ restart)
        // Sử dụng QueueBuilder để gài các tham số DLX vào Main Queue
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ORDER_ROUTING_KEY);
    }

    // --- CẤU HÌNH CHO DELAYED MESSAGE (ISSUE 9) ---
    @Bean
    public Queue orderDelayQueue() {
        return new Queue(ORDER_DELAY_QUEUE, true);
    }

    @Bean
    public CustomExchange orderDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        // Khai báo kiểu exchange nội tại mà plugin sẽ sử dụng để định tuyến
        args.put("x-delayed-type", "direct");

        // "x-delayed-message" là tham số bắt buộc để kích hoạt plugin của RabbitMQ
        return new CustomExchange(ORDER_DELAY_EXCHANGE, "x-delayed-message", true, false, args);
    }

    @Bean
    public Binding delayBinding() {
        // Dùng phương thức noargs() vì CustomExchange trả về kiểu dữ liệu hơi khác một chút
        return BindingBuilder.bind(orderDelayQueue())
            .to(orderDelayExchange())
            .with(ORDER_DELAY_ROUTING_KEY)
            .noargs();
    }

    // --- CẤU HÌNH MESSAGE CONVERTER ---

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
