package pl.mati.migration;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();

        if (args.length < 3) {
            System.out.println("camel-migraiton <firstBrokerUri> <secondBrokerUri> <queueName> <?secondQueueName>");
            return;
        }

        String firstBrokerUrl = args[0];
        String secondBrokerUrl = args[1];
        String firstQueueName = args[2];
        String secondQueueName = args.length == 4 ? args[3] : firstQueueName;

        LOG.info("first broker: {}" ,firstBrokerUrl);
        LOG.info("second broker: {}" ,secondBrokerUrl);
        LOG.info("queue name: {}" ,firstQueueName);

        ActiveMQComponent firstBroker = ActiveMQComponent.activeMQComponent(firstBrokerUrl);
        ActiveMQComponent secondBroker = ActiveMQComponent.activeMQComponent(secondBrokerUrl);

        camelContext.addComponent("activemq1", firstBroker);
        camelContext.addComponent("activemq2", secondBroker);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                errorHandler(deadLetterChannel("activemq1:deadmigration."+firstQueueName)
                        .useOriginalMessage());

                from("activemq1:"+firstQueueName)
                        .log(body().toString())
                        .to("activemq2:"+secondQueueName);
            }
        });

        camelContext.start();
    }
}
