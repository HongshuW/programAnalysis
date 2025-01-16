package programanalysis.sootconfig;

import java.util.Collections;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

public class ClosureSootConfig extends SootConfiguration {

    @Override
    public void setClassPath() {
        Options.v().set_process_dir(Collections.singletonList("C:\\Users\\admin\\cophi\\Closure1Bug\\build\\test"));
        Options.v().set_soot_classpath("C:\\Users\\admin\\cophi\\Closure1Bug\\build;C:\\Program Files\\Java\\jdk1.8.0_202\\jre\\lib\\rt.jar");
    }

    @Override
    public SootMethod getTargetMethod() {
        // Load the target class
        SootClass targetClass = Scene.v().loadClassAndSupport("com.google.javascript.jscomp.CommandLineRunnerTest");
        // Specify the method to analyze
        String targetMethodSignature = "void test(java.lang.String[],java.lang.String[],com.google.javascript.jscomp.DiagnosticType)";
        SootMethod targetMethod = findMethodBySignature(targetClass, targetMethodSignature);

        return targetMethod;
    }

    @Override
    public Local getTargetVariable(Body methodBody) {
        return findLocalVariable(methodBody, "compiled");
    }
    
}
