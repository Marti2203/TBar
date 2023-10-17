package edu.lu.uni.serval.tbar.faultlocalization;

import edu.lu.uni.serval.tbar.config.Configuration;
import edu.lu.uni.serval.tbar.dataprepare.DataPreparer;
import edu.lu.uni.serval.tbar.faultlocalization.Metrics.Metric;
import edu.lu.uni.serval.tbar.utils.FileHelper;
import edu.lu.uni.serval.tbar.utils.PathUtils;
import edu.lu.uni.serval.tbar.utils.ShellUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class FL {

	private static Logger log = LoggerFactory.getLogger(FL.class);
	
	public DataPreparer dp = null;
	public List<SuspiciousCode> suspStmts;
	
	/**
	 * Input: 
	 * 		1. Defects4J project path. e.g., ../Defects4JData/
	 * 		2. Output path: e.g., suspiciousCodePositions/
	 * 		3. The project name: e.g., Chart_1
	 * 
	 * Output:
	 * 		1. a ranked list of suspicious statements for a buggy project.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 3) {
			System.err.println("Arguments: \n"
					+ "\t<Output_Path>: the directory of storing fault localization report. \n"
					+ "\t<Bug_Data_Path>: the directory of checking out programming task. \n"
					+ "\t<project_name>: completion id of each question, such as has_close_elements. \n");
			System.exit(0);
		}

		String outputPath = args[0];//args[0]; // Configuration.SUSPICIOUS_POSITIONS_FILE_APTH;
		String buggy_proj_path = args[1];//args[1]; // Configuration.BUGGY_PROJECTS_PATH;
		String projectName = args[2];//args[2] hce_11
		String metricStr = "Ochiai";
		
		FL fl = new FL();
		fl.locateSuspiciousCode(buggy_proj_path, projectName, outputPath, metricStr);
	}
	
	public void locateSuspiciousCode(String path, String buggyProject, String outputPath, String metricStr) {
		// System.out.println(buggyProject);
		// String fullBuggyProjectPath = path + buggyProject;
		// try{
		// 	ShellUtils.shellRun(Arrays.asList("mvn -f " + fullBuggyProjectPath + "/pom.xml " + "clean compile test-compile"), buggyProject, 1);
		// }catch (IOException e){
		// 	log.debug(buggyProject + "Fail to compile test");
		// }
		if (dp == null) {
			dp = new DataPreparer(path);
			dp.prepareData(buggyProject);
		}
		if (!dp.validPaths) return;

		GZoltarFaultLoclaization gzfl = new GZoltarFaultLoclaization();
		gzfl.threshold = 0.0;
		gzfl.maxSuspCandidates = -1;
        String srcPath = path + buggyProject + "/" + PathUtils.getSrcPath().get(2);
		gzfl.srcPath = srcPath;
		
		try {
			gzfl.localizeSuspiciousCodeWithGZoltar(dp.classPaths, checkNotNull(Arrays.asList("")), dp.testCases);
		} catch (NullPointerException e) {
			log.error(buggyProject + "\n" + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		System.out.println(metricStr);
		Metric metric = new Metrics().generateMetric(metricStr);
		gzfl.sortSuspiciousCode(metric);
		
		suspStmts = new ArrayList<SuspiciousCode>(gzfl.candidates.size());
		suspStmts.addAll(gzfl.candidates);
        
		StringBuilder builder = new StringBuilder();
		for (int index = 0, size = suspStmts.size(); index < size; index ++) {
			SuspiciousCode candidate = suspStmts.get(index);
			String className = candidate.getClassName();
			int lineNumber = candidate.lineNumber;
			builder.append(className).append("@").append(lineNumber)
				.append("@").append(candidate.getSuspiciousValueString()).append("\n");
		}
		FileHelper.outputToFile(outputPath + "/" + buggyProject + "/" + metricStr + ".txt", builder, false);
	}
}
