package pl.mati.migration;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class Main {
    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();

        if (args.length < 3) {
            System.out.println("camel-migraiton <firstBrokerUri> <secondBrokerUri> <queueName>");
        }

        String firstBrokerUrl = args[0];
        String secondBrokerUrl = args[1];
        String queueName = args[2];
        ActiveMQComponent firstBroker = ActiveMQComponent.activeMQComponent(firstBrokerUrl);
        ActiveMQComponent secondBroker = ActiveMQComponent.activeMQComponent(secondBrokerUrl);

        camelContext.addComponent("activemq1", firstBroker);
        camelContext.addComponent("activemq2", secondBroker);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                errorHandler(deadLetterChannel("activemq1:deadmigration."+queueName)
                        .useOriginalMessage());

                from("activemq1:"+queueName)
                        .log(body().toString())
                        .to("activemq2:"+queueName);
            }
        });

        camelContext.start();
    }
}
