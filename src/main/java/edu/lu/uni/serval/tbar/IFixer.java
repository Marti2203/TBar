package edu.lu.uni.serval.tbar;

import java.util.List;

import edu.lu.uni.serval.tbar.AbstractFixer.SuspCodeNode;
import edu.lu.uni.serval.tbar.utils.SuspiciousPosition;

/**
 * Fixer Interface.
 * 
 * @author kui.liu
 *
 */
public interface IFixer {

	List<SuspiciousPosition> readSuspiciousCodeFromFile();
	
	List<SuspCodeNode> parseSuspiciousCode(SuspiciousPosition suspiciousCode);

	void fixProcess();
	
}
