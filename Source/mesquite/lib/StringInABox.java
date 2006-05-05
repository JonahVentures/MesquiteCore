/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.Version 1.1, May 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.lib;import java.awt.*;import java.util.*;/* ======================================================================== *//** A utility class to draw a string in a box, useful for printing and screen display.  A bit like TextArea, butwithout rectangle around it and with multipage printing facility.  The string, font, and width are given to it, and it calculates a breakdown of the string into an array of strings each of which will fit in the allottedwidth, and also calcualted is how high of a rectangle is needed to accommodate all of the text.  */public class StringInABox {	Font font = null;	StringBuffer sb = null;	StringBuffer sMunched = null;	int width = 0;	int height = 0;	static final char linebreak = '\n';	static int spacesPerTab = 4;	static final char space = ' ';	int buffer = 2;	static final int leftMargin = 0;	String[] strings;	Color textColor, edgeColor;	/* ----------------------------------------------- */	public StringInABox(StringBuffer s, Font f, int w){		font = f;		width = w;		sb = s;		munch();	}	/* ----------------------------------------------- */	public StringInABox(String s, Font f, int w){		font = f;		width = w;		if (s==null)			s = "";		sb = new StringBuffer(s);		munch();	}	/* ----------------------------------------------- */	public void setBuffer(int b){		buffer = b;	}	public void setStringAndFont(StringBuffer s, Font f){		sb = s;		font = f;		munch();	}	public void setFont(Font f){		if (font==f)			return;		font = f;		munch();	}	public void setWidth(int w){		if (width==w)			return;		width = w;		munch();	}	public int getWidth(){		return width;	}	public void setString(StringBuffer s){		sb = s;		munch();	}	public void setString(String s){		if (s==null)			s = "";		sb = new StringBuffer(s);			munch();	}	public String getString(){		if (sb!=null)			return sb.toString();		else			return null;	}	/* ----------------------------------------------- */	/**Prints the passed string (automatically prints with full page width and on	multiple pages as needed.  \n are used as linebreads.  Text is wrapped as necessary to	fit onto page*/	public static void printText(Frame f, String s, Font font) {		if (s == null || f==null || font==null)			return;		MainThread.setShowWaitWindow(false); 		PrintJob pjob = f.getToolkit().getPrintJob(f, "Print?", null); 		if (pjob != null) {			Dimension dim = pjob.getPageDimension();			StringBuffer sB = new StringBuffer(s);			StringInABox sBox = new StringInABox(sB,font, dim.width);  //TODO: should set font here!!!			int tot =  sBox.getHeight();			int lastY = 0;			boolean done = false; 			while (lastY<tot && !done){				int r = sBox.getRemainingHeight(lastY);				if (r> dim.height)					r=dim.height;				Graphics pg = pjob.getGraphics();	 			if (pg!=null) {	 				lastY = sBox.draw(pg, 0, 0, lastY, r);   					pg.dispose();				}				else done = true;  			} 		} 		pjob.end();		MainThread.setShowWaitWindow(true);	}	/* ----------------------------------------------- */	/**Prints the passed string using given PrintJob (automatically prints with full page width and on	multiple pages as needed.  \n are used as linebreads.  Text is wrapped as necessary to	fit onto page*/	public static void printText(PrintJob pjob, String s, Font font) {		if (s == null || font==null || pjob==null)			return;		Dimension dim = pjob.getPageDimension();		StringBuffer sB = new StringBuffer(s);		StringInABox sBox = new StringInABox(sB, font, dim.width);  		int tot =  sBox.getHeight();		int lastY = 0;		boolean done = false; 		while (lastY<tot && !done){			int r = sBox.getRemainingHeight(lastY);			if (r> dim.height)				r=dim.height;			Graphics pg = pjob.getGraphics(); 			if (pg!=null) { 				lastY = sBox.draw(pg, 0, 0, lastY, r);   				pg.dispose();			}			else done = true;		}	}	/* ----------------------------------------------- */	private boolean lineEnd(char c){		return (c=='\n' || c=='\r');	}	/* ----------------------------------------------- */	private boolean isPunc(char c){		return (c=='\n' || c=='\r' || c==',' || c==';' || c==':' || c==')' || c==']'|| c=='}'|| c=='!'|| c=='?'|| c=='-');	}	/* ----------------------------------------------- */	/* ----------------------------------------------- */	/* takes stringBuffer and inserts line breaks as needed to satisfy available width, does some other processing	(e.g., tabs to spaces, \r to \n), then converts to an array of strings*/	private void munch(){		if (sb!=null && font!=null && width>0 && sb.length()>0){			sMunched = new StringBuffer(sb.length());						FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);			int pos = 0;			int i=0;			long count = 0;			do {				count++;				if (count>100000) {					//MesquiteMessage.warnProgrammer("munch in StringInABox exceeded 100000 iterations (i " + i + " sb.length() " + sb.length() + ") ");					return;				}				char nextChar = sb.charAt(i);				if (lineEnd(nextChar)) {					sMunched.append(linebreak);					pos = 0;					i++;				}				else if (nextChar == '\t'){  //tab					for (int j = 1; j<spacesPerTab && pos<width; j++) {						sMunched.append(space);						pos += fm.charWidth(space);					}					if (pos>width) {						sMunched.append(linebreak);						pos = 0;					}					i++;				}				else if (nextChar == space) {					sMunched.append(space);					pos += fm.charWidth(space);					if (pos>width) {						sMunched.append(linebreak);						pos = 0;					}					i++;				}				else if (StringUtil.punctuation(nextChar, null)) {					sMunched.append(nextChar);					pos += fm.charWidth(nextChar);					if (pos>width) {						sMunched.append(linebreak);						pos = 0;					}					i++;				}				else {					int wordLength = 0;					int j = i;					char c = sb.charAt(j);					while (j<sb.length() && !StringUtil.punctuation(c, null) && !StringUtil.whitespace(c, null)) { //measuring next word						wordLength +=fm.charWidth(c);						j++;						if (j<sb.length())							c = sb.charAt(j);					}					if ((wordLength>width && pos==0) || pos+wordLength<width){ 								//single word wider than width; break into pieces;  or, will fit and og as far as can						c = sb.charAt(i);						while (i<sb.length() && !StringUtil.punctuation(c, null) && !StringUtil.whitespace(c, null) && pos<width) {							sMunched.append(c);							pos += fm.charWidth(c);							i++;							if (i<sb.length())								c = sb.charAt(i);						}					}					else {						sMunched.append(linebreak);						pos = 0;					}				}			} while(i<sb.length());						    			StringTokenizer tokenizer = new StringTokenizer(sMunched.toString(), ""+ linebreak);  			storeInStrings(tokenizer);    		}	}	/* ----------------------------------------------- */	private void storeInStrings(StringTokenizer tokenizer){		strings = new String[tokenizer.countTokens()];		int i=0;		while (tokenizer.hasMoreTokens()) {			strings[i] = tokenizer.nextToken();			i++;		}		}	/* To use StringUtil.highlightString (only works for horizontal text)*/	public void setColors(Color textColor, Color edgeColor){		this.textColor = textColor;		this.edgeColor = edgeColor;	}	/* ----------------------------------------------- */	/** draw text based at x, y, starting at local position y in box for height h*/	public int getNextPageTop(int yPos, int h){		if (sb!=null && sMunched!=null && strings!=null && font!=null && width>0){			int lastDrawnYPos = yPos;			FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);			int increment = fm.getMaxAscent() + fm.getMaxDescent() + buffer;			int accumulatedHeight = increment;			int i = 0;			while (i<strings.length && accumulatedHeight<yPos) {				accumulatedHeight += increment;				i++;			 }			int heightOnThisPage = increment;			while (i<strings.length && heightOnThisPage<h) {				lastDrawnYPos = heightOnThisPage + accumulatedHeight;				heightOnThisPage += increment;				i++;			}				return lastDrawnYPos;		}		return 0;	}	/* ----------------------------------------------- */	TextRotator textRotator;	/** draw text based at x, y, starting at local position y in box for height h*/	public int draw(Graphics g, int x, int y, int yPos, int h, Component component, boolean drawHorizontal){		if (sb!=null && sMunched!=null && strings!=null && font!=null && width>0){			int lastDrawnYPos = yPos;			g.setFont(font); //use g's font if font is null			FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);			int increment = fm.getMaxAscent() + fm.getMaxDescent() + buffer;			int accumulatedHeight = increment;			int i = 0;			while (i<strings.length && accumulatedHeight<yPos) {				accumulatedHeight += increment;				i++;			 }			 if (!drawHorizontal && textRotator==null)			 	textRotator = new TextRotator(MesquiteInteger.maximum(10, strings.length));			int heightOnThisPage = increment;			while (i<strings.length && heightOnThisPage<h) {				if (drawHorizontal) {					if (textColor !=null && edgeColor!=null) {						StringUtil.highlightString(g, strings[i], x, y + heightOnThisPage, textColor, edgeColor);					}					else						g.drawString(strings[i], x, y + heightOnThisPage);  				}				else					textRotator.drawRotatedText(strings[i], i, g, component, x + heightOnThisPage, y);					 				lastDrawnYPos = y + heightOnThisPage + accumulatedHeight;				heightOnThisPage += increment;				i++;			}				return lastDrawnYPos;		}		return 0;	}	/* ----------------------------------------------- */	/** draw text based at x, y, starting at local position y in box for height h*/	public int draw(Graphics g, int x, int y, int yPos, int h){		return draw(g,x,y,yPos,h, null, true);	}	/* ----------------------------------------------- */	/** draw text based at x, y*/	public int draw(Graphics g, int x, int y){		return draw(g, x, y, 0, getHeight());	}	/* ----------------------------------------------- */	/** draw text based at x, y*/	public int drawInBox(Graphics g, Color backgroundColor, int x, int y){		int h = getHeight();		Color c = g.getColor();		g.setColor(backgroundColor);		g.fillRect(x-8, y, width+12, h+8);		if (c!=null) g.setColor(c);		g.drawRect(x-8, y, width+11, h+7);		return draw(g, x, y, 0, h);	}	/* ----------------------------------------------- */	public int getHeight(){		if (strings==null)			return 0;		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);		int increment = fm.getMaxAscent() + fm.getMaxDescent() + buffer;		return (strings.length) * increment + buffer;		}	/* ----------------------------------------------- */	public int getRemainingHeight(int usedSoFar){		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);		int increment = fm.getMaxAscent() + fm.getMaxDescent() + buffer;		return getHeight() - usedSoFar/increment*increment + increment+ buffer;		}	/*.................................................................................................................*/	public static void drawStringIfNotBlank(Graphics g, String s, int x, int y){		if (g == null || StringUtil.blank(s))			return;		try {		g.drawString(s, x, y);		}		catch (Exception e){		}	}}