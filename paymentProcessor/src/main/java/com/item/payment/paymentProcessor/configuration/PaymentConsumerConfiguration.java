package com.item.payment.paymentProcessor.configuration;

import com.item.payment.paymentProcessor.exception.ItemNotAvailableInStorage;
import com.item.payment.paymentProcessor.exception.LowItemQuantityException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaymentConsumerConfiguration {

    private final Logger log= LoggerFactory.getLogger(PaymentConsumerConfiguration.class);
    private PaymentKafkaConfiguration paymentKafkaConfiguration;

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Value("${item.retryTopic}")
    private String retryTopic;

    @Value("${item.deadTopic}")
    private String deadLetterTopic;

    public PaymentConsumerConfiguration(PaymentKafkaConfiguration paymentKafkaConfiguration) {
        this.paymentKafkaConfiguration = paymentKafkaConfiguration;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory()
    {
        Map<String, Object> properties= new HashMap<>();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, paymentKafkaConfiguration.getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, paymentKafkaConfiguration.getKeyDeserializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, paymentKafkaConfiguration.getValueDeserializer());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, paymentKafkaConfiguration.getAutoOffsetReset());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, paymentKafkaConfiguration.getGroupId());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, paymentKafkaConfiguration.isEnableAutoCommit());
        properties.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CooperativeStickyAssignor.class.getName());

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, String> factory= new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);
        factory.setCommonErrorHandler(defaultErrorHandler());
        return factory;
    }

    /*
     * Error handler it will retry for the 2 times in interval of the 1-Second
     * @return DefaultErrorHandler
     * */
    @Bean
    public DefaultErrorHandler defaultErrorHandler()
    {
        FixedBackOff backOff= new FixedBackOff(1000l, 1);
        DefaultErrorHandler errorHandler= new DefaultErrorHandler( publishRecover(), backOff);
        errorHandler.setRetryListeners((consumerRecord, e, i) -> log.info("The exception is occurred while retrying {} and retry attempt {}", e.getMessage(), i));
        return errorHandler;
    }

    public DeadLetterPublishingRecoverer publishRecover()
    {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (r, e) -> {
                    if (e.getCause() instanceof LowItemQuantityException || e.getCause() instanceof ItemNotAvailableInStorage) {
                        log.info("Sending message into the Retry Topic: {} for exception: {}",retryTopic, e.getCause());
                        return new TopicPartition(retryTopic,  r.partition());
                    }
                    else {
                        log.info("Sending message into the DeadLetter Topic: {} for exception: {}",deadLetterTopic, e.getCause());
                        return new TopicPartition(deadLetterTopic , r.partition());
                    }
                });
    }


}
