package de.unibremen.informatik.tdki.bl2020;



import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;

import java.util.Set;
import java.util.stream.Collectors;


public class ELReasoner {

    public ELReasoner() {
        // Nur diesen leeren Konstruktor verwenden!
    }

    public OWLOntology classify(OWLOntologyManager manager, OWLOntology ontology) throws OWLReasonerRuntimeException, NotInELNormalFormException {

        OWLDataFactory df = manager.getOWLDataFactory();
        OWLClass topClass = df.getOWLClass(IRI.create("http://www.w3.org/2002/07/owl#Thing"));

        Set<OWLAxiom> allAxioms = ontology.axioms().collect(Collectors.toSet());
        Set<OWLAxiom> allLogicalAxioms = ontology.logicalAxioms().collect(Collectors.toSet());

        //First Step: Check if NNF
        for (OWLAxiom ax : allAxioms){
            //If Restriction of NNF is hurt
            if(false){
                throw new NotInELNormalFormException("No NNF!");
            }
        }

        //Next Step do Algorithm (Rule R1...R4) with our NNF
        //Loop over all Axioms of our ontology
        for (OWLAxiom ax : allAxioms){

            Set<OWLProperty> owlProps = ax.objectPropertiesInSignature().collect(Collectors.toSet());
            Set<OWLClass> owlClasses = ax.classesInSignature().collect(Collectors.toSet());

            if(ax.getAxiomType().getActualClass() == OWLDeclarationAxiom.class){
                //Current Axiom is of Type "Declaration"

                OWLClass classOfCurrentDeclarationAxiom = df.getOWLClass(owlClasses.iterator().next()); //hint declaration has only one class!

                //Rule 1: R1
                ontology.add(df.getOWLSubClassOfAxiom(classOfCurrentDeclarationAxiom, classOfCurrentDeclarationAxiom));

                //Rule 2: R2
                ontology.add(df.getOWLSubClassOfAxiom(classOfCurrentDeclarationAxiom, topClass));


            }else if(ax.getAxiomType().getActualClass() == OWLSubClassOfAxiom.class){
                //Current Axiom is of Type "Subclass"

                //TODO IMPLEMENTATION
            }else{
                //Unknown axiomtype! Maybe not NNF!?
                throw new RuntimeException("Unknown Type!");
            }

            //System.out.println("    OWLClasses: "+owlClasses);
            //System.out.println("    OWLProps: "+owlProps);
            printAxiomStructure(ax,false);
        }


        for (OWLAxiom ax : allAxioms){
            printAxiomStructure(ax,false);
        }
/*
        System.out.println("Logische Axiome");
        Set<OWLLogicalAxiom> ontLogAxs = ontology.logicalAxioms().collect(Collectors.toSet());
        for (OWLLogicalAxiom ax : ontLogAxs){
            //printAxiomStructure(ax,true);
        }

        //throw new RuntimeException("TODO: Implement");
*/
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
        //System.out.println("     "+ax.toString());
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
