import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.util.CancelException;

public class Print0CFA {
    public static CallGraph print0CFA(AnalysisScope scope) throws ClassHierarchyException, CancelException {
        // 1.生成类层次关系对象
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        // 2.生成进入点
        AllApplicationEntrypoints entrypoints = new AllApplicationEntrypoints(scope, cha);
        // 3.利用0-CFA算法构建调用图
        AnalysisOptions option = new AnalysisOptions(scope, entrypoints);
        SSAPropagationCallGraphBuilder builder = Util.makeZeroCFABuilder(Language.JAVA, option, new AnalysisCacheImpl(), cha, scope);
        return builder.makeCallGraph(option);
    }
}
