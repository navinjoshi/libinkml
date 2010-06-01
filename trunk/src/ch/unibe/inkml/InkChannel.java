package ch.unibe.inkml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ch.unibe.inkml.util.Formatter;

/**
 * Represent an InkML Channel element.
 * The following describtion is copied from  http://www.w3.org/TR/InkML/#channel
 * 
 * Channels are described using the <channel> element, with various attributes.
 * The required name attribute specifies the interpretation of the channel in the trace data.
 * The reserved channel names are given by the enum Name
 * 
 * The type attribute defines the encoding type for the channel (either boolean, decimal, or integer). 
 * If type is not specified, it defaults to decimal.
 * 
 * A default value can be specified for the channel using the default attribute; 
 * the use of default values within a trace is described in the next section. 
 * If no default is specified, it is assumed to be zero for integer and 
 * decimal-valued channels, and false for boolean channels.
 * 
 * The min and max attributes, if given, specify the minimum and maximum 
 * possible values for a channel of type integer or decimal. If neither is 
 * given, then there is no a prior bound on the channel values. If one is 
 * given, then the channel values are bounded above or below but unbounded 
 * in the other direction. If both are given, then all channel values must 
 * fall within the specified range.
 * 
 * The orientation attribute is applicable to channels of integer or decimal 
 * type. It gives the meaning of increasing value. For example, whether X 
 * increases to the left or the right. The value may be given as "+ve" 
 * or "-ve", with "+ve" being the default.
 * These values are covered by the enum type Orientation.
 * 
 * The respectTo attribute specifies the origin for channels of integer or 
 * decimal type. For time channels, this is given as a URI for a <timestamp> 
 * element. For other dimensions the meaning is application-dependent.
 * 
 * Typically, a channel in the <traceFormat> will map directly to a corresponding 
 * channel provided by the digitizing device, and its values as recorded in the trace 
 * data will be the original channel values recorded by the device. 
 * However, for some applications, it may be useful to store normalized channel values instead, 
 * or even to remap the channels provided by the digitizing device to different channels in 
 * the trace data. This correspondence between the trace data and the device channels 
 * is recorded using a <mapping> element (described in the Mappings section) within 
 * the <channel> element. If no mapping is specified for a channel, it is assumed to be unknown.
 * 
 * 3.1.4 Orientation Channels
 * The channels OTx, OTy, OA, OE and OR are defined for recording of pen orientation data. 
 * Implementers may choose to use either pen azimuth OA and pen elevation OE, or alternatively 
 * tilt angles OTx and OTy. The latter are the angles of projections of the pen axis onto the 
 * XZ and YZ planes, measured from the vertical. It is often useful to record the sine of this 
 * angle, rather than the angle itself, as this is usually more useful in calculations 
 * involving angles. The <mapping> element can be employed to specify an applied sine transformation.
 * 
 * The third degree of freedom in orientation is generally defined as the rotation of the 
 * pen about its axis. This is potentially useful (in combination with tilt) in application 
 * such as illustration or calligraphy, and signature verification.
 * 
 * 3.1.5 Color Channels
 * The channels CR, CG, CB, CC, CM, CY, CK and C are defined to record color data as 
 * captured by an optical device, software settings or other means.
 * 
 * The channels CR, CG, CB provide an additive color model for the colors 
 * red, green and blue. The channels CC, CY, CM, CK provide a subtractive color 
 * model for the colors cyan, magenta, yellow and black. The channel C provides a 
 * mechanism to give color as a single numerical value, e.g. as a gray scale or a 
 * device-dependent hex-encoded number.
 * 
 * Color channels are intended for use when these values are part of the data 
 * itself and hence potentially changing from one sample to the next. Strokes 
 * with constant color may more economically be described with reference to a <brush> element.
 * 
 * 3.1.6 Width Channel
 * The channel W is provided for recording stroke width. This allows optical devices 
 * to record measured stroke width and allows applications that generate InkML 
 * to specify desired width for rendering. The value is in length units, measured 
 * orthogonally to the stroke direction.
 * 
 * As with the color channels, the width channel is intended for use when 
 * this quantity is part of the data itself and hence potentially changing f
 * rom one sample to the next. Strokes with constant width may more economically 
 * be described with reference to a <brush> element.
 * 
 * 3.1.7 Time Channel
 * The time channel allows for detailed recording of the timing information for 
 * each sample point within a trace. This can be useful if the digitizing device 
 * has a non-uniform sampling rate, for example, or in cases where duplicate 
 * point data is removed for the sake of compactness.
 * 
 * The time channel can be specified as either a regular or intermittent channel.
 *  When specified as a regular channel, the single quote prefix can be used to 
 *  record incremental time between successive points. Otherwise, the value of 
 *  the time channel for a given sample point is defined to be the timestamp of 
 *  that point in the units and frame of reference specified by its corresponding 
 *  <inkSource> description (more precisely, by the <traceFormat> element for the channel).
 * 
 * As with the other predefined channels, the meaning of the integer or decimal 
 * values recorded by the time channel in a given trace is defined by the 
 * <inkSource> information associated with the trace's traceFormat. In the 
 * case of the time channel, its <channel> element contains both a units 
 * and respectTo attribute.
 * 
 * The units attribute gives the units of the recorded time values, and 
 * the relativeTo attribute describes the frame of reference for those 
 * recorded values. The value of the relativeTo attribute is a reference 
 * to a time stamp. If it is not given, the time channel values are relative 
 * to the beginning timestamps of the individual traces in which they appear.
 * 
 * The following example defines a time channel whose values for a 
 * given point are the relative to the timestamp refered to to by #ts1:
 * 
 * <channel name="T"
 *             type="integer"
 *             units="ms"
 *             respectTo="#ts1"/>
 * 
 * If no <inkSource> information is provided, or if no value is specified 
 * for the respectTo attribute, the ink processor cannot make any assumption 
 * about the relative timing of points within different traces. Likewise, if no 
 * units are specified, no assumption can be made about the units of the time channel data.
 * @author emanuel
 *
 */

abstract public class InkChannel extends InkUniqueElement {
	
    /**
     * The type attribute defines the encoding type for the channel 
     * (either boolean, decimal, or integer). 
     * If type is not specified, it defaults to decimal.
     *
     */
	public static enum Type { INTEGER, DECIMAL, BOOLEAN ;
		public String toString(){
			return super.toString().toLowerCase();
		}
	};
	
	public static enum Orientation {P,M};
	
	/**
	 * The required name attribute specifies the interpretation of the channel in the trace data
	 *
	 */
	public static enum Name {
    	    /**
    	     * X coordinate. This is the horizontal pen position on the 
    	     * writing surface, increasing to the right for +ve orientation.
    	     */
	        X, 
	        /**
	         * Y coordinate. This is the vertical position on the 
	         * writing surface, increasing downward for +ve orientation.
	         */
	        Y, 
	        /**
	         * Z coordinate. This is the height of pen above the 
	         * writing suface, increasing upward for +ve orientation.
	         */
	        Z, 
	        /**
	         * pen tip force
	         */
	        F, 
	        /**
	         * tip switch state (touching/not touching the writing surface)
	         */
	        S, 
	        /**
	         * B1..Bn side button states
	         */
	        B1,
	        B2, 
	        B3, 
	        B4, 
	        B5, 
	        /**
	         * tilt along the x-axis
	         */
	        OTX,
	        /**
	         * tilt along the y-axis
	         */
	        OTY, 
	        /**
	         * azimuth angle of the pen (yaw)
	         */
	        OA, 
	        /**
	         * elevation angle of the pen (pitch)
	         */
	        OE, 
	        /**
	         * rotation (rotation about pen axis)
	         */
	        OR, 
	        /**
	         * color value (device specific encoding)
	         */
	        C, 
	        /**
	         * RED
	         */
	        CR, 
	        /**
	         * GREEN
	         */
	        CG, 
	        /**
	         * BLUE
	         */
	        CB, 
	        /**
	         * CYAN
	         */
	        CC, 
	        /**
	         * Mangenta
	         */
	        CM,
	        /**
	         * Yellow
	         */
	        CY,
	        /**
	         * Black
	         */
	        CK,
	        /**
	         * stroke width (orthogonal to stroke)
	         */
	        W,
	        /**
	         * Time (of the sample point)
	         */
	        T
	        }

	private Name name;
	private Orientation orientation = Orientation.P; 
	private String units;
	private boolean intermittent = false;
	protected boolean maxSet;
	protected boolean minSet;
	
	private InkMapping mapping;
	
	/**
	 * Creates a Channel from an InkML XML DOM node, by identifying the type and selecting
	 * one of subclass of InkChannel
	 * @param ink
	 * @param node
	 * @return
	 * @throws InkMLComplianceException
	 */
	public static InkChannel channelFactory(InkInk ink,Element node) throws InkMLComplianceException{
		String pureT = node.getAttribute("type");
		for(Type t : Type.values()){
			if(pureT.equals(t.toString())){
				InkChannel c =  channelFactory(t,ink);
				c.buildFromXMLNode(node);
				return c;
			}
		}
		InkChannel c = channelFactory(Type.DECIMAL,ink);
		c.buildFromXMLNode(node);
		return c;
	}
	
	/**
	 * Creates a Channel from the type of the channel specified by "t"
	 * @param t
	 * @param ink
	 * @return
	 */
	public static InkChannel channelFactory(Type t,InkInk ink){
		switch(t){
		case INTEGER:
			return new InkChannelInteger(ink);
		case BOOLEAN:
			return new InkChannelBoolean(ink);
		default:
			return new InkChannelDouble(ink);
		}
	}
	
	
	public InkChannel(InkInk ink){
		super(ink);
	}
	
	/**
	 * Returns the name of the channel which in InkML is the string used to specify
	 * the purpose of the given Channel. 
	 * @see Name
	 * @return
	 */
	public Name getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * @see Name
	 * @see this.getName()
	 * @param name
	 * @return
	 */
	public InkChannel setName(Name name) {
		this.name = name;
		return this;
	}
	
	/**
	 * Returns the type of the values of this channel
	 * @see Type
	 * @return
	 */
	public abstract Type getType();
	
	/**
	 * Returns the default value if ? is given in a intermittend Channel.
	 * @return
	 */
	abstract public Object getDefaultValue();
	/**
	 * You can specify the default value that shoud be produced by an intermittend Channel when ? is given.
	 * @param defaultValue
	 */
	abstract public void setDefaultValue(String defaultValue);
	
	/**
	 * Returns the minimum value of the channel 
	 * @return
	 * @throws InkMLComplianceException
	 */
	abstract public double getMin() throws InkMLComplianceException;
	
	/**
	 * Set the minimum value
	 * @param min
	 * @throws InkMLComplianceException
	 */
	abstract public void setMin(String min) throws InkMLComplianceException;
	
	/**
	 * Returns the maximum value of this Channel
	 * @return
	 * @throws InkMLComplianceException
	 */
	abstract public double getMax() throws InkMLComplianceException;
	
	/**
	 * Set the maximum value of this channel
	 * @param max
	 * @throws InkMLComplianceException
	 */
	abstract public void setMax(String max) throws InkMLComplianceException;
	
	/**
	 * Returns true if the is a maximum value specified
	 * @return
	 */
	public boolean isMax(){
		return this.maxSet;
	}
	/**
	 * Returns true if there is a minimum value specified
	 * @return
	 */
	public boolean isMin(){
		return this.minSet;
	}
	
	/**
	 * Returns the orientation used for this channel
	 * @see Orientation
	 * @return
	 */
	public Orientation getOrientation() {
		return orientation;
	}
	
	/**
	 * You can set the orientation
	 * @see Orientation
	 * @param orientation
	 */
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}
	/**
	 * Returns the unit used to measure the values of this channel
	 * @return
	 */
	public String getUnits() {
		return units;
	}
	
	/**
	 * Sets the unit used to measure the values of this channel
	 * @param units
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * Returns the value in as Object which is specified as string
	 * @param value
	 * @return
	 */
	abstract public Object parse(String value);
	
	/**
	 * Returns the value as double which is specified as string
	 * if boolean 0 or 1 is returned
	 * if int a double very near to the int value is returned
	 * @param value
	 * @return
	 */
	public double parseToDouble(String value){
	    return doublize(parse(value));
	}

	/**
	 * True if this channel is an intermittent channel 
	 * @return
	 */
	public boolean isIntermittent() {
		return this.intermittent ;
	}
	
	/**
	 * Specify that this channel is an intermittent channel.
	 * @param i
	 */
	public void setIntermittent(boolean i){
		this.intermittent = i;
	}

	/**
	 * Formatter factory method. Returns the formatter 
	 * which will work with this Channel.
	 * @return
	 */
	@SuppressWarnings("unchecked")
    abstract public Formatter formatterFactory();
	
	
	@Override
	public void buildFromXMLNode(Element node) throws InkMLComplianceException {
		super.buildFromXMLNode(node);
		//type and id is done allready
		//name
		this.name = Name.valueOf(node.getAttribute("name"));
		if(this.name == null){
			throw new InkMLComplianceException("A channel has the unsupported name '"+node.getAttribute("name")+"'");
		}
		//default
		this.setDefaultValue(node.getAttribute("default"));
		//min
		this.setMin(node.getAttribute("min"));
		//max
		this.setMax(node.getAttribute("max"));
		//orientation
		String o = node.getAttribute("orientation");
		if(o.equals("-ve")){
			this.setOrientation(Orientation.M);
		}else{
			this.setOrientation(Orientation.P);
		}
		//units
		if(node.hasAttribute("units")){
			this.units = node.getAttribute("units");
		}
		 NodeList list = node.getElementsByTagName("mapping");
		 if(list.getLength() > 0){
			 this.mapping = InkMapping.mappingFactory(getInk(), (Element)list.item(0));
		 }
	}
	
	@Override
	public void exportToInkML(Element tf) throws InkMLComplianceException {
		Element c = tf.getOwnerDocument().createElement("channel");
		//id
		if(this.getId() != null){
			c.setAttribute("xml:id", this.getId());
		}
		//name
		c.setAttribute("name", this.getName().toString());
		//type
		if(this.getType() != Type.DECIMAL){
			c.setAttribute("type", this.getType().toString());
		}
		//default
		this.exportDefaultToInkML(c);
		//min
		if(this.minSet){
			c.setAttribute("min", Double.toString(this.getMin()));
		}
		//max
		if(this.maxSet){
			c.setAttribute("max", Double.toString(this.getMax()));
		}
		//orientation
		if(this.orientation != Orientation.P){
			c.setAttribute("orientation","-ve");
		}
		//units
		if(this.units != null){
			c.setAttribute("units", this.units);
		}
		tf.appendChild(c);
		if(this.mapping != null){
			this.mapping.exportToInkML(c);
		}
	}

	protected abstract void exportDefaultToInkML(Element c);
	
	/**
	 * Returns the prefix for ids
	 * @return
	 */
	public String getPrefix(){
		return "channel";
	}

    /**
     * @param o
     */
    abstract public double doublize(Object o);

    /**
     * @param d
     * @return
     */
    abstract public Object objectify(double d);
	
}
