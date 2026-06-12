package com.asp.transactionservice.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // Set the message converter so RabbitTemplate serializes/deserializes JSON correctly
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        rabbitTemplate.setReplyTimeout(5000);
        // IMPORTANT: do NOT send messages here (no convertAndSend)
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);

        return factory;
    }
}





//package com.asp.transactionservice.config;
//
///*
// * Copyright (c) 2025 Ayshi Shannidhya Panda. All rights reserved.
// *
// * This source code is confidential and intended solely for internal use.
// * Unauthorized copying, modification, distribution, or disclosure of this
// * file, via any medium, is strictly prohibited.
// *
// * Project: NPT
// * Author: Ayshi Shannidhya Panda
// * Created on: 17-09-2025
// */
//
//
//import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    @Bean
//    public Jackson2JsonMessageConverter messageConverter(){
//        return new Jackson2JsonMessageConverter();
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
//                                         Jackson2JsonMessageConverter jackson2JsonMessageConverter){
//
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.convertAndSend(jackson2JsonMessageConverter);
//        rabbitTemplate.setReplyTimeout(5000);
//
//        return rabbitTemplate;
//    }
//
//    @Bean
//    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(
//            ConnectionFactory connectionFactory,
//            Jackson2JsonMessageConverter jackson2JsonMessageConverter){
//
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//
//        factory.setConnectionFactory(connectionFactory);
//        factory.setMessageConverter(jackson2JsonMessageConverter);
//        factory.setConcurrentConsumers(3);
//        factory.setMaxConcurrentConsumers(10);
//
//        return factory;
//    }
//
//}
