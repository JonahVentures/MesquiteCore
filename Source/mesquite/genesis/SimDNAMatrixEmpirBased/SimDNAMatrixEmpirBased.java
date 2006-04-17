/* Mesquite (package mesquite.stochchar).  Copyright 1997-2005 W. Maddison and D. Maddison. Version 1.06, August 2005.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.genesis.SimDNAMatrixEmpirBased;import java.util.*;import java.awt.*;import java.awt.event.*;import mesquite.categ.lib.*;import mesquite.genesis.lib.SimulationDNAModel;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.stochchar.lib.*;/* ======================================================================== */public class SimDNAMatrixEmpirBased extends CharMatrixSource implements Incrementable {	int currentDataSet=0;	long originalSeed=System.currentTimeMillis(); //0L;	Random rng;	MesquiteLong seed;	DNAData empiricalData;	OneTreeSource treeTask;	int numMatrices = MesquiteInteger.infinite;	Tree lastTree;	Taxa lastTaxa;		boolean initialized = false;	CharMatrixSource matrixSource;	ProbModelSourceLike modelSource; //ProbModelSourceSim	Vector originalModels, localModels;	/*.................................................................................................................*/ 	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {		loadPreferences();		if (condition != null && condition instanceof CompatibilityTest)			condition = ((CompatibilityTest) condition).getAcceptedClass();		matrixSource = (CharMatrixSource) hireNamedEmployee(CommandRecord.scriptingRecord, CharMatrixSource.class, "#StoredMatrices", DNAState.class);		if (matrixSource == null)			return sorry(commandRec, "Simulate DNA matrix (Emp. Based) could not start because no appropriate source of matrices could be obtained");		if (RandomBetween.askSeed && !commandRec.scripting()) {			long response = MesquiteLong.queryLong(containerOfModule(), "Seed for Matrix simulation", "Set Random Number seed for Matrix simulation:", originalSeed);			if (MesquiteLong.isCombinable(response))				originalSeed = response;		}		seed = new MesquiteLong(1);		seed.setValue(originalSeed);		modelSource = (ProbModelSourceLike) hireNamedEmployee(commandRec, ProbModelSourceLike.class, "#CurrentProbModels", DNAState.class);		if (modelSource == null) {			return sorry(commandRec, "Evolve DNA characters could not start because no appropriate model source module could be obtained");		}		if (getHiredAs() != CharMatrixObedSource.class) {			MesquiteMenuItemSpec mm = addMenuItem(null, "Next Simulated Matrix", makeCommand("nextMatrix", this));			mm.setShortcut(KeyEvent.VK_RIGHT); // right			mm = addMenuItem(null, "Previous Simulated Matrix", makeCommand("prevMatrix", this));			mm.setShortcut(KeyEvent.VK_LEFT); // right		}		addMenuItem("Set Seed (Matrix simulation)...", makeCommand("setSeed", this));		rng = new Random(originalSeed);		treeTask = (OneTreeSource) hireEmployee(commandRec, OneTreeSource.class, "Source of tree on which to simulate character evolution");		if (treeTask == null) {			return sorry(commandRec, "Simulated Matrices could not start because no source of trees was obtained");		}		return true;	}  	 	/*.................................................................................................................*/  	 public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification, CommandRecord commandRec) {  	 	if (employee==matrixSource) {  	 		empiricalData = null;  // to force retrieval of new empirical matrix			parametersChanged(notification, commandRec);  	 	}  	 	else if (employee==modelSource) {  	 		empiricalData = null;			parametersChanged(notification, commandRec);  	 	}  	 }	/* ................................................................................................................. */	public boolean isPrerelease(){		return true;	}	/*.................................................................................................................*/	public void endJob(){			if (lastTaxa!=null)				lastTaxa.removeListener(this);		  	 if (originalModels !=null)		  		 originalModels.removeAllElements();		  	 if (localModels !=null)		  		localModels.removeAllElements();		  	 			super.endJob();	}	/*.................................................................................................................*/	/** passes which object is being disposed (from MesquiteListener interface)*/	public void disposing(Object obj){		if (obj instanceof Taxa && (Taxa)obj == lastTaxa) {			iQuit();		}	}	/* ................................................................................................................. */	public Snapshot getSnapshot(MesquiteFile file) {		Snapshot temp = new Snapshot();		if (getHiredAs() != CharMatrixObedSource.class)			temp.addLine("setMatrix " + CharacterStates.toExternal(currentDataSet));		if (MesquiteInteger.isCombinable(numMatrices))			temp.addLine("setNumMatrices " + numMatrices);		temp.addLine("getMatrixSource ", matrixSource);		temp.addLine("getModelSource ", modelSource);		temp.addLine("setSeed " + originalSeed);		temp.addLine("getTreeSource ", treeTask);		return temp;	}	/* ................................................................................................................. */	MesquiteInteger pos = new MesquiteInteger(0);    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {		if (checker.compare(this.getClass(), "Returns the module used to supply matrix", null, commandName, "getMatrixSource")) {			return matrixSource;		}		else if (checker.compare(this.getClass(), "Returns the module used to supply stochastic models", "[name of module]", commandName, "getModelSource")) {			return modelSource;		}		else if (checker.compare(this.getClass(), "Returns the source of trees", null, commandName, "getTreeSource")) {			return treeTask;		}		else if (checker.compare(this.getClass(), "Sets the number of matrices to be simulated", "[number of matrices]", commandName, "setNumMatrices")) {			int newNum = MesquiteInteger.fromFirstToken(arguments, pos);			if (!MesquiteInteger.isCombinable(newNum))				newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set number of matrices", "Number of matrices available from simulations:", numMatrices);			if (MesquiteInteger.isCombinable(newNum) && newNum > 0 && newNum < 1000000 && newNum != numMatrices) {				numMatrices = newNum;				parametersChanged(null, commandRec);			}		}		else if (checker.compare(this.getClass(), "Sets the taxa block used", "[block number]", commandName, "setTaxa")) {			int setNumber = MesquiteInteger.fromFirstToken(arguments, pos); 			if (lastTaxa != null)				lastTaxa.removeListener(this);			lastTaxa = getProject().getTaxa(checker.getFile(), setNumber);			if (lastTaxa != null)				lastTaxa.addListener(this);		}		else if (checker.compare(this.getClass(), "Returns the current tree", null, commandName, "getTree")) {			if (lastTree == null && lastTaxa != null && treeTask != null) {				return treeTask.getTree(lastTaxa, commandRec);			}			else				return lastTree;		}		else if (checker.compare(this.getClass(), "Sets the random number seed to that passed", "[long integer seed]", commandName, "setSeed")) {			long s = MesquiteLong.fromString(parser.getFirstToken(arguments));			if (!MesquiteLong.isCombinable(s)) {				s = MesquiteLong.queryLong(containerOfModule(), "Random number seed", "Enter an integer value for the random number seed for character evolution simulation", originalSeed);			}			if (MesquiteLong.isCombinable(s)) {				originalSeed = s;				seed.setValue(originalSeed);				parametersChanged(null, commandRec); // ?			}		}		else if (checker.compare(this.getClass(), "Simulates the next matrix", null, commandName, "nextMatrix")) {			currentDataSet++;			parametersChanged(null, commandRec);		}		else if (checker.compare(this.getClass(), "Simulates the previous matrix", null, commandName, "prevMatrix")) {			currentDataSet--;			if (currentDataSet >= 0) {				parametersChanged(null, commandRec);			}			else				currentDataSet = 0;		}		else if (checker.compare(this.getClass(), "Sets which character matrix to simulate", "[matrix number]", commandName, "setMatrix")) {			pos.setValue(0);			int icNum = MesquiteInteger.fromString(arguments, pos);			if (!MesquiteInteger.isCombinable(icNum))				return null;			int ic = CharacterStates.toInternal(icNum);			if ((ic >= 0) && (MesquiteInteger.isCombinable(ic))) {				currentDataSet = ic;				parametersChanged(null, commandRec);			}		}		else			return super.doCommand(commandName, arguments, commandRec, checker);		return null;	}    /* ................................................................................................................. */	public void initialize(Taxa taxa, CommandRecord commandRec) {		getEmpirical(taxa, commandRec);		initialized = true;		treeTask.initialize(taxa, commandRec);	}	/* ................................................................................................................. */ 	 void getEmpirical(Taxa taxa, CommandRecord commandRec) { 		 		if (empiricalData == null){			MCharactersDistribution m = matrixSource.getCurrentMatrix(taxa, commandRec);			if (m == null)				return;			empiricalData = (DNAData)m.getParentData();			if (empiricalData != null){  //get local copies of all of the models		  		evolvingStates =(DNACharacterHistory) new DNAState().makeCharacterHistory(taxa, taxa.getNumTaxa());		  		originalModels = new Vector();				localModels = new Vector();				for (int ic = 0; ic< empiricalData.getNumChars(); ic++){			  		SimulationDNAModel icModel = (SimulationDNAModel)modelSource.getCharacterModel(empiricalData, ic, commandRec);			  		if (originalModels.indexOf(icModel)<0){			  			originalModels.addElement(icModel);			  			SimulationDNAModel model = (SimulationDNAModel)icModel.cloneModelWithMotherLink(null); 			  			localModels.addElement(model);			  	   		model.setMaxStateSimulatable(3); 			  		}				}			}		}	 }  	int countt = 0;	DNACharacterHistory evolvingStates;	/*.................................................................................................................*/ 	/** This returns just the states at the terminal **/	private void evolve(ProbabilityCategCharModel model, Tree tree, MDNAAdjustable statesAtTips, int node, int ic) {   		if (node!=tree.getRoot()) {   			long statesAtAncestor = evolvingStates.getState(tree.motherOfNode(node));   			int stateAtAncestor = DNAState.minimum(statesAtAncestor);   			long statesAtNode = DNAState.makeSet(model.evolveState(stateAtAncestor, tree, node));   			evolvingStates.setState(node, statesAtNode);     			if (tree.nodeIsTerminal(node)) {   				statesAtTips.setState(ic, tree.taxonNumberOfNode(node), statesAtNode);   			}   		}		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))				evolve(model, tree, statesAtTips, daughter, ic);	}	/*.................................................................................................................*/  	private MCharactersDistribution getM(Taxa taxa, CommandRecord commandRec){		 		if (treeTask == null) {			System.out.println("Tree task null");			return null;		}		else if (taxa==null){			System.out.println("taxa null");			return null;		}		if (!initialized)			initialize(taxa, commandRec);		else if (empiricalData == null)			getEmpirical(taxa, commandRec);		if (empiricalData == null)			return null;		int numChars = empiricalData.getNumChars(); 		DNAState state = new DNAState();		MDNAAdjustable matrix =  (MDNAAdjustable)state.makeMCharactersDistribution(taxa, numChars, taxa.getNumTaxa());		Tree tree = treeTask.getTree(taxa, commandRec);		if (tree== null){			System.out.println("No tree");			return null;		}  		evolvingStates =(DNACharacterHistory)evolvingStates.adjustHistorySize(tree, evolvingStates);		lastTree = tree;		CharacterDistribution states = null;				rng.setSeed(originalSeed);		long rnd = originalSeed;		for (int it = 0; it<=currentDataSet; it++)			rnd =  rng.nextInt();		seed.setValue(rnd + 1); 				for (int ic = 0; ic < numChars; ic++) {			SimulationDNAModel model = getModel(ic, commandRec);			if (seed != null)				model.setSeed(seed.getValue());			model.initForNextCharacter();			long rootState = model.getRootState(tree);				evolvingStates.setState(tree.getRoot(), rootState); // starting root			evolve(model, tree, matrix, tree.getRoot(), ic);			if (seed != null)				seed.setValue(model.getSeed());		}   		matrix.setName("Matrix #" + CharacterStates.toExternal(currentDataSet)  + " simulated by " + getName());   		matrix.setAnnotation(accumulateParameters(" "), false);   		matrix.setBasisTree(tree);  		return matrix;   	}  	  	private SimulationDNAModel getModel(int ic, CommandRecord commandRec){   		SimulationDNAModel icModel = (SimulationDNAModel)modelSource.getCharacterModel(empiricalData, ic, commandRec);   		int i = originalModels.indexOf(icModel);   		if (i<0){  			originalModels.addElement(icModel);  			SimulationDNAModel model = (SimulationDNAModel)icModel.cloneModelWithMotherLink(null);   			localModels.addElement(model);  	   		model.setMaxStateSimulatable(3);   	   		return model;   		}   		return (SimulationDNAModel)localModels.elementAt(i); 	}	/*.................................................................................................................*/    	 public String getMatrixName(Taxa taxa, int ic, CommandRecord commandRec) {   		return "Matrix #" + CharacterStates.toExternal(ic)  + " simulated by " + getName();   	 }	/*.................................................................................................................*/  	public MCharactersDistribution getCurrentMatrix(Taxa taxa, CommandRecord commandRec){   		return getM(taxa, commandRec);   	}	/*.................................................................................................................*/  	public MCharactersDistribution getMatrix(Taxa taxa, int im, CommandRecord commandRec){   		commandRec.tick("Simulating matrix " + im);   		currentDataSet = im;   		return getM(taxa, commandRec);   	}	/*.................................................................................................................*/    	public  int getNumberOfMatrices(Taxa taxa, CommandRecord commandRec){    		return numMatrices;     	}	/*.................................................................................................................*/   	/** returns the number of the current matrix*/   	public int getNumberCurrentMatrix(){   		return currentDataSet;   	}	/*.................................................................................................................*/ 	public void setCurrent(long i, CommandRecord commandRec){  //SHOULD NOT notify (e.g., parametersChanged) 		currentDataSet = (int)i; 	} 	public long getCurrent(CommandRecord commandRec){ 		return currentDataSet; 	} 	public long getMin(CommandRecord commandRec){		return 0; 	} 	public long getMax(CommandRecord commandRec){		return numMatrices; 	} 	public long toInternal(long i){ 		return i-1; 	} 	public long toExternal(long i){ //return whether 0 based or 1 based counting 		return i+1; 	}	/*.................................................................................................................*/    	 public String getParameters() {				String s =  "";				if (getHiredAs() != CharMatrixObedSource.class)					s += "Matrix #" + CharacterStates.toExternal(currentDataSet) + "; "; 				s += "Simulator: " + getName();				if (lastTree !=null)					s+= "; most recent tree: " + lastTree.getName();								return s + " [seed for matrix sim. " + originalSeed + "]";			   	 }	/*.................................................................................................................*/    	 public String getName() {		return "Sim. DNA Matrices (Based on Empirical)";   	 }	/*.................................................................................................................*/	/** returns whether this module is requesting to appear as a primary choice */   	public boolean requestPrimaryChoice(){   		return true;     	}	/*.................................................................................................................*/  	 public String getExplanation() {		return "Supplies simulated character matrices, based on an empirical matrix, on the current tree.";   	 }   	 	/*.................................................................................................................*/   	 public boolean showCitation(){   	 	return true;   	 }	/*.................................................................................................................*/  	 public CompatibilityTest getCompatibilityTest() {  	 	return new DNAStateTest();  	 }}