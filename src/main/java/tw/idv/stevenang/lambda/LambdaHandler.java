package tw.idv.stevenang.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.sns.message.SnsNotification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class LambdaHandler implements RequestHandler<SQSEvent, Void> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        log.info("Start processing sqs events...");
        sqsEvent.getRecords()
                .stream()
                .forEach(message -> {
                    try {
                        // This will get the message main body from the SQS message
                        JsonNode recordNode = mapper.readTree(message.getBody());
                        // The original message contains some characters which will causing error when parse to json. Need to clean the message first
                        String messageJson = recordNode.get("Message").asText().replace("\\", "").replace("\"\"", "\"");
                        // Parser the message into a json node
                        JsonNode messageNode = mapper.readTree(messageJson);
                        // Get records from the message node
                        messageNode.get("Records")
                                .elements()
                                .forEachRemaining(record -> {
                                    // Get s3 element from the record
                                    JsonNode s3 = record.get("s3");
                                    // Retrieve the bucket and object information
                                    JsonNode bucket = s3.get("bucket");
                                    JsonNode object = s3.get("object");
                                    System.out.println("Object: " + object.get("key").asText() + " was created in " + bucket.get("name").asText() + " bucket.");
                                });
                     } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });

        return null;
    }
}
