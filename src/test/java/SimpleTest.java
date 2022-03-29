import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.soya.consent.ConsentHandle;
import org.soya.consent.Matching;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class SimpleTest {

    static JsonObject scenario;

    @BeforeAll
    public static void init() throws IOException {

        InputStream consentIS = SimpleTest.class.getClassLoader().getResourceAsStream("consent/consent_handling.json");
        scenario = Json.createReader(consentIS).readObject();
        consentIS.close();
    }

    @Test
    public void simpleTest1() throws Exception {
        JsonObject originalConsent = scenario.getJsonObject("user-consent").getJsonObject("permissive");
        JsonObject originalHandling = scenario.getJsonObject("data-handling").getJsonObject("permissive");

        Model consent = ConsentHandle.INSTANCE.getPolicy(ConsentHandle.CONSENT, originalConsent);
        Model handling = ConsentHandle.INSTANCE.getPolicy(ConsentHandle.HANDLING, originalHandling);

        StringWriter consentWriter = new StringWriter();
        StringWriter handlingWriter = new StringWriter();

        consent.write(consentWriter, Lang.TURTLE.getName());
        consent.write(System.out, Lang.TURTLE.getName());
        String consentString = consentWriter.toString();

        handling.write(handlingWriter, Lang.TURTLE.getName());
        handling.write(System.out, Lang.TURTLE.getName());
        String handlingString = handlingWriter.toString();

        Assertions.assertTrue(Matching.matchingString(consentString, handlingString, ConsentHandle.CONSENT, ConsentHandle.HANDLING));
    }

    @Test
    public void simpleTest2() throws Exception {
        JsonObject originalConsent = scenario.getJsonObject("user-consent").getJsonObject("with-array");
        JsonObject originalHandling = scenario.getJsonObject("data-handling").getJsonObject("permissive");

        Model consent = ConsentHandle.INSTANCE.getPolicy(ConsentHandle.CONSENT, originalConsent);
        Model handling = ConsentHandle.INSTANCE.getPolicy(ConsentHandle.HANDLING, originalHandling);

        StringWriter consentWriter = new StringWriter();
        StringWriter handlingWriter = new StringWriter();

        consent.write(consentWriter, Lang.TURTLE.getName());
        consent.write(System.out, Lang.TURTLE.getName());
        String consentString = consentWriter.toString();

        handling.write(handlingWriter, Lang.TURTLE.getName());
        handling.write(System.out, Lang.TURTLE.getName());
        String handlingString = handlingWriter.toString();

        Assertions.assertFalse(Matching.matchingString(consentString, handlingString, ConsentHandle.CONSENT, ConsentHandle.HANDLING));
    }

    @Test
    public void simpleTest3() throws Exception {
        JsonObject originalConsent = scenario.getJsonObject("user-consent").getJsonObject("permissive");
        JsonObject originalHandling = scenario.getJsonObject("data-handling").getJsonObject("with-array");

        Model consent = ConsentHandle.INSTANCE.getPolicy(ConsentHandle.CONSENT, originalConsent);
        Model handling = ConsentHandle.INSTANCE.getPolicy(ConsentHandle.HANDLING, originalHandling);

        StringWriter consentWriter = new StringWriter();
        StringWriter handlingWriter = new StringWriter();

        consent.write(consentWriter, Lang.TURTLE.getName());
        consent.write(System.out, Lang.TURTLE.getName());
        String consentString = consentWriter.toString();

        handling.write(handlingWriter, Lang.TURTLE.getName());
        handling.write(System.out, Lang.TURTLE.getName());
        String handlingString = handlingWriter.toString();

        Assertions.assertTrue(Matching.matchingString(consentString, handlingString, ConsentHandle.CONSENT, ConsentHandle.HANDLING));
    }

    @Test
    public void simpleTest4() throws Exception {
        JsonObject originalConsent = scenario.getJsonObject("user-consent").getJsonObject("with-array");
        JsonObject originalHandling = scenario.getJsonObject("data-handling").getJsonObject("with-array");

        Model consent = ConsentHandle.INSTANCE.getPolicy(ConsentHandle.CONSENT, originalConsent);
        Model handling = ConsentHandle.INSTANCE.getPolicy(ConsentHandle.HANDLING, originalHandling);

        StringWriter consentWriter = new StringWriter();
        StringWriter handlingWriter = new StringWriter();

        consent.write(consentWriter, Lang.TURTLE.getName());
        consent.write(System.out, Lang.TURTLE.getName());
        String consentString = consentWriter.toString();

        handling.write(handlingWriter, Lang.TURTLE.getName());
        handling.write(System.out, Lang.TURTLE.getName());
        String handlingString = handlingWriter.toString();

        Assertions.assertTrue(Matching.matchingString(consentString, handlingString, ConsentHandle.CONSENT, ConsentHandle.HANDLING));
    }
}
