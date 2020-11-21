import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.io.FileProvider;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
        //输入所需要的模式
        String order=args[0];
        //输入所需要读取的文件夹名字
        String FolderName = args[1];
        //输入更改目录的情况
        String change_info = args[2];
        //使用CHA
        PrintCHA  printcha = new PrintCHA();
        printChange Printchange = new printChange();
        //Print0CFA print0cfa = new Print0CFA();
        //未用到0-CFA
        AnalysisScope scope = MakeScope.makeScope(FolderName);
        //printcha.printCHA(scope,FolderName);
        printChange.PrintChange(scope,FolderName,change_info,order);
    }
}
