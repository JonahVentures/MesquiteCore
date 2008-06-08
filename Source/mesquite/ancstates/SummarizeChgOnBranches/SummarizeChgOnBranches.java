package mesquite.ancstates.SummarizeChgOnBranches;

/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison.
Version 2.5, June 2008.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.ancstates.lib.*;


/* 
 * 
 * doesn't listen properly to changes
 * 
 * */

/*======================================================================== */
public class SummarizeChgOnBranches extends TreeWindowAssistantA {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(ChgSummarizerMultTrees.class, getName() + " needs a calculator.",
		"This is chosen automatically");
	}
	ChgSummarizerMultTrees summarizerTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		summarizerTask = (ChgSummarizerMultTrees)hireNamedEmployee(ChgSummarizerMultTrees.class,"#SummarizeChanges");

		if (summarizerTask == null) {
			return sorry(getName() + " couldn't start because no summarizer was obtained.");
		}
		summarizerTask.setSensitiveToBranchSelection(true);
		return true;
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("getSummarizer ",summarizerTask);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns the summarizer", null, commandName, "getSummarizer")) {
			return summarizerTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Summarize Changes In Selected Clade";
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Summarizes reconstructions of state changes of a character on descendants of selected branch, over a series of trees.  The branches defined are those that are the descendants of the single branch selected on the tree in the tree window.";
	}
	/*.................................................................................................................*/
//	this is called if used by tree window assistant to show changes on branches
	public   void setTree(Tree tree){
		summarizerTask.setTree(tree);
	}
}

