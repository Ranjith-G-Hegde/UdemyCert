package kafka.demo;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PDemoWithCallback {
static Logger logger = LoggerFactory.getLogger(PDemoWithCallback.class);
    public static void main(String args[]){
        Properties properties = new Properties();
properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());


        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);

        ProducerRecord<String,String> record = new ProducerRecord<String, String>("first_topic","hello world");

        producer.send(record, new Callback() {
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
             //executes when message is sent
             if(e ==null){
                logger.info("Recieved new metadata. \n "+"Topic:"+recordMetadata.topic()+"\n"
                + "Partition:" + recordMetadata.partition() + "\n"+
                        "Offset:"+recordMetadata.offset() + "\n"+
                        "Timestamp:" + recordMetadata.timestamp()
                );
             }else {
                 logger.error("Error while producing");
             }
            }
        });

        producer.flush();
        producer.close();
    }
}
