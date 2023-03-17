package edu.lu.uni.serval.tbar.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class PathUtils {

	public static ArrayList<String> getSrcPath() {
		ArrayList<String> path = new ArrayList<String>();
		path.add(System.getenv("CLASS_DIRECTORY") );
		path.add(System.getenv("TEST_CLASS_DIRECTORY"));
		path.add(System.getenv("SOURCE_DIRECTORY"));
		path.add(System.getenv("TEST_SOURCE_DIRECTORY"));
		return path;

	}

	public static String getJunitPath() {
		return System.getProperty("user.dir")+"/target/dependency/junit-4.12.jar";
	}
	
	private static String getHamcrestPath() {
		return System.getProperty("user.dir")+"/target/dependency/hamcrest-all-1.3.jar";
	}

	public static String buildCompileClassPath(List<String> additionalPath, String classPath, String testClassPath){
		String path = "\"";
		path += classPath;
		path += System.getProperty("path.separator");
		path += testClassPath;
		path += System.getProperty("path.separator");
		path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		path += System.getProperty("path.separator");
		path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
		path += "\"";
		return path;
	}
	
	public static String buildTestClassPath(String classPath, String testClassPath) {
		String path = "\"";
		path += classPath;
		path += System.getProperty("path.separator");
		path += testClassPath;
		path += System.getProperty("path.separator");
		path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		path += System.getProperty("path.separator");
	    path += getJunitPath();
	    path += System.getProperty("path.separator");
	    path += getHamcrestPath();
	    path += System.getProperty("path.separator");
		path += "\"";
		return path;
    }

}
