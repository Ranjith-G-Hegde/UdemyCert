package kafka.demo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.omg.SendingContext.RunTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class CDemoWithThread {
    public static void main(String args[]) {
        Properties properties = new Properties();
        CountDownLatch latch = new CountDownLatch(1);
        LoggerFactory.getLogger(CDemoWithThread.class.getName()).info("Creating the consumer..");
        Runnable myConsumerThread = new ConsumerThread(latch);
        Thread myThread = new Thread(myConsumerThread);
        myThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            LoggerFactory.getLogger(CDemoWithThread.class.getName()).info("caught shutdown hook..");
            ((ConsumerThread) myConsumerThread).shutDown();
            }

        ));
        try {
            latch.await();
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(CDemoWithThread.class.getName()).error("exception occured..");

        } finally {
            LoggerFactory.getLogger(CDemoWithThread.class.getName()).info("closing the application..");

        }
    }

}

     class ConsumerThread implements Runnable{
private CountDownLatch latch;
        private Logger logger = LoggerFactory.getLogger(ConsumerThread.class.getName());

private KafkaConsumer<String,String> consumer;
        public ConsumerThread(CountDownLatch latch){
this.latch = latch;
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,"my-5-app");
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        logger.info("creatng consumer thread");
            KafkaConsumer<String,String> consumer = new KafkaConsumer<String, String>(properties);


            consumer.subscribe(Collections.singleton("first_topic"));
        }

        @Override
        public void run() {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Key: " + record.key() + " Value:" + record.value());
                        logger.info("Partition:" + record.partition() + " Offset:" + record.offset());
                    }
                }

            } catch (WakeupException e) {
        logger.info("Recieved shutdown signal!");
            } finally {
                consumer.close();
                //done with consumer
                latch.countDown();
            }

        }
        public void shutDown(){
        consumer.wakeup();
        }
    }

