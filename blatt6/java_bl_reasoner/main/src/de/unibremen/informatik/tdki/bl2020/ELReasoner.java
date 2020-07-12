package de.unibremen.informatik.tdki.bl2020;


import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Authors: Dennis Marschner, Alireza Mahdavi
 *
 * This EL-Reasoner konsequenzbasierte Verfahren R1-R3
 *
 * Known Issue:
 * - R3 is not working for konjuctions, but for simple Transitions like in Testcase 3!
 * - R4 is not implemented yet. But it should be implemented like our solution for R3 -> Just recursive Checking all possible subclasses
 */
public class ELReasoner {

    public ELReasoner() {
        // Nur diesen leeren Konstruktor verwenden!
    }

    public OWLOntology classify(OWLOntologyManager manager, OWLOntology ontology) throws OWLReasonerRuntimeException, NotInELNormalFormException {

        //Throw exception if we have not NF
        checkForNFRestrictionProblem(ontology.logicalAxioms().collect(Collectors.toSet()));

        OWLDataFactory df = manager.getOWLDataFactory();
        Set<OWLAxiom> allAxioms = ontology.axioms().sorted().collect(Collectors.toSet());
        Set<OWLAxiom> allLogicalAxioms = ontology.logicalAxioms().collect(Collectors.toSet());

        //Add Subclasses because of Rule 1 + 2
        OWLClass topClass = df.getOWLClass(IRI.create("http://www.w3.org/2002/07/owl#Thing"));
        Set<OWLClass> ontologyClasses = ontology.classesInSignature().collect(Collectors.toSet());
        for (OWLClass c : ontologyClasses) {
            //Loop over all classes from ontology

            //Rule 1: R1
            OWLAxiom newAxiom = df.getOWLSubClassOfAxiom(c, c);
            System.out.println("R1 Add: "+newAxiom);
            //TODO: Known issue: Normally we should check if we would add a duplicate axiom here.
            // But duplicates are not wrong in TBoxes, only some redundancy...
            ontology.add(df.getOWLSubClassOfAxiom(c, c));
            //Rule 2: R2
            newAxiom = df.getOWLSubClassOfAxiom(c, topClass);
            //TODO: Known issue: Normally we should check if we would add a duplicate axiom here.
            // But duplicates are not wrong in TBoxes, only some redundancy...
            System.out.println("R2 Add: "+newAxiom);
            ontology.add(newAxiom);
        }


        Set<OWLClass> allClasses = ontology.classesInSignature().collect(Collectors.toSet());
        for (OWLClass c : ontologyClasses) {
            for (OWLClass c2 : ontologyClasses) {
                if(c.getIRI() != c2.getIRI()){
                    if(isAimpliziertB(ontology,c,c2)){
                        System.out.println("R3 Add: "+df.getOWLSubClassOfAxiom(c, c2));
                        //TODO: Known issue: Normally we should check if we would add a duplicate axiom here.
                        // But duplicates are not wrong in TBoxes, only some redundancy...
                        ontology.add(df.getOWLSubClassOfAxiom(c, c2));
                    }
                }
            }
        }

        return ontology;
    }


    /**
     * This function can ce called to check if some OWLClass A implies OWLClass B.
     * Because this function calls itself recursivly we can find deeper implications like:
     * A implies X and X implies B! Transitiv Rule 3!
     *
     *  Known Issue:
     *  - R3 is not working for konjuctions, but for simple Transitions like in Testcase 3!
     *
     *  We think to add Rule 4 it's not much code extension in this function. Just also some further recursive
     *  checking and adding of possibly subclasses
     * @param ontology
     * @param a
     * @param b
     * @return
     */
    public boolean isAimpliziertB(OWLOntology ontology,OWLClass a,OWLClass b){
        //System.out.println("Find: "+a+" [= "+b+" ...");

        Set<OWLAxiom> allLogicalAxioms = ontology.logicalAxioms().collect(Collectors.toSet());
        //Loop over all Axioms of our ontology
        for (OWLAxiom ax : allLogicalAxioms){
            int classCount = 0;
            int objectSomeValuesFromCount = 0;
            int objectIntersectionOfCount = 0;
            int unknownType = 0;
            Set<OWLClassExpression> owlClassExpressions = ax.nestedClassExpressions().sorted().collect(Collectors.toSet());
            for (OWLClassExpression expressionType:owlClassExpressions){
                if(expressionType.getClassExpressionType().getName()=="Class") {
                    classCount++;
                }else if(expressionType.getClassExpressionType().getName()=="ObjectSomeValuesFrom"){
                    objectSomeValuesFromCount++;
                 }else if(expressionType.getClassExpressionType().getName()=="ObjectIntersectionOf"){
                    objectIntersectionOfCount++;
                }else{
                    unknownType++;
                }
            }
            //System.out.println("c:"+classCount+",k:"+objectIntersectionOfCount+",e:"+objectSomeValuesFromCount);
            if(classCount == 2 && unknownType==0 && objectIntersectionOfCount==0 && objectSomeValuesFromCount==0){

                Set<OWLClass> classesOfAxiom = ax.classesInSignature().sorted().collect(Collectors.toSet());
                List<OWLClass> list = new ArrayList<OWLClass>(classesOfAxiom);
                OWLClass leftClass = list.get(0);
                OWLClass rightClass = list.get(1);

                //HotFix - Find first and second Class by REGEX Search. Sometimes they are swapped!?
                String subClassStr = ax.toString();
                subClassStr = subClassStr.replaceAll("owl:Thing", "<owl:Thing>");
                String firstConcept = findFirstAxiomTag(subClassStr);
                if(firstConcept.contentEquals("<"+list.get(0).getIRI()+">")){
                    leftClass = list.get(0);
                    rightClass = list.get(1);
                }else{
                    leftClass = list.get(1);
                    rightClass = list.get(0);
                }

                if(leftClass == a && rightClass == b){
                    return true;
                }else if(rightClass == b){
                    if(isAimpliziertB(ontology,a,leftClass)){
                        return true;
                    }
                }
            }
        }
        return false;
    }




    /**
     * Check if given ontology is in NF. If not throw exception
     */
    static void checkForNFRestrictionProblem( Set<OWLAxiom> allLogicalAxioms) throws NotInELNormalFormException{
        //Loop over all Axioms of our ontology
        for (OWLAxiom ax : allLogicalAxioms){

            Set<OWLClassExpression> owlClassExpressions = ax.nestedClassExpressions().sorted().collect(Collectors.toSet());
            int classCount = 0;
            int objectSomeValuesFromCount = 0;
            int objectIntersectionOfCount = 0;
            int unknownType = 0;
            for (OWLClassExpression expressionType:owlClassExpressions){
                if(expressionType.getClassExpressionType().getName()=="Class") {
                    //CONCEPT
                    classCount++;
                }else if(expressionType.getClassExpressionType().getName()=="ObjectSomeValuesFrom"){
                    //EXISTS
                    objectSomeValuesFromCount++;
                }else if(expressionType.getClassExpressionType().getName()=="ObjectIntersectionOf"){
                    //KONJUNCTION
                    objectIntersectionOfCount++;
                }else{
                    unknownType++;
                }
            }
            //System.out.println("c:"+classCount+",k:"+objectIntersectionOfCount+",e:"+objectSomeValuesFromCount);
            //Full Test if we have NF
            if(objectSomeValuesFromCount >= 1 && objectIntersectionOfCount >= 1){
                throw new NotInELNormalFormException("No NF! Because we have more than one conjuction and exists restriction in one subclass");
            }
            if(objectSomeValuesFromCount >= 2){
                throw new NotInELNormalFormException("No NF! Because we have more than 1 exists restriction in one subclass");
            }
            if(objectIntersectionOfCount >= 2){
                throw new NotInELNormalFormException("No NF! Because we have more than 1 conjunction in one subclass");
            }
            if(classCount < 1){
                throw new NotInELNormalFormException("No NF! Because we have no concepts in one sublass");
            }
            if(unknownType > 0){
                throw new NotInELNormalFormException("No NF! Because we we not permitted axiom types in subclass");
            }
        }
    }

    /**
     * Hust remoe the ugly long part of axiom-name "http....#"
     * @param ax
     */
    static void printAxiomPretty(OWLAxiom ax) {
        String[] parts = ax.toString().split(">");
        String s = "";
        int i=0;
        for(String p :parts){
            s = s+p.replaceAll("http://.*#", "");
            if(i++ < parts.length-1){
                s = s+">";
            }
        }
        System.out.println(s);
    }


    /**
     * This function is important, because we did not find a java-method to get left or right element of subclass.
     * But it is very important, which class is left and which class is right of subclass like: SubClassOf(<A> <B>)!
     * "Example: String="SubClassOf(<http://www.semanticweb.org/schneider/ontologies/2019/6/test1in#B> <owl:Thing>)";
     * would return "<http://www.semanticweb.org/schneider/ontologies/2019/6/test1in#B>"
     * @param owlAxiomString
     * @return
     */
    public String findFirstAxiomTag(String owlAxiomString){
        Matcher m = Pattern.compile("<[^>]+>").matcher(owlAxiomString);
        if(m.find()) {
            return m.group();
        }else{
            return null;
        }
    }

}
