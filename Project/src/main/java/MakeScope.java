import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import java.io.File;
import java.io.IOException;

public class MakeScope {
    public static AnalysisScope makeScope(String address) throws InvalidClassFileException, IOException {
        File exFile= null;
        try {
            exFile = new FileProvider().getFile("src/main/resources/exclusion.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        AnalysisScope scope = null;
        try {
            scope = AnalysisScopeReader.readJavaScope("src/main/resources/scope.txt",exFile,ClassLoader.class.getClassLoader());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //工作代码
        File folder = new File("givenData/ClassicAutomatedTesting/"+address+"/target/classes/net/mooctest/");
        File[] listOfFiles = folder.listFiles();
        for(File file:listOfFiles){
            if(file.isFile()){
                //System.out.println(file.getName());
                scope.addClassFileToScope(ClassLoaderReference.Application, file);
            }
        }
        //测试代码
        File folder2 = new File("givenData/ClassicAutomatedTesting/"+address+"/target/test-classes/net/mooctest/");
        File[] listOfFiles2 = folder2.listFiles();
        for(File file:listOfFiles2){
            if(file.isFile()){
                //System.out.println(file.getName());
                scope.addClassFileToScope(ClassLoaderReference.Application, file);
            }
        }
/*
        File TestClazz = new FileProvider().getFile("givenData/ClassicAutomatedTesting/0-CMD/target/classes/net/mooctest/CMD.class");
        File TestClazz1 = new FileProvider().getFile("givenData/ClassicAutomatedTesting/0-CMD/target/test-classes/net/mooctest/CMDTest.class");
        File TestClazz2 = new FileProvider().getFile("givenData/ClassicAutomatedTesting/0-CMD/target/test-classes/net/mooctest/CMDTest1.class");
        File TestClazz3 = new FileProvider().getFile("givenData/ClassicAutomatedTesting/0-CMD/target/test-classes/net/mooctest/CMDTest2.class");
        File TestClazz4 = new FileProvider().getFile("givenData/ClassicAutomatedTesting/0-CMD/target/test-classes/net/mooctest/CMDTest3.class");
        scope.addClassFileToScope(ClassLoaderReference.Application, TestClazz);
        scope.addClassFileToScope(ClassLoaderReference.Application, TestClazz1);
        scope.addClassFileToScope(ClassLoaderReference.Application, TestClazz2);
        scope.addClassFileToScope(ClassLoaderReference.Application, TestClazz3);
        scope.addClassFileToScope(ClassLoaderReference.Application, TestClazz4);
 */
        return scope;
    }
}
