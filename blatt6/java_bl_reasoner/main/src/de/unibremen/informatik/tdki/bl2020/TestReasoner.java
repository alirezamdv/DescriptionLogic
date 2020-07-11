package de.unibremen.informatik.tdki.bl2020;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TestReasoner {

    private static String testFilePrefix = "tests/";

    private static String[][] testFileNames = {
           /* {"test0in.owl", null},
            {"test1in.owl", "test1out.owl"},
            {"test2in.owl", "test2out.owl"},*/
            {"test3in.owl", "test3out.owl"},
           /* {"test4in.owl", "test4out.owl"},
            {"test5in.owl", "test5out.owl"},
            {"test6in.owl", "test6out.owl"},
            {"test7in.owl", "test7out.owl"},
            {"test8in.owl", "test8out.owl"}*/
    };


    public static void main(String[] args) {
    	
        try {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            OWLOntology[] ont = new OWLOntology[2];
            File file;
            ELReasoner reasoner;

            for (int i = 0; i < testFileNames.length; i++) {
                System.out.println("\nTestfall " + i + ":  ");
                String[] testFileName = testFileNames[i];

                file = new File(testFilePrefix + testFileName[0]);
                if (file.exists()){
                    ont[0] = man.loadOntologyFromOntologyDocument(file);
                    System.out.println("  " + file.getAbsolutePath());
                }
                else {
                    System.out.println("  FEHLER: Testontologie " + file.getAbsolutePath() + " fehlt!");
                    continue;
                }

                if (testFileName[1] != null) {
                    file = new File(testFilePrefix + testFileName[1]);
                    if (file.exists()) {
                        ont[1] = man.loadOntologyFromOntologyDocument(file);
                    }
                    else {
                        System.out.println("  FEHLER: Testontologie " + file.getAbsolutePath() + " fehlt!");
                        continue;
                    }
                }

                reasoner = new ELReasoner();
                OWLOntology result;
                try {

                    result = reasoner.classify(man, ont[0]);
                    compareOntology(result, ont[1]);
                }
                catch(NotInELNormalFormException e) {
                    if (ont[1] == null) {
                        System.out.println("  OK.  (Ontologie ist nicht in EL-Normalform)");
                    }
                    else {
                        System.out.println("  FEHLER:  Reasoner wirft fälschlicherweise NotInELNormalFormException:");
                        System.out.println(e.getMessage());
                    }
                }

            }

        }
        catch (OWLReasonerRuntimeException | OWLOntologyCreationException e) {
            System.out.println("  Fehler: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private static void compareOntology(OWLOntology ont, OWLOntology testOnt) {
        if (testOnt == null){
            System.out.println("  FEHLER:  Reasoner wirft fälschlicherweise KEINE NotInELNormalFormException");
        }
        else {
            Set<OWLLogicalAxiom> ontAxs = ont.logicalAxioms().collect(Collectors.toSet());
            Set<OWLLogicalAxiom> testAxs = testOnt.logicalAxioms().collect(Collectors.toSet());

            if (ontAxs.equals(testAxs)) {
                System.out.println("  OK.");
            }
            else {
                System.out.println("  FEHLER:  ");
                Set<OWLLogicalAxiom> diff = new HashSet<>(testAxs);
                diff.removeAll(ontAxs);
                if (!diff.isEmpty()){
                    System.out.println(errorMessage(diff.size(), "nicht erzeugt"));
                    //System.out.println("  Folgende Subsumtionen werden vom Reasoner fälschlicherweise nicht erzeugt:");
                    for (OWLLogicalAxiom ax : diff) System.out.println("    " + prettyPrint(ax.toString()));
                }

                diff = new HashSet<>(ontAxs);
                diff.removeAll(testAxs);
                if (!diff.isEmpty()){
                    System.out.println(errorMessage(diff.size(), "erzeugt"));
                    // System.out.println("  Folgende Subsumtionen werden vom Reasoner fälschlicherweise erzeugt:");
                    for (OWLLogicalAxiom ax : diff) System.out.println("    " + prettyPrint(ax.toString()));
                }
            }
        }
    }

    private static String errorMessage(int size, String suffix) {
        if (size == 1) {
            return "Folgende Subsumtion wird vom Reasoner fälschlicherweise " + suffix + ":";
        }
        else {
            return "Folgende Subsumtionen werden vom Reasoner fälschlicherweise " + suffix + ":";
        }

    }
    static String prettyPrint2(String s) {
        String[] parts = s.split(">");
        String resultString = "";
        int i=0;
        for(String part :parts){
            resultString = resultString+part.replaceAll("http://.*#", "");
            if(i++ < parts.length-1){
                resultString = resultString+">";
            }
        }
        return resultString;
    }

    /*
    static String prettyPrint(String s) {
        return s.replaceAll("http://.*#", "")
                .replaceAll("<","")
                .replaceAll(">","");
    }
    */

    static String prettyPrint(String str) {
        String[] parts = str.split(">");
        String s = "";
        int i=0;
        for(String p :parts){
            s = s+p.replaceAll("http://.*#", "");
            if(i++ < parts.length-1){
                s = s+">";
            }
        }
        return s;
    }



}
