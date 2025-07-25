package com.ncslab.code;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import java.io.*;
import java.util.Optional;

import com.ncslab.code.template.TemplateProvider;
import com.ncslab.code.template.TemplateProviderFactory;

import com.ncslab.code.util.ResourceUtils;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.GlobalVariable;
import com.ncslab.block.io.terminal.Terminal;
import com.utils.Property;

import lombok.Getter;

abstract public class CodeStruct{

    // Template provider for accessing resource files
    protected final TemplateProvider templateProvider;

    public Set<String> globalDeclareCodeSet = new LinkedHashSet<>();
    public Set<String> globalInitCodeSet = new LinkedHashSet<>();
    public Set<String> globalEndCodeSet = new LinkedHashSet<>();
    public Set<String> includeCodeSet = new LinkedHashSet<>();
    public Set<WrittenFile> writtenFileSet = new HashSet<>();

    /**
     * You can freely add declare code in this function, and it will be added to the
     * cpp file.
     * @param code The code you want to add. ples add "\n" at the end for each line.
     * @author Ethy9160
     */
    public void addGlobalDeclareCode(String code) {
        globalDeclareCodeSet.add(code);
    }

    /**
     * You can freely add init code in this function, and it will be added to the
     * global init com.ncslab.code.
     * @param code The code you want to add. ples add "\n" at the end for each line.
     * @author Ethy9160
     */
    public void addGlobalInitCode(String code) {
        globalInitCodeSet.add(code);
    }

    /**
     * You can freely add end code in this function, and it will be added to the
     * global end com.ncslab.code.
     * @param code The code you want to add. ples add "\n" at the end for each line.
     * @author Ethy9160
     */
    public void addGlobalEndCode(String code) {
        globalEndCodeSet.add(code);
    }

    /**
     * You can freely add include code in this function, and it will be added to the
     * cpp file.
     * @param code The code you want to add. ples add "\n" at the end for each line.
     * @author Ethy9160
     */
    public void addIncludeCode(String code) {
        includeCodeSet.add(code);
    }

    /**
     * Add written file to the set. <br>
     * <b>ATTENTION</b><br>
     * If you use relative path, it will be searched in the resources directory structure.
     * The file will be searched in src/main/resources/com/ncslab/code/ and its subdirectories.
     * @param filePath the relative path of the file in the source folder.
     * @param targetPath the target path of the file.
     * @param overwrite whether to overwrite the file if it exists.
     */
    public void addWrittenFile(String filePath, String targetPath, boolean overwrite) {
        writtenFileSet.add(new WrittenFile(filePath, targetPath, overwrite));
    }

    /**
     * Add written file to the set. <br>
     * <b>ATTENTION</b><br>
     * If you use reletive path ,you <b>should be able to know the source folder!</b>
     * i.e. in Ubuntu22.04, the source folder is "src/main/java/com/ncslab/code/c/linux/pc/simulation/".<br>
     * Or, you can use the absolute path for the filePath.
     * @param filePath the reletive path of the file in the source folder.
     * @param targetPath the target path of the file.
     * @see #addWrittenFile(String filePath, String targetPath, boolean overwrite)
     */
    public void addWrittenFile(String filePath, String targetPath) {
        addWrittenFile(filePath, targetPath, true);
    }

    //模块的输入是否作为信号

    public boolean inputAsSignal=true;
    //模块的输出是否作为信号
    public boolean outputAsSignal=true;


    //头文件的代码

    //author:xiazhiqiang
    //define arrays to save data
    @Getter
    public String arraysCode="";
    //end

    // Global variables
    @Getter
    public String globalVariable="";
    // public String globalDeclare="";
    public String globalInit="";
    // public String globalEnd="";

    public String includeCode="";
    public String includeCodeStm="";
    //init初始化的代码
    @Getter
    public String initCode="";
    //init初始化STM32中的c初始化前的配置的的代码
    public String initConfigCode="";
    //Output的代码
    @Getter
    public String outputCode="";
    //update的代码
    @Getter
    public String updateCode="";

    public String discreteUpdateCode="";

    public String sinkOutputCode="";
    public String sinkStatusClearCode="";

    //定义的代码
    public String statementCode="";
    //微分计算的代码
    public String derivativeCode="";

    public String terminateCode="";

    /*����Parameter�Ĵ��� ��*REAL Block5_Parameter_P*/

    public String parameterDefineCode="";
    /*����State�Ĵ��� �� REAL Block1_State_pumpState;*/
    public String stateDefineCode="";
    /*����Output�źŵĴ��룬�� REAL Block1_Output1;*/
    public String outputSignalDefineCode="";

    public String hardwareDefineCode="";

    static String BLOCK_STRUCTURE_FORMAT = "BLOCK block%d={(char *)\"%s\",(char *)\"%s\",(char *)\"%s\",(char *)\"%s\",%d,%d,%d,%d,%d};\n";

    /*定义所有监控数据实体的代码，包括INPUT_PORT OUT_PORT PARAMTER STATE SIGNAL BLOCK*/
    public String dataStructureCode="";
    /*定义监控数据实体初始化的代码，初始化各个组件结构的名称，path等，让指针指向指定的位置，建立数据结构， */
    public String dataStructureInitCode="";

    /**
     * End code, that will be run after the simulation ends
     */
    @Getter
    public String finalizeCode = "";

    private int parameterIndex=1;
    private int stateIndex=1;

    private Vector<Parameter> parameterList=new Vector<Parameter>();
    private Vector<State> stateList=new Vector<State>();
    private Vector<OutputSignal> outputSignalList=new Vector<OutputSignal>();
    private Vector<GlobalVariable> variableList = new Vector<>();


    protected CodeModel model;

    public CodeStruct(CodeModel model) {
        this.model = model;
        // Initialize the template provider for this class
        this.templateProvider = TemplateProviderFactory.createForClass(this.getClass());
    }

    public void addGlobalVariable(String code){
        this.globalVariable += code;
    }

    public void addInitCode(String code) {
        initCode+=code;
    }
    public void addInitConfigCode(String code) {
        initConfigCode+=code;
    }

    //end

    public void addArraysCode(String code) { arraysCode+=code; }
    //end

    public void addOutputCode(String code) {
        outputCode+=code;
    }

    public void addFinalizeCode(String code){
        this.finalizeCode += code;
    }

    public void addTerminateCode(String code) {
        terminateCode+=code;
    }

    public void addUpdateCode(String code) {
        updateCode+=code;
    }

    public void addDiscreteUpdateCode(String code) {
        discreteUpdateCode+=code;
    }

    public void addSinkOutputCode(String code) {
        sinkOutputCode+=code;
    }

    public void addSinkStatusClearCode(String code) {
        sinkOutputCode+=code;
    }

    public void addDerivativeCode(String code) {
        derivativeCode+=code;
    }

    @Getter
    protected String m2plabRoot = Optional.ofNullable(System.getenv("M2PLAB_ROOT")).orElse("/data/M2PLab");

    // Get the code path base using properties, with environment variable substitution
    protected String codePathBase=("deploy".equals(Property.instance.getProperty("mode").trim())?
        Property.instance.getProperty("CCodePath")
        :
        Property.instance.getProperty("CCodePathWin"))
        .replace("${M2PLAB_ROOT}", m2plabRoot)
        .replace("${user.home}", System.getProperty("user.home"))
        .replace("${user.dir}", System.getProperty("user.dir"));


    final private String maketool = Property.instance.getProperty("MakeTool");

    //目标文件夹的位置codePathBase/用户id/modelId
    @Getter
    protected String codePath;

    /**
     * Gets the appropriate resource subdirectory based on the class name
     * Example: com.ncslab.code.c.linux.pc.CodeModelLinuxPC -> c/linux/pc
     * @return The resource subdirectory to search in
     */
    private String getResourceSubdirectory() {
        String className = this.getClass().getName();
        if (className.startsWith("com.ncslab.code.")) {
            String[] parts = className.split("\\.");

            // Start building from index 3 (after com.ncslab.code)
            StringBuilder subDir = new StringBuilder();
            for (int i = 3; i < parts.length - 1; i++) { // Exclude the class name itself
                if (subDir.length() > 0) {
                    subDir.append("/");
                }
                subDir.append(parts[i]);
            }

            return subDir.toString();
        }

        // Default to "c" if we can't determine
        return "c";
    }

    //写文件的方法，将文件从resource中拷贝出来，写在目标文件夹
    protected void writeNCSLabFile(String fileName) {
        String fileNameOut = fileName;
        if(fileName.contains("/")){
            String [] paths = fileName.split("/");
            fileNameOut = paths[paths.length-1];
        }
        writeNCSLabFile(fileName, fileNameOut, false);
    }

    protected void writeNCSLabFile(String fileName,String fileNameOut,boolean overwrite) {
        // Get the template using our template provider, which handles nested search
        InputStream inputStream = templateProvider.getTemplate(fileName);

        // Fallback to the old method if template provider fails
        if (inputStream == null) {
            // Get subdirectory from class name to search in resources
            String subDirectory = getResourceSubdirectory();

            // Try to get the resource using ResourceUtils with nested search capability
            inputStream = ResourceUtils.getResourceAsStream(fileName, subDirectory);

            // Final fallback to the old method
            if (inputStream == null) {
                int count = 3;
                String srcFileName = fileName;
                while(inputStream == null && (count--) > 0) {
                    inputStream = this.getClass().getResourceAsStream(srcFileName);
                    srcFileName = "../" + srcFileName;
                }
            }
        }

        if(inputStream == null) {
            System.err.println("No file "+fileName+"...");
            return;
        }

        File file=new File(codePath+"/"+fileNameOut);
        if(file.exists() && !overwrite) {
            return;
        }

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while((len=inputStream.read(buffer))>0) {
                outputStream.write(buffer,0,len);
            }

            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void writeNCSLabFile(String fileName, String fileNameOut) {
        writeNCSLabFile(fileName, fileNameOut, false);
    }

    //加入全局的Parameter的列表
    public void addParameter(Parameter parameter) {
        parameterList.add(parameter);
    }
    //加入全局的state的列表
    public void addState(State state) {
        stateList.add(state);
    }
    //加入全局的信号的列表
    public void addOutputSignal(OutputSignal outputSignal) {
        outputSignalList.add(outputSignal);
    }
    // add global variables
    public void addGlobalVariable(GlobalVariable variable){
        this.variableList.add(variable);
    }

    public void addStatementCode(String code) {
        this.statementCode += code;
    }

    //建立Model,block,input,output,signal,state,parameter等数据结构，并初始化
    public void gnenrateDataStructureCode() {
        //建立一系列数据结构的定义
        generateDataStrucure();
        //初始化数据结构，实现数据结构之间的指针连接
        generateDataStrucureInit();
    }

    //建立数据结构的定义
    abstract protected void generateDataStrucure();

    //初始化数据结构，实现数据结构之间的指针连接
    private void generateDataStrucureInit() {
        dataStructureInitCode+="/*Initialize data structure*/\n";
    }
}

class WrittenFile implements Comparable<WrittenFile>{
    String filePath;
    String targetPath;
    boolean overwritten;
    WrittenFile(String filePath,String targetPath, boolean overwritten){
        this.filePath=filePath;
        this.targetPath=targetPath;
        this.overwritten = overwritten;
    }

    String getFilePath() {
        return filePath;
    }

    String getTargetPath(){
        return targetPath;
    }

    boolean isOverwritten(){
        return overwritten;
    }

    // Override hashCode and equals to make the set unique
    @Override
    public int hashCode() {
        return targetPath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WrittenFile) {
            WrittenFile file=(WrittenFile)obj;
            return file.targetPath.equals(this.targetPath);
        }
        return false;
    }

    /**
     * Impliment compareTo in order to defeat hash attack.
     * Though I don't think our project will suffer from hash attack...
     * @param o the object to be compared.
     * @return int value...
     */
    @Override
    public int compareTo(WrittenFile o) {
        return this.targetPath.compareTo(o.targetPath);
    }
}