package kafka.twitter;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import kafka.DataBaseAssault.DBApp;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());
    String consumerKey = "7E2Vm1p4RmGyFaHbe2cq082Rh";
    String consumerSecret = "JhsPVlUneJFizpLY2uCz3ongHwSqa7hLbhDtaWRp9QMJbN3xBO";
    String  token = "1143437967953809408-vabNwLz908GbQ2OcqNbyyW1nVxD4WO";
    String  tokenSecret = "gXfxIIRJfwW8lTZgRM1bcrhiiMPTv2HvfMYo6RfjRv1Ry";

 public static void main(String Args[]){
     new TwitterProducer().run();
 }



    public TwitterProducer(){
    }



    public void run()  {

     logger.info("Setting you up");
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

        Client hosebirdClient = createTwitterClient(msgQueue);
        hosebirdClient.connect();
        DBApp.initialize();

        KafkaProducer<String,String> producer = createKafkaProducer();
Runtime.getRuntime().addShutdownHook(new Thread(() ->{

    logger.info("Stopping application");
    logger.info("Disconnecting twitter client");
    hosebirdClient.stop();
    logger.info("closing producer");
producer.close();
logger.info("Done!");
}  ));

        String msg = null;
        while (!hosebirdClient.isDone()) {
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                hosebirdClient.stop();
            }
            if (msg != null) {

                int start = msg.indexOf("\"text\":\"");
                start = start + 8;
                int end = msg.indexOf("\"",start);


                int start2 = msg.indexOf("\"name\":\"");
                start2 = start2 + 8;
                int end2 = msg.indexOf("\"",start2);
               // logger.info(msg.substring(start,end));

                DBApp.callToActor(msg.substring(start2,end2),msg.substring(start,end));
         producer.send(new ProducerRecord<>("twitter_tweets", null, msg), new Callback() {
             @Override
             public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                 if(e!=null)
                     logger.error("Something bad happened", e);
             }
         });
            }
        }
        logger.info("end of application..");
    }

    private KafkaProducer<String, String> createKafkaProducer() {

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());


        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);

        return producer;
 }

    public Client createTwitterClient(BlockingQueue<String> msgQueue){
/** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
// Optional: set up some followings and track terms
        //List<Long> followings = Lists.newArrayList(1234L, 566788L);
        List<String> terms = Lists.newArrayList("kafka");
       // hosebirdEndpoint.followings(followings);
        hosebirdEndpoint.trackTerms(terms);

// These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, tokenSecret);

//        Creating a client:

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client hosebirdClient = builder.build();
        return hosebirdClient;
    }

}

