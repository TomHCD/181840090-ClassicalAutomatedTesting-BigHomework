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

import java.io.*;
import java.util.*;

//对于生成改变的读取和写入
public class printChange {
    public static void PrintChange(AnalysisScope scope, String address, String changeAddress) throws ClassHierarchyException, CancelException, IOException {
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
        List<String> Callee = new ArrayList<String>();
        List<String> Caller = new ArrayList<String>();
        List<String> CalleeMethod = new ArrayList<String>();
        List<String> CallerMethod = new ArrayList<String>();
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
                        Callee.add(node.getMethod().getDeclaringClass().getName().toString());
                        Caller.add(temp.getMethod().getDeclaringClass().getName().toString());
                        CalleeMethod.add(node.getMethod().getSignature());
                        CallerMethod.add(temp.getMethod().getSignature());
                        //CalleeToCaller.add('"' + node.getMethod().getDeclaringClass().getName().toString() + '"' + " -> " + '"' + temp.getMethod().getDeclaringClass().getName().toString() + '"' + ';');
                        //CalleeToCallerMethod.add('"' + node.getMethod().getSignature()+ '"' + " -> " + '"' + temp.getMethod().getSignature()+ '"' + ';');
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
                        Callee.add(temp.getMethod().getDeclaringClass().getName().toString());
                        Caller.add(node.getMethod().getDeclaringClass().getName().toString());
                        CalleeMethod.add(temp.getMethod().getSignature());
                        CallerMethod.add(node.getMethod().getSignature());
                        //CalleeToCaller.add('"' + temp.getMethod().getDeclaringClass().getName().toString() + '"' + " -> " + '"' + node.getMethod().getDeclaringClass().getName().toString() + '"' + ';');
                        //CalleeToCallerMethod.add('"' + temp.getMethod().getSignature()+ '"' + " -> " + '"' + node.getMethod().getSignature()+ '"' + ';');
                        //System.out.println('"'+temp.getMethod().getDeclaringClass().getName().toString()+ '"' + " -> "+'"'+node.getMethod().getDeclaringClass().getName().toString()+'"'+';');                    }
                    }
                }
            }
        }
/*
        //去重
        HashSet calleeTocaller = new HashSet(CalleeToCaller);
        Iterator i1 = calleeTocaller.iterator();
        while(i1.hasNext()){
            System.out.println(i1.next());
        }
        //去重
        HashSet calleeTocallerMethod = new HashSet(CalleeToCallerMethod);
        Iterator i2 = calleeTocallerMethod.iterator();
        while(i2.hasNext()){
            System.out.println(i2.next());
        }


*/
        StringBuffer buffer = new StringBuffer();
        BufferedReader bf = new BufferedReader(new FileReader(changeAddress));
        String s = null;
        while ((s = bf.readLine()) != null) {//使用readLine方法，一次读一行
            buffer.append(s.trim());
            buffer.append(" ");          //加空格方便split进入数组
        }
        String xml = buffer.toString();
        //System.out.println(xml);
        String[] str = xml.split(" ");
        List<String> changeClass = new ArrayList<String>();
        List<String> changeMethod = new ArrayList<String>();
        for(int i=0;i< str.length;i++){
            if(i%2==0){//因为split的时候，类都在偶数，方法在奇数，因此这样分别读入两个数组
                changeClass.add(str[i]);
            }else{
                changeMethod.add(str[i]);
            }
        }
        //System.out.println(changeClass);
        //System.out.println(changeMethod);

        List<String> selectionClassFirstclass = new ArrayList<String>();
        List<String> selectionClassFirst = new ArrayList<String>();
        //对于selection-class
        for(int i=0;i< changeClass.size();i++){
            for(int k=0;k<Callee.size();k++){
                if(changeClass.get(i).equals(Callee.get(k))){
                    selectionClassFirstclass.add(Caller.get(k));
                    selectionClassFirst.add(Caller.get(k)+" "+CallerMethod.get(k));
                    //System.out.println(Caller.get(k)+" "+CallerMethod.get(k));
                    //System.out.println();
                }
            }
        }
        HashSet selectionClassSecondclass = new HashSet();
        //此处筛选出来的依赖和方法还含有classes中的，因此需要去掉不含Test文件名的类
        for(int i=0;i<selectionClassFirstclass.size();i++){
            if(selectionClassFirstclass.get(i).contains("Test")){
                selectionClassSecondclass.add(selectionClassFirstclass.get(i));
            }
        }
        //System.out.println(selectionClassSecondclass);
        HashSet selectionClassSecond = new HashSet(selectionClassFirst);
        List<String> selectionClassThirdclass = new ArrayList<String>();
        List<String> selectionClassThird = new ArrayList<String>();
        selectionClassThirdclass.addAll(selectionClassSecondclass);
        selectionClassThird.addAll(selectionClassSecond);

        //去重
        HashSet selectionClassResult = new HashSet();
        for(int i=0;i<selectionClassThirdclass.size();i++){
            for(int k=0;k<selectionClassThird.size();k++){
                if(selectionClassThird.get(k).contains(selectionClassThirdclass.get(i))){
                    selectionClassResult.add(selectionClassThird.get(k));
                }
            }
        }

        //写入selection-class.txt
        PrintWriter pw = new PrintWriter(new FileWriter("seletction-class.txt"));
        for(Object object:selectionClassResult){
            pw.println(object);
        }
        pw.close();



        //对于selection-method
        List<String> selectionMethod = new ArrayList<String>();
        //对于selection-method
        for(int i=0;i< changeMethod.size();i++){
            for(int k=0;k<CalleeMethod.size();k++){
                if(changeMethod.get(i).equals(CalleeMethod.get(k))){
                    selectionMethod.add(Caller.get(k)+" "+CallerMethod.get(k));
                    //System.out.println(Caller.get(k)+" "+CallerMethod.get(k));
                    //System.out.println();
                }
            }
        }
        //借用上部分的selectionClassThirdclass，这样保证类只属于test class
        HashSet selectionMethodResult = new HashSet();
        for(int i=0;i<selectionClassThirdclass.size();i++){
            for(int k=0;k<selectionMethod.size();k++){
                if(selectionMethod.get(k).contains(selectionClassThirdclass.get(i))){
                    selectionMethodResult.add(selectionMethod.get(k));
                }
            }
        }

        //写入selection-class.txt
        PrintWriter pw2 = new PrintWriter(new FileWriter("seletction-method.txt"));
        for(Object object:selectionMethodResult){
            pw2.println(object);
        }
        pw2.close();



/*
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
        FileUtil.writeFile(new File("report/"+Temp1), builderClass.toString());

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
        FileUtil.writeFile(new File("report/"+Temp2), builderMethod.toString());

 */
    }
}