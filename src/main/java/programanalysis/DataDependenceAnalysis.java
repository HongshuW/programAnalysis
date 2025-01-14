package programanalysis;

import soot.Body;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataDependenceAnalysis {
    public static void main(String[] args) {
        G.reset();

        /* Configure Soot */
        Options.v().set_keep_line_number(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().setPhaseOption("jb", "use-original-names:true");


        /* Load class */

        // Options.v().set_process_dir(Collections.singletonList("C:\\Users\\admin\\cophi\\Closure1Bug\\build\\test"));
        // Options.v().set_soot_classpath("C:\\Users\\admin\\cophi\\Closure1Bug\\build;C:\\Program Files\\Java\\jdk1.8.0_202\\jre\\lib\\rt.jar");

        Options.v().set_process_dir(Collections.singletonList("C:\\Users\\admin\\cophi\\programAnalysis\\target\\classes"));
        Options.v().set_soot_classpath("C:\\Users\\admin\\cophi\\programAnalysis\\target;C:\\Program Files\\Java\\jdk1.8.0_202\\jre\\lib\\rt.jar");

        Scene.v().loadNecessaryClasses();

        /* Configure Spark */
        Map<String,String> sparkOptions = new HashMap<>();
        sparkOptions.put("enabled", "true");
        sparkOptions.put("verbose", "true");
        sparkOptions.put("vta", "true");
        sparkOptions.put("pta", "spark");
        SparkTransformer.v().transform("", sparkOptions);

        /* Points-to Analysis */
        PackManager.v().runPacks();
        PointsToAnalysis pa = Scene.v().getPointsToAnalysis();

        // // Load the target class
        // SootClass targetClass = Scene.v().loadClassAndSupport("com.google.javascript.jscomp.CommandLineRunnerTest");

        // // Specify the method to analyze
        // String targetMethodSignature = "void test(java.lang.String[],java.lang.String[],com.google.javascript.jscomp.DiagnosticType)";
        // SootMethod targetMethod = findMethodBySignature(targetClass, targetMethodSignature);

        // Load the target class
        SootClass targetClass = Scene.v().loadClassAndSupport("programanalysis.ExampleProgram");
        // Specify the method to analyze
        SootMethod targetMethod = targetClass.getMethodByName("main");

        Body body = targetMethod.retrieveActiveBody();
        for (Local local : body.getLocals()) {
            PointsToSet pts = pa.reachingObjects(local);
            System.out.println("Local Variable: " + local.getName() + 
                            " | PointsToSet: " + pts);
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

}
