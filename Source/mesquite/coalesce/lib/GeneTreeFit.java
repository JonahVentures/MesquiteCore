/* Mesquite source code.  Copyright 1997-2006 W. Maddison. Version 1.1, May 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.coalesce.lib;/*~~  */import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.coalesce.lib.*;import mesquite.assoc.lib.*;/* ======================================================================== *//** This evaluates a gene tree by calculating how much lineage sorting is implied by a containing species tree. */public abstract class GeneTreeFit extends ObjFcnForTree {	MesquiteNumber nt;	OneTreeSource treeSourceTask;	AssociationSource associationTask;	ReconstructAssociation reconstructTask;	MesquiteString treeSourceName;	TaxaAssociation association;	Taxa containingTaxa;	Tree lastTree, lastContaining;	MesquiteCommand tstC;	/*.................................................................................................................*/	public boolean superStartJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {		//todo: allow choices && put in setHiringCommand		associationTask = (AssociationSource)hireEmployee(commandRec, AssociationSource.class, "Source of taxon associations");		if (associationTask == null)			return sorry(commandRec, getName() + " couldn't start because no source of taxon associatons obtained.");				//todo: if this is going to be for deep coalescences, should hire just that reconstructor.  Otherwise change name of this throughout to neutral		reconstructTask = (ReconstructAssociation)hireEmployee(commandRec, ReconstructAssociation.class, "Method to reconstruct association history");		if (reconstructTask == null)			return sorry(commandRec, getName() + " couldn't start because module to reconstruct association histories obtained.");		treeSourceTask = (OneTreeSource)hireEmployee(commandRec, OneTreeSource.class, "Source of containing trees");		if (treeSourceTask == null)			return sorry(commandRec, getName() + " couldn't start because source of containing trees obtained.");		tstC =  makeCommand("setTreeSource",  this);		treeSourceTask.setHiringCommand(tstC);		treeSourceName = new MesquiteString(treeSourceTask.getName());		if (numModulesAvailable(TreeSource.class)>1) {			MesquiteSubmenuSpec mss = addSubmenu(null, "Species Tree Source",tstC, OneTreeSource.class);			mss.setSelected(treeSourceName);		} 		nt= new MesquiteNumber(); 		return true;  	 }	public void employeeQuit(MesquiteModule m){		if (m != treeSourceTask)			iQuit();	}  	 	/*.................................................................................................................*/  	 public Snapshot getSnapshot(MesquiteFile file) {    	 	Snapshot temp = new Snapshot();  	 	temp.addLine("setTreeSource ", treeSourceTask);   	 	return temp;  	 }	MesquiteInteger pos = new MesquiteInteger();	/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {    	 	if (checker.compare(this.getClass(), "Sets the source of the species tree", "[name of module]", commandName, "setTreeSource")) {			OneTreeSource temp = (OneTreeSource)replaceEmployee(commandRec, OneTreeSource.class, arguments, "Source of trees", treeSourceTask);			if (temp !=null){				treeSourceTask = temp;				treeSourceTask.setHiringCommand(tstC);				treeSourceName.setValue(treeSourceTask.getName());				parametersChanged(null, commandRec);    	 			return treeSourceTask;    	 		}    	 	} 		else { 			return super.doCommand(commandName, arguments, commandRec, checker);		}		return null;   	 }   	    	    	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/   	public void initialize(Tree tree, CommandRecord commandRec){   		if (tree ==null)   			return;   		Taxa taxa = tree.getTaxa();        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {        		association = associationTask.getCurrentAssociation(taxa, commandRec);         		if (association.getTaxa(0)== taxa)        			containingTaxa = association.getTaxa(1);        		else        			containingTaxa = association.getTaxa(0);        	}   	}   	MesquiteTree t;	MesquiteString r = new MesquiteString();	/*.................................................................................................................*/	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {    	 	if (result==null || tree == null)    	 		return;		result.setToUnassigned();		lastTree = tree;		Taxa taxa = tree.getTaxa();        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {        		association = associationTask.getCurrentAssociation(taxa, commandRec);         		if (association == null)        			association = associationTask.getAssociation(taxa, 0, commandRec);         		if (association == null){				if (resultString!=null)					resultString.setValue("Deep coalescence (gene tree) not calculated (no association )");				return;			}        		if (association.getTaxa(0)== taxa)        			containingTaxa = association.getTaxa(1);        		else        			containingTaxa = association.getTaxa(0);        	}        	/*tree coming in is gene tree, hence need to find out for each taxon in gene tree if it has more than one associate.  If so, then         	can't do calculations since gene copy in more than one species*/		for (int i=0; i< taxa.getNumTaxa(); i++){			Taxon tax = taxa.getTaxon(i);			if (association.getNumAssociates(tax)>1){				if (resultString!=null)					resultString.setValue("Deep coalescence not calculated (some genes in more than one species)");				return;			}		}				//get containing tree		Tree containingTree = ((OneTreeSource)treeSourceTask).getTree(containingTaxa, commandRec); 				//cloning the contained tree in case we need to change its resolution	        if (t==null || t.getTaxa()!=taxa || !(tree instanceof MesquiteTree))	        	t = tree.cloneTree();	        else	        	((MesquiteTree)t).setToClone((MesquiteTree)tree);	        		        			//calculate deep coalescence cost	    calculateCost(reconstructTask, containingTree, t, association, result, r, commandRec);		lastContaining = containingTree;		if (resultString!=null)			resultString.setValue(r.toString());	}	public abstract void calculateCost(ReconstructAssociation reconstructTask, Tree speciesTree, MesquiteTree geneTree, TaxaAssociation association, MesquiteNumber result, MesquiteString r, CommandRecord commandRec);	/*.................................................................................................................*/    	 public String getParameters() {		if (lastContaining !=null)			return "Species tree " + lastContaining.getName();		return "";   	 }   	public boolean biggerIsBetter() {		return false;	}}