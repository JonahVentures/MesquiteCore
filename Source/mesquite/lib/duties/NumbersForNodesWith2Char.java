/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.Version 1.1, May 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.lib.duties;import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;/* ======================================================================== *//**Suppliies numbers for each node of a tree.*/public abstract class NumbersForNodesWith2Char extends NumbersForNodes {	private CharSourceCoordObed characterSource, characterSourceB;	Object dataCondition;	int currentCharA = 0;	int currentCharB = 1;	Taxa taxa;	CharacterDistribution observedStatesA, observedStatesB;	/*.................................................................................................................*/	/** superStartJob is called automatically when an employee is hired.  This is intended for use by superclasses of modules that need	their own constructor-like call, without relying on the subclass to be polite enough to call super.startJob().*/	public boolean superStartJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName){		if (NumbersForNodesWith2Char.class.isAssignableFrom(getHiredAs()))			return true;		//assume hired as NumbersForNodes; thus responsible for getting characters		//hire character source with two characters -- one or two separate sources? 		characterSource = (CharSourceCoordObed)hireCompatibleEmployee(commandRec, CharSourceCoordObed.class, getCharacterClass(), "Source of characters (for " + getName() + ")");		if (characterSource == null)			return sorry(commandRec, getName() + " couldn't start because no source of characters (for " + getName() + ") was obtained");		characterSourceB = characterSource;		addMenuItem( "Choose 1st Character (for " + getName() + ")", makeCommand("chooseCharacterA",  this));		addMenuItem( "Choose 2nd Character (for " + getName() + ")", makeCommand("chooseCharacterB",  this));		return true;	}	/*.................................................................................................................*/  	 public Class getCharacterClass() {  	 	return null;  	 }	/*.................................................................................................................*/  	 public Snapshot getSnapshot(MesquiteFile file) {    	 	Snapshot temp = new Snapshot();  	 	temp.addLine("getCharacterSource ", characterSource);   	 	temp.addLine("setCharacterA " + CharacterStates.toExternal(currentCharA));   	 	if (characterSourceB!= characterSource)  	 		temp.addLine("getCharacterSourceB ", characterSourceB);   	 	temp.addLine("setCharacterB " + CharacterStates.toExternal(currentCharB));   	 	return temp;  	 }  	 MesquiteInteger pos = new MesquiteInteger();	/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {    	 	if (checker.compare(this.getClass(), "Sets module supplying characters", "[name of module]", commandName, "setCharacterSource")) {//temporary, for data files using old system without coordinators 			if (characterSource!=null) 				return characterSource.doCommand(commandName, arguments, commandRec, checker);    	 	}    	 	else if (checker.compare(this.getClass(), "Returns module supplying characters", null, commandName, "getCharacterSource")) { 			return characterSource;    	 	}    	 	else if (checker.compare(this.getClass(), "Sets module supplying characters", "[name of module]", commandName, "setCharacterSourceB")) {//temporary, for data files using old system without coordinators 			if (characterSourceB!=null) 				return characterSourceB.doCommand(commandName, arguments, commandRec, checker);    	 	}    	 	else if (checker.compare(this.getClass(), "Returns module supplying characters", null, commandName, "getCharacterSourceB")) { 			return characterSourceB;    	 	}    	 	else if (checker.compare(this.getClass(), "Goes to next character for X axis", null, commandName, "nextCharacterA")) {    	 		if (characterSource == null)    	 			return null;    	 		if (currentCharA >= characterSource.getNumberOfCharacters(taxa, commandRec)-1)    	 			currentCharA=0;    	 		else    	 			currentCharA++;   	 		observedStatesA = null;	   	 	parametersChanged(null, commandRec);    	 	}    	 	else if (checker.compare(this.getClass(), "Goes to previous character for X axis", null, commandName, "previousCharacterA")) {    	 		if (characterSource == null)    	 			return null;    	 		if (currentCharA<=0)    	 			currentCharA=characterSource.getNumberOfCharacters(taxa, commandRec)-1;    	 		else    	 			currentCharA--;   	 		observedStatesA = null; 			parametersChanged(null, commandRec);    	 	}    	 	else if (checker.compare(this.getClass(), "Queries the user to select a character for X axis", null, commandName, "chooseCharacterA")) {    	 		if (characterSource == null)    	 			return null;    	 		if (taxa==null)    	 			return null;    	 		int ic=characterSource.queryUserChoose(taxa, " for X axis ", commandRec);    	 		if (MesquiteInteger.isCombinable(ic)) {	   			currentCharA = ic;	   	 		observedStatesA = null;	 			parametersChanged(null, commandRec); 			}			    	 	}    	 	else if (checker.compare(this.getClass(), "Sets the character to show for X axis", "[number of character]", commandName, "setCharacterA")) {    	 		if (characterSource == null)    	 			return null;    	 		int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments));    	 		if ((ic>=0) && (ic<=characterSource.getNumberOfCharacters(taxa, commandRec)-1)) {    	 			currentCharA = ic;	   	 		observedStatesA = null;	 			parametersChanged(null, commandRec); 			}    	 	}    	 	else if (checker.compare(this.getClass(), "Goes to next character for Y axis", null, commandName, "nextCharacterB")) {    	 		if (characterSourceB == null)    	 			return null;    	 		if (currentCharB >= characterSourceB.getNumberOfCharacters(taxa, commandRec)-1)    	 			currentCharB=0;    	 		else    	 			currentCharB++; 			observedStatesB = null;			parametersChanged(null, commandRec);    	 	}    	 	else if (checker.compare(this.getClass(), "Goes to previous character for Y axis", null, commandName, "previousCharacterB")) {    	 		if (characterSourceB == null)    	 			return null;    	 		if (currentCharB<=0)    	 			currentCharB=characterSourceB.getNumberOfCharacters(taxa, commandRec)-1;    	 		else    	 			currentCharB--; 			observedStatesB = null;			parametersChanged(null, commandRec);    	 	}    	 	else if (checker.compare(this.getClass(), "Queries the user to select a character for Y axis", null, commandName, "chooseCharacterB")) {    	 		if (characterSourceB == null)    	 			return null;    	 		if (taxa==null)    	 			return null;    	 		int ic=characterSourceB.queryUserChoose(taxa, " for Y axis ", commandRec);    	 		if (MesquiteInteger.isCombinable(ic)) {	   			currentCharB = ic;	   	 		observedStatesB = null;	 			parametersChanged(null, commandRec); 			}			    	 	}    	 	else if (checker.compare(this.getClass(), "Sets the second character to use for " + getName(), "[number of character]", commandName, "setCharacterB")) {    	 		if (characterSourceB == null)    	 			return null;    	 		int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments));    	 		if ((ic>=0) && (ic<=characterSourceB.getNumberOfCharacters(taxa, commandRec)-1)) {    	 			currentCharB = ic;   	 			observedStatesB = null; 				parametersChanged(null, commandRec); 			}    	 	}     	 	else    	 		return super.doCommand(commandName, arguments, commandRec, checker);    	 	return null;    	 }	/*.................................................................................................................*/ 	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification, CommandRecord commandRec) {		if (employee instanceof CharacterOneSource) {	   	 	observedStatesA = null;	   	 	observedStatesB = null;	   	 	parametersChanged(notification, commandRec); 		}	}   	 public Class getDutyClass() {   	 	return NumbersForNodesWith2Char.class;   	 } 	public String getDutyName() { 		return "Numbers for Nodes of Tree using 2 Character Distributions";   	 }	public void calculateNumbers(Tree tree, NumberArray result, MesquiteString resultString, CommandRecord commandRec){		if (tree!=null)			taxa = tree.getTaxa(); 		if (observedStatesA ==null) 			observedStatesA = characterSource.getCharacter(tree, currentCharA, commandRec); 		if (observedStatesB ==null) 			observedStatesB = characterSource.getCharacter(tree, currentCharB, commandRec);		calculateNumbers(tree, observedStatesA, observedStatesB, result, resultString, commandRec);	}	/*.................................................................................................................*/	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */ 	public void employeeQuit(MesquiteModule employee) { 		if (employee == characterSource || employee == characterSourceB)  // character source quit and none rehired automatically 			iQuit();	}   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/   	public void initialize(Tree tree, CommandRecord commandRec){   		if (tree == null)   			return;		taxa = tree.getTaxa();   		if (characterSource!=null)   			characterSource.initialize(tree.getTaxa(), commandRec);   		if (characterSourceB!=null && characterSourceB!=characterSource)   			characterSourceB.initialize(tree.getTaxa(), commandRec);   	}	public abstract void calculateNumbers(Tree tree, CharacterDistribution charDistribution1, CharacterDistribution charDistribution2, NumberArray result, MesquiteString resultString, CommandRecord commandRec); }