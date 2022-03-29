package org.soya.consent;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soya.consent.InstanceHandle.InstantType;
import org.soya.consent.vocab.DPV;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public enum ConsentHandle {

    INSTANCE;

    public static final String NS_INSTANCE = "http://example.org/instance-ns#";
    public static final String HANDLING = "http://example.org/id/handling";
    public static final String CONSENT = "http://example.org/id/consent";

    private static final Logger log = LoggerFactory.getLogger(ConsentHandle.class);
    public Model dpvModel = ModelFactory.createDefaultModel();

    ConsentHandle() {
        if (dpvModel.isEmpty()) {
            InputStream is = ConsentHandle.class.getClassLoader().getResourceAsStream("dpv-wellfort.ttl");
            RDFDataMgr.read(dpvModel, is, Lang.TURTLE);
        }
    }

    public Model getPolicy(String uri, JsonObject consent) throws UnregisteredTermException {
        Resource handlingResource = InstanceHandle.createInstance(uri);
        return getModel(consent, handlingResource);
    }

    public Model getExperimentDataHandling(String experimentId, JsonObject consent) throws UnregisteredTermException {
        Resource handlingResource = InstanceHandle.createInstance(NS_INSTANCE, experimentId, InstantType._EXPERIMENT);
        return getModel(consent, handlingResource);
    }

    public Model getUserConsent(String datasetId, JsonObject consent) throws Exception {
        Resource consentResource = InstanceHandle.createInstance(NS_INSTANCE, datasetId, InstantType._CONSENT);
        return getModel(consent, consentResource);
    }

    private Model getModel(JsonObject consent, Resource consentResource) throws UnregisteredTermException {

        String data = getJsonString(consent, "dpv:hasPersonalData");
        String processing = getJsonString(consent, "dpv:hasProcessing");
        String purpose = getJsonString(consent, "dpv:hasPurpose");
        String recipient = getJsonString(consent, "dpv:hasRecipient");
        String location = getJsonString(consent, "dpv:hasLocation");
        String expiry = consent.getString("dpv:hasExpiryTime");

        return getConsent(consentResource, data, processing, purpose, recipient, location, expiry);
    }

    private String getJsonString(JsonObject consent, String property) {
        JsonValue value = consent.getValue("/" + property);
        if (value.getValueType().equals(JsonValue.ValueType.ARRAY)) {
            JsonArray array = consent.getJsonArray(property);
            StringJoiner joiner = new StringJoiner(",");
            array.forEach(val -> joiner.add(val.toString().replace("\"", "")));

            return joiner.toString();
        } else {
            return consent.getString(property);
        }
    }

    private Model getConsent(Resource consent, String data, String processing, String purpose, String recipient, String location, String expiry)
            throws UnregisteredTermException {
        Model consentModel = ModelFactory.createDefaultModel();
        consentModel.setNsPrefix("dpv", DPV.NS);
        consentModel.setNsPrefix("owl", OWL.NS);
        consentModel.setNsPrefix("xsd", XSD.NS);

        Resource equivClass = consentModel.createResource();

        Resource bnodeData =
                setRestriction(consentModel, data, DPV.HAS_PERSONAL_DATA_CATEGORY, DPV.PERSONAL_DATA_CATEGORY);
        Resource bnodeProcessing = setRestriction(consentModel, processing, DPV.HAS_PROCESSING, DPV.PROCESSING);
        Resource bnodePurpose = setRestriction(consentModel, purpose, DPV.HAS_PURPOSE, DPV.PURPOSE);
        Resource bnodeRecipient = setRestriction(consentModel, recipient, DPV.HAS_RECIPIENT, DPV.RECIPIENT);
        Resource bnodeLocation = setRestriction(consentModel, location, DPV.HAS_LOCATION, DPV.LOCATION);
        Resource bnodeExpiryTime = setExpiryDate(consentModel, expiry);

        RDFList policyList = consentModel.createList().with(bnodeData);
        policyList.add(bnodeProcessing);
        policyList.add(bnodePurpose);
        policyList.add(bnodeRecipient);
        policyList.add(bnodeLocation);
        policyList.add(bnodeExpiryTime);
        equivClass.addProperty(OWL.intersectionOf, policyList);

        consentModel.add(consent, RDF.type, OWL.Class);
        consentModel.add(consent, OWL.equivalentClass, equivClass);

        return consentModel;
    }

    private Resource setRestriction(Model consentModel, String input, Property property, Resource defaultValue)
            throws UnregisteredTermException {

        Resource restriction = consentModel.createResource();
        Resource value = getRestrictionValue(consentModel, input, defaultValue);

        restriction.addProperty(RDF.type, OWL.Restriction);
        restriction.addProperty(OWL.onProperty, property);
        restriction.addProperty(OWL.someValuesFrom, value);

        return restriction;
    }

    private Resource setExpiryDate(Model consentModel, String expiryDate) {

        Resource restriction = consentModel.createResource();

        Resource maxInclusive = consentModel.createResource();
        Literal dateTime = consentModel.createTypedLiteral(expiryDate, XSD.dateTime.getURI());
        maxInclusive.addProperty(consentModel.createProperty(XSD.NS, "maxInclusive"), dateTime);

        Resource value = consentModel.createResource();
        value.addProperty(RDF.type, RDFS.Datatype);
        value.addProperty(OWL2.onDatatype, XSD.dateTime);
        value.addProperty(OWL2.withRestrictions, consentModel.createList(maxInclusive));

        restriction.addProperty(RDF.type, OWL.Restriction);
        restriction.addProperty(OWL.onProperty, DPV.EXPIRY_TIME);
        restriction.addProperty(OWL.someValuesFrom, value);

        return restriction;
    }

    private Resource getRestrictionValue(Model consentModel, String input, Resource defaultValue) throws UnregisteredTermException {
        Resource restrictionValue;

        if (input.contains(",")) {
            List<String> values = Arrays.asList(input.trim().split("\\s*,\\s*"));
            restrictionValue = consentModel.createResource();
            RDFList unionOf = null;
            for (int i = 0; i < values.size(); i++) {
                Resource value = checkExistingResource(values.get(i), defaultValue);
                if (unionOf == null) {
                    unionOf = consentModel.createList().with(value);
                } else {
                    unionOf.add(value);
                }
            }
            restrictionValue.addProperty(OWL.unionOf, unionOf);
        } else {
            restrictionValue = checkExistingResource(input, defaultValue);
        }

        return restrictionValue;
    }

    private Resource checkExistingResource(String input, Resource defaultValue) throws UnregisteredTermException {
        Resource restrictionValue;

        if (input.isEmpty()) {
            restrictionValue = defaultValue; // empty means default!
        } else {
            Resource resDPV;
            if (input.startsWith("dpv:")) {
                String inputValue = input.split(":")[1];
                resDPV = dpvModel.createResource(DPV.NS + inputValue);
            } else {
                resDPV = dpvModel.createResource(DPV.NS + input);
            }
            if (dpvModel.containsResource(resDPV)) {
                restrictionValue = resDPV;
            } else {
                throw new UnregisteredTermException("'" + input + "' is not registered as terms of consent");
            }
        }

        return restrictionValue;
    }

}
