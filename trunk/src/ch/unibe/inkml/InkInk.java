package ch.unibe.inkml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.NotImplementedException;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.inkml.util.Timespan;
import ch.unibe.inkml.util.TraceBound;
import ch.unibe.inkml.util.ViewTreeManipulationException;


public class InkInk extends InkAnnotatedElement implements Observer {
	
	public static final Aspect ON_CHANGE = new Aspect(){};

	/**
	 * The definitions element, enables access to all elements with referenceIds 
	 * not only those which are defined in definintions
	 */
    private InkDefinitions definitions;	

    public static final Aspect ON_TRACE_REMOVED = new Aspect(){};


	/**
	 * List of traces (leaf or containers) that are direct children of ink (have no parent)
	 */
	private List<InkTrace> traces;

	/**
	 * List of trace views (leaf or containers), that are direct children of ink (have no parent).
	 */
	private List<InkTraceView> views;
	
	/**
	 * This is the current InkContext used when new traces are added.
	 * if no context is defined this is an instance of InkDefaultContext
	 */
	private InkContext currentContext;


	private int traceNumber;

	
	/**
	 * Constructs the actual InkML tree, without Ink, no document can exist.
	 */
	public InkInk() {
		super(null);
		this.traces = new ArrayList<InkTrace>();
		this.views = new ArrayList<InkTraceView>();
	}

	/**
	 * Returns the context which is currently active.
	 * If no context is active, the default context, as defined by InkML is 
	 * added.
	 * @return
	 */
	public InkContext getCurrentContext() {
		if(currentContext==null){
			try {
				currentContext = new InkDefaultContext(this);
			} catch (InkMLComplianceException e) {
				e.printStackTrace();
			}
		}
		return currentContext;
	}

	/**
	 * Sets a context active. This means that all traces created
	 * afterwards will have this context as the context they live in.
	 * Formerly created traces keep their context.
	 * @param currentContext
	 */
	public void setCurrentContext(InkContext currentContext) {
		this.currentContext = currentContext;
	}

	/**
	 * Gets the current brush. This is a shortcut for
	 * getCurrentContext().getBrush() since brush can only be refered to
	 * within a context, or on the Trace(View) directly.
	 * @return
	 */
	public InkBrush getCurrentBrush() {
		return getCurrentContext().getBrush();
	}


	public void setDefinitions(InkDefinitions inkDefinitions) {
		this.definitions = inkDefinitions;
	}


	public InkDefinitions getDefinitions() {
		return definitions;
	}


	public void addView(InkTraceView inkTraceView) {
		views.add(inkTraceView);
		inkTraceView.registerFor(InkTraceView.ON_CHANGE, this);
	}
	
    @Override
    public void notifyFor(Aspect event, Object subject) {
        if(event == InkTraceView.ON_CHANGE){
            //System.err.println("Ink has recieved change");
            notifyObserver(InkInk.ON_CHANGE, subject);
        }
    }


	
	@Override
	public void buildFromXMLNode(Element node) throws InkMLComplianceException {
		super.buildFromXMLNode(node);
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling()){
			if(child.getNodeType() == Node.ELEMENT_NODE){
				stepNode((Element)child);
			}
		}
		
	}

	private void stepNode(Element node) throws InkMLComplianceException{
		String n = node.getNodeName();
		if(n.equals("definitions")){
			definitions = new InkDefinitions(this.getInk());
			definitions.buildFromXMLNode(node);
		}else if(n.equals("definition")){ // backwards compatibility for definition-instead-of-definitions bug
		    definitions = new InkDefinitions(this.getInk());
            definitions.buildFromXMLNode(node);
		}else if(n.equals("context")){
			InkContext context = new InkContext(this);
			context.buildFromXMLNode((Element)node);
			this.getInk().getDefinitions().enter(context);
			this.getInk().setCurrentContext(context);
		}else if(n.equals("traceView")){
			InkTraceView view = InkTraceView.createTraceView(this.getInk(),null,node);
			addView(view);
		}else if(n.equals("trace")){
			InkTraceLeaf t = new InkTraceLeaf(this.getInk(),null);
			t.buildFromXMLNode(node);
			this.addTrace(t);
		}else if(n.equals("traceGroup")){
			InkTraceGroup g = new InkTraceGroup(this.getInk(),null);
			g.buildFromXMLNode(node);
			this.addTrace(g);
		}
	}

	public void addTrace(InkTrace inkTrace) {
		if(!inkTrace.testFormat(inkTrace.getContext().getCanvasTraceFormat())){
			System.err.println("trace is not well formated");
			inkTrace.testFormat(inkTrace.getContext().getCanvasTraceFormat());
		}
		this.traces.add(inkTrace);
	}


	public int getTraceNumber() {
		return traceNumber ++;
	}




	@Override
	public void exportToInkML(Element node) throws InkMLComplianceException {
		super.exportToInkML(node);
		this.getDefinitions().exportToInkML(node);
		if(this.currentContext!= null){
			this.currentContext.exportToInkML(node);
		}
		for(InkTrace t: this.traces){
			t.exportToInkML(node);
		}
		for(InkTraceView t: this.views){
			t.exportToInkML(node);
		}
	}




	public List<InkTrace> getTraces() {
		return new ArrayList<InkTrace>(traces);
	}
	public List<InkTrace> getFlatTraces(){
	    ArrayList<InkTrace> leafs = new ArrayList<InkTrace>();
	    for(InkTrace trace:traces){
	        if(trace.isLeaf()){
	            leafs.add((InkTraceLeaf) trace);
	        }else{
	            leafs.addAll(((InkTraceGroup)trace).getFlattenedTraceLeafs());
	        }
	    }
		return leafs;
	}

	public TraceBound getBounds() {
		TraceBound bound = new TraceBound();
		for(InkTrace s : getTraces()){
			bound.add(s.getBounds());
		}
		return bound;
	}

	public void setTime(InkTrace stroke, Double time) {
		throw new NotImplementedException();
	}

	public List<InkTraceViewLeaf> getFlatTraceViewLeafs() {
		return this.getViewRoot().getFlattenedTraceLeafs();
	}


	public InkTraceViewContainer getViewRoot() {
		if(this.views.size() > 0){
			return (InkTraceViewContainer) this.views.get(0);
		}
		else{
			InkTraceViewContainer tv = new InkTraceViewContainer(this,null);
			addView(tv);
			return tv;
		}
	}

	public List<InkTraceView> getViewRoots(){
	    return views;
	}

	public InkInk getInk(){
		return this;
	}

	public void exportToInkML(org.w3c.dom.Document document2) throws InkMLComplianceException {
		Element ink = document2.createElement("ink");
		document2.appendChild(ink);
		this.exportToInkML(ink);
	}





	public Timespan getTimeSpan() {
		Timespan ts = new Timespan();
		for(InkTrace l : this.getTraces()){
			ts.add(l.getTimeSpan());
		}
		return ts;
	}

	public void removeView(InkTraceView inkTraceViewContainer) {
		views.remove(inkTraceViewContainer);
		inkTraceViewContainer.unregisterFor(ON_ALL, this);
	}


	public void annotate(String name, String value){
	    super.annotate(name,value);
	    notifyObserver(ON_CHANGE,this);
	}



	public void reloadTraces() throws InkMLComplianceException {
		for(InkTrace l : this.getFlatTraces()){
			((InkTraceLeaf)l).reloadPoints();
		}
		
	}

	public void removeTrace(InkTrace trace) {
		if(traces.contains(trace)){
			traces.remove(trace);
		}
		if(!trace.isRoot()){
			trace.getParent().remove(trace);
		}

		for(InkTraceView view : getFlatTraceViewLeafs()){
			if(((InkTraceViewLeaf) view).getTrace() == trace){
				if(!view.isRoot()){
					try {
                        view.remove();
                    } catch (ViewTreeManipulationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
				}
			}
		}
		if(this.definitions.containsValue(trace)){
			this.definitions.remove(trace.getId());
		}
	}





	public static InkInk loadFromXMLDocument(Document document) throws InkMLComplianceException {
		Node node = document.getDocumentElement();
		if(node != null && node.getNodeName().equals("ink")){
			InkInk ink = new InkInk();
			ink.buildFromXMLNode((Element) node);
			return ink;
		}else{
			throw new InkMLComplianceException("XML tree do not contain 'ink' root element");
		}
	}



}
