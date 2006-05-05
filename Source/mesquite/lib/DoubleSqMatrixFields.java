/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison. Version 1.1, May 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.lib;import java.awt.*;import java.util.*;import mesquite.lib.*;import mesquite.lib.characters.*;/*===============================================*//** a collection of fields for doubles */public class DoubleSqMatrixFields  {	ExtensibleDialog dialog;	SingleLineTextField [][] textFields;	double[][] matrix;	String[] labels;	boolean upperRight;	boolean validDouble=false;	boolean lastValueEditable = false;	/*.................................................................................................................*/	public DoubleSqMatrixFields (ExtensibleDialog dialog, double[][] matrix, String[] labels, boolean upperRight, int fieldLength) {		super();		this.dialog = dialog;		this.labels =labels;		this.matrix = matrix;		initDoubleSqMatrixFields(matrix,upperRight,fieldLength);	}	/*.................................................................................................................*/	public DoubleSqMatrixFields (ExtensibleDialog dialog, int matrixSize, boolean upperRight, int fieldLength) {		super();		this.dialog = dialog;		matrix = new double[matrixSize][matrixSize];		for (int i = 0; i<matrixSize; i++)			for (int j = 0; j<matrixSize; j++) 				matrix[i][j] = MesquiteDouble.unassigned;		initDoubleSqMatrixFields(matrix,upperRight,fieldLength);	}	/*.................................................................................................................*/	public void initDoubleSqMatrixFields (double[][]  matrix, boolean upperRight, int fieldLength) {		this.upperRight = upperRight;		textFields = new SingleLineTextField [matrix.length][matrix[0].length]; //protect against non-square or null matrices!			        GridBagLayout gridBag = new GridBagLayout();	        GridBagConstraints constraints = new GridBagConstraints();	        constraints.gridwidth=1;	        constraints.gridheight=1;	        constraints.fill=GridBagConstraints.BOTH;	        		Panel newPanel = new Panel();		newPanel.setLayout(gridBag);	        gridBag.setConstraints(newPanel,constraints);	     	constraints.gridy = 1;	        constraints.gridx=3;		newPanel.add(new Label("To:"),constraints);			     	constraints.gridy = 2;		for (int i = 1; i<matrix.length; i++) {			constraints.gridx=i+2;			newPanel.add(new Label("" +labels[i]+ "  "),constraints);		}			        constraints.gridx=1;	     	constraints.gridy = 3;		newPanel.add(new Label("From: "),constraints);		TextField lastField = null;		int numRows = matrix.length;		if (upperRight)			numRows --;		for (int i = 0; i<numRows; i++)  { //cycle through rows 			 constraints.gridy=i+3;			 constraints.gridx=2;			newPanel.add(new Label("  " + labels[i] + "  "),constraints);			for (int j = 0; j<matrix[0].length; j++) {				 constraints.gridx=j+2;				if (!upperRight || j>i) {					if (matrix[i][j]==MesquiteDouble.unassigned)						textFields[i][j] = new SingleLineTextField("",fieldLength);					else						textFields[i][j] = new SingleLineTextField(MesquiteDouble.toString(matrix[i][j]),fieldLength);					textFields[i][j].setBackground(Color.white);					lastField = textFields[i][j];					newPanel.add(textFields[i][j],constraints);									}			}		}		if (!lastValueEditable && lastField!=null) {			lastField.setEditable(false);			lastField.setBackground(dialog.getBackground());		}		dialog.addNewDialogPanel(newPanel);	}	/*.................................................................................................................*/	public boolean getValidDouble () {		return validDouble;	}	/*.................................................................................................................*/	public void setLastValueEditable (boolean editable) {		 lastValueEditable = editable;	}	/*.................................................................................................................*/	public boolean getLastValueEditable () {		return lastValueEditable;	}	/*.................................................................................................................*/	public double getValue (int row, int column) {		String s = textFields[row][column].getText();		double value = MesquiteDouble.fromString(s);		validDouble=true;		if (!MesquiteDouble.isCombinable(value)) {			validDouble = false;			value=matrix[row][column];					}	        return value;	}}