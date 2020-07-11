package de.unibremen.informatik.tdki.bl2020;



import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class ELReasoner {

    public ELReasoner() {
        // Nur diesen leeren Konstruktor verwenden!
    }

    public OWLOntology classify(OWLOntologyManager manager, OWLOntology ontology) throws OWLReasonerRuntimeException, NotInELNormalFormException {

        //System.out.println("Printe gesamte Onology");
        //System.out.println(ontology.toString());

        System.out.println("Alle Axiome");
        Set<OWLAxiom> ontAxs = ontology.axioms().collect(Collectors.toSet());
        for (OWLAxiom ax : ontAxs){
            printAxiomStructure(ax,false);
        }

        System.out.println("Logische Axiome");
        Set<OWLLogicalAxiom> ontLogAxs = ontology.logicalAxioms().collect(Collectors.toSet());
        for (OWLLogicalAxiom ax : ontLogAxs){
            printAxiomStructure(ax,true);
        }

        //throw new RuntimeException("TODO: Implement");

        return ontology;

    }


    static void printAxiomStructure(OWLAxiom ax,boolean printMetaDetails) {
        String[] parts = ax.toString().split(">");
        String s = "";
        int i=0;
        for(String p :parts){
            s = s+p.replaceAll("http://.*#", "");
            if(i++ < parts.length-1){
                s = s+">";
            }
        }
        System.out.println("     "+s);
        if(printMetaDetails){
            System.out.println("         "
                    +"Meta: {"
                    +"'type':"+(ax.getAxiomType().toString())+","
                    +"'isAxiom':"+(ax.isAxiom()?"1":"0")+","
                    +"'isAnnotationAxiom':"+(ax.isAnnotationAxiom()?"1":"0")+","
                    +"'isIndividual':"+(ax.isIndividual()?"1":"0")+","
                    +"'isLogicalAxiom':"+(ax.isLogicalAxiom()?"1":"0")+","
                    +"}"
            );
        }
    }




}
