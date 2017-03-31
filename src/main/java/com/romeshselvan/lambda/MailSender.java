package main.java.com.romeshselvan.lambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import java.util.Collections;
import java.util.Map;

/**
 * @author Romesh Selvan
 */
public class MailSender implements RequestHandler<DynamodbEvent, Void> {

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        event.getRecords().forEach(record -> {
            if(record.getEventName().equals("INSERT")) {
                Map<String, AttributeValue> recordMap = record.getDynamodb().getKeys();
                String email = recordMap.get("email").getS();
                String firstName = recordMap.get("firstName").getS();
                Boolean attending = recordMap.get("areAttending").getBOOL();
                String numberAttending = recordMap.get("numberAttending").getN();

                String message = getMessage(firstName, attending, numberAttending);

                AmazonSimpleEmailService service =
                        AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.EU_WEST_1).build();

                service.sendEmail(new SendEmailRequest("romeshselvan@hotmail.co.uk",
                        new Destination(Collections.singletonList(email)),
                        new Message(
                                new Content("RSVP Confirmation - Romesh & Charmikha"),
                                new Body(new Content(message))
                        )
                ));
            }
        });
        return null;
    }

    private String getMessage(String firstName, boolean areAttending, String numberAttending) {
        return String.format(
            "Hey %s\n"
            + "\n"
            + "Thank you for letting us know whether you are coming to the reception.\n"
            + "\n"
            + "Attending : %s\n"
            + "Number of people Attending : %s\n"
            + "\n"
            + "If you would like to make any changes, please contact us as soon as possible.\n"
            + "\n"
            + "Kind Regards,\n" + "\n"
            + "Romesh & Charmikha",
            firstName, getAttendanceString(areAttending), numberAttending);
    }

    private String getAttendanceString(boolean attending) {
        return (attending) ? "Yes" : "No";
    }
}
