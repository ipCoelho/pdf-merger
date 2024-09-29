package br.com.hinova.powerpdf.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PDF_MERGE_QUEUE = "pdf.merge.queue";
    public static final String PDF_MERGE_EXCHANGE = "pdf.merge.exchange";
    public static final String PDF_MERGE_ROUTING_KEY = "pdf.merge.routingkey";

    @Bean
    public Queue pdfMergeQueue() {
        return new Queue(PDF_MERGE_QUEUE, true);
    }

    @Bean
    public DirectExchange pdfMergeExchange() {
        return new DirectExchange(PDF_MERGE_EXCHANGE);
    }

    @Bean
    public Binding pdfMergeBinding(Queue pdfMergeQueue, DirectExchange pdfMergeExchange) {
        return BindingBuilder.bind(pdfMergeQueue).to(pdfMergeExchange).with(PDF_MERGE_ROUTING_KEY);
    }
}
