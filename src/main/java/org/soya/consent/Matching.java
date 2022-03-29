package org.soya.consent;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.stream.Stream;

public class Matching {

    private static final Logger log = LoggerFactory.getLogger(Matching.class);
    private static final String dpv = "dpv-wellfort.ttl";

    public static boolean matchingJenaModel(Model consent, Model handling, String consentURI, String handlingURI) {


        return true;
    }

    public static boolean matchingString(String consent, String handling, String consentURI, String handlingURI) {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();

        try {

            InputStream libraryIS = Matching.class.getClassLoader().getResourceAsStream(dpv);
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(libraryIS);

            InputStream consentIS = IOUtils.toInputStream(consent, "UTF-8");
            ontology.addAxioms(manager.loadOntologyFromOntologyDocument(consentIS).axioms());
            consentIS.close();

            InputStream handlingIS = IOUtils.toInputStream(handling, "UTF-8");
            ontology.addAxioms(manager.loadOntologyFromOntologyDocument(handlingIS).axioms());
            handlingIS.close();

            OWLClass dataControllerCls = df.getOWLClass(IRI.create(handlingURI));
            OWLClass dataSubjectCls = df.getOWLClass(IRI.create(consentURI));

            OWLReasonerFactory rf = new ReasonerFactory();
            OWLReasoner r = rf.createReasoner(ontology);
            OWLAxiom axiom = df.getOWLSubClassOfAxiom(dataControllerCls, dataSubjectCls);

            return r.isEntailed(axiom);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    public static boolean matching(String inputFile, String consentURI, String handlingURI, String dpv) {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();

        try {

            InputStream libraryIS = Matching.class.getClassLoader().getResourceAsStream(dpv);
            InputStream inputIS = Matching.class.getClassLoader().getResourceAsStream(inputFile);

            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputIS);
            ontology.addAxioms(manager.loadOntologyFromOntologyDocument(libraryIS).axioms());
            OWLClass dataControllerCls = df.getOWLClass(IRI.create(handlingURI));
            OWLClass dataSubjectCls = df.getOWLClass(IRI.create(consentURI));

            OWLReasonerFactory rf = new ReasonerFactory();
            OWLReasoner r = rf.createReasoner(ontology);
            OWLAxiom axiom = df.getOWLSubClassOfAxiom(dataControllerCls, dataSubjectCls);

            return r.isEntailed(axiom);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    public static boolean matching(String inputFile, String consentURI, String handlingURI) {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();

        try {

            InputStream libraryIS = Matching.class.getClassLoader().getResourceAsStream(dpv);
            InputStream inputIS = Matching.class.getClassLoader().getResourceAsStream(inputFile);

            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputIS);
            ontology.addAxioms(manager.loadOntologyFromOntologyDocument(libraryIS).axioms());
            OWLClass dataControllerCls = df.getOWLClass(IRI.create(handlingURI));
            OWLClass dataSubjectCls = df.getOWLClass(IRI.create(consentURI));

            OWLReasonerFactory rf = new ReasonerFactory();
            OWLReasoner r = rf.createReasoner(ontology);
            OWLAxiom axiom = df.getOWLSubClassOfAxiom(dataControllerCls, dataSubjectCls);

            return r.isEntailed(axiom);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    public static boolean loadAndMatch(String inputFile, String handlingURI) {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();

        try {

            log.info("start loadAndMatch");

            InputStream libraryIS = Matching.class.getClassLoader().getResourceAsStream(dpv);
            InputStream inputIS = Matching.class.getClassLoader().getResourceAsStream(inputFile);

            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputIS);
            ontology.addAxioms(manager.loadOntologyFromOntologyDocument(libraryIS).axioms());

            OWLClass dataControllerCls = df.getOWLClass(IRI.create(handlingURI));
            OWLReasonerFactory rf = new ReasonerFactory();
            OWLReasoner r = rf.createReasoner(ontology);

            Stream<OWLClass> subclasses = r.subClasses(dataControllerCls, false);
            subclasses.forEach(subclass -> log.info("cls: " + subclass.toStringID()));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    public static void main(String args[]) throws FileNotFoundException {
        InputStream is = new FileInputStream("output/500.ttl");
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, is, Lang.TURTLE);
        RDFDataMgr.write(new FileOutputStream("output/500-formatted.ttl"), model, Lang.TURTLE);
    }
}