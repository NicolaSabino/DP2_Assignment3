//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.01.20 at 12:15:55 PM CET 
//


package it.polito.dp2.RNS.sol1.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for commonPlaceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="commonPlaceType">
 *   &lt;complexContent>
 *     &lt;extension base="{}identifiedEntityType">
 *       &lt;sequence>
 *         &lt;element name="nextPlace" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "commonPlaceType", propOrder = {
    "nextPlace"
})
@XmlSeeAlso({
    Place.class
})
public class CommonPlaceType
    extends IdentifiedEntityType
{

    @XmlSchemaType(name = "anyURI")
    protected List<String> nextPlace;

    /**
     * Gets the value of the nextPlace property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nextPlace property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNextPlace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getNextPlace() {
        if (nextPlace == null) {
            nextPlace = new ArrayList<String>();
        }
        return this.nextPlace;
    }

}
