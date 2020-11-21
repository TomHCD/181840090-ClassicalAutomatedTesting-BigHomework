import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.io.FileUtil;
import com.sun.tools.classfile.Dependency;

import javax.swing.text.html.HTMLDocument;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
//依赖图生成
public class PrintCHA {
    public static void printCHA(AnalysisScope scope,String address) throws ClassHierarchyException, CancelException, IOException {
        // 1.生成类层次关系对象
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        // 2.生成进入点
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
        // 3.利用CHA算法构建调用图
        // CallGraph cg = new CHACallGraph(cha); // 这句出错了，请注意。正确用法见下一行
        CHACallGraph cg = new CHACallGraph(cha);
        cg.init(eps);
        // 4.遍历cg中所有的节点
        for (CGNode node : cg) {
            // node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if (node.getMethod() instanceof ShrikeBTMethod) {
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                // 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    // 获取声明该方法的类的内部表示
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    // 获取方法签名
                    String signature = method.getSignature();
                    //System.out.println(classInnerName + " " + signature);
                }
            } else {
                //System.out.println(String.format("'%s'不是一个ShrikeBTMethod：%s", node.getMethod(), node.getMethod().getClass()));
            }
        }
        List<String> CalleeToCaller = new ArrayList<String>();
        List<String> CalleeToCallerMethod = new ArrayList<String>();
        //打印前继节点
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                // 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    Iterator<CGNode> it = cg.getPredNodes(node);
                    while (it.hasNext()) {
                        CGNode temp = it.next();
                        if (!"Application".equals(temp.getMethod().getDeclaringClass().getClassLoader().toString())) {
                            continue;
                        }
                        CalleeToCaller.add('"' + node.getMethod().getDeclaringClass().getName().toString() + '"' + " -> " + '"' + temp.getMethod().getDeclaringClass().getName().toString() + '"' + ';');
                        CalleeToCallerMethod.add('"' + node.getMethod().getSignature()+ '"' + " -> " + '"' + temp.getMethod().getSignature()+ '"' + ';');
                        //System.out.println('"'+node.getMethod().getDeclaringClass().getName().toString()+ '"' + " -> "+'"'+temp.getMethod().getDeclaringClass().getName().toString()+'"'+';');
                    }
                }
            }
        }
        //打印后继节点
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                // 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    Iterator<CGNode> it = cg.getSuccNodes(node);
                    while (it.hasNext()) {
                        CGNode temp = it.next();
                        if (!"Application".equals(temp.getMethod().getDeclaringClass().getClassLoader().toString())) {
                            continue;
                        }
                        CalleeToCaller.add('"' + temp.getMethod().getDeclaringClass().getName().toString() + '"' + " -> " + '"' + node.getMethod().getDeclaringClass().getName().toString() + '"' + ';');
                        CalleeToCallerMethod.add('"' + temp.getMethod().getSignature()+ '"' + " -> " + '"' + node.getMethod().getSignature()+ '"' + ';');
                        //System.out.println('"'+temp.getMethod().getDeclaringClass().getName().toString()+ '"' + " -> "+'"'+node.getMethod().getDeclaringClass().getName().toString()+'"'+';');                    }
                    }
                }
            }
        }
        //去重
        //HashSet calleeTocaller = new HashSet(CalleeToCaller);
        //Iterator i1 = calleeTocaller.iterator();
        //while(i1.hasNext()){
        //    System.out.println(i1.next());
        //}
        //去重
        //HashSet calleeTocallerMethod = new HashSet(CalleeToCallerMethod);
        //Iterator i2 = calleeTocallerMethod.iterator();
        //while(i2.hasNext()){
        //    System.out.println(i2.next());
        //}

        String formatAddress = address.substring(2).toLowerCase();
        //打印类依赖
        StringBuilder builderClass = new StringBuilder();
        builderClass.append("digraph "+formatAddress+"_class").append(" {\n");
        HashSet calleeTocaller = new HashSet(CalleeToCaller);
        Iterator i1 = calleeTocaller.iterator();
        while(i1.hasNext()){
            builderClass.append("\t").append(i1.next()).append("\n");
        }
        builderClass.append("}");
        String Temp1="class-"+address.substring(2)+"-cfa.dot";
        FileUtil.writeFile(new File("Report/"+Temp1), builderClass.toString());

        //打印方法依赖
        StringBuilder builderMethod = new StringBuilder();
        builderMethod.append("digraph "+formatAddress+"_method").append(" {\n");
        HashSet calleeTocallerMethod = new HashSet(CalleeToCallerMethod);
        Iterator i2 = calleeTocallerMethod.iterator();
        while(i2.hasNext()){
            builderMethod.append("\t").append(i2.next()).append("\n");
        }
        builderMethod.append("}");
        String Temp2="method-"+address.substring(2)+"-cfa.dot";
        FileUtil.writeFile(new File("Report/"+Temp2), builderMethod.toString());
    }
}

