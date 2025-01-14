package programanalysis;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;

import java.util.Collections;

public class DataDependenceAnalysis {
    public static void main(String[] args) {
        // Configure Soot
        Options.v().set_prepend_classpath(true); // Use the system classpath
        Options.v().set_whole_program(true);    // Enable whole-program analysis
        Options.v().set_allow_phantom_refs(true); // Allow references to unresolved classes
        Options.v().set_process_dir(Collections.singletonList("C:\\Users\\admin\\cophi\\Closure1Bug\\build\\test")); // Path to compiled classes

        // Load the target class
        SootClass targetClass = Scene.v().loadClassAndSupport("com.google.javascript.jscomp.CommandLineRunnerTest");
        Scene.v().loadNecessaryClasses();

        // Specify the method to analyze
        String targetMethodSignature = "void test(java.lang.String[],java.lang.String[],com.google.javascript.jscomp.DiagnosticType)";
        SootMethod targetMethod = findMethodBySignature(targetClass, targetMethodSignature);

        if (targetMethod != null && targetMethod.isConcrete()) {
            System.out.println("Analyzing method: " + targetMethod.getName());
            performDataDependenceAnalysis(targetMethod);
        } else {
            System.out.println("Method not found or is not concrete: " + targetMethodSignature);
        }
    }

    public static SootMethod findMethodBySignature(SootClass sootClass, String methodSignature) {
        // Find the method in the class
        for (SootMethod method : sootClass.getMethods()) {
            if (method.getSubSignature().equals(methodSignature)) {
                return method;
            }
        }
        return null; // Return null if the method is not found
    }

    public static void performDataDependenceAnalysis(SootMethod method) {
        // Retrieve the active body of the method
        Body body = method.retrieveActiveBody();

        // Build the control flow graph
        UnitGraph cfg = new ExceptionalUnitGraph(body);

        // Perform reaching definitions analysis
        SimpleLocalDefs defs = new SimpleLocalDefs(cfg);
        SimpleLocalUses uses = new SimpleLocalUses(cfg, defs);

        for (Unit unit : body.getUnits()) {
            if (unit instanceof Stmt) {
                Stmt stmt = (Stmt) unit;

                // Identify statements using variables
                for (ValueBox useBox : stmt.getUseBoxes()) {
                    if (useBox.getValue() instanceof Local) {
                        Local local = (Local) useBox.getValue();
                        System.out.println("Variable: " + local);

                        // Find all definitions reaching this use
                        for (Unit defUnit : defs.getDefsOfAt(local, stmt)) {
                            System.out.println("Definition: " + defUnit + " -> Use: " + stmt);
                        }
                    }
                }
            }
        }
    }
}
