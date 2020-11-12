/**
 * 
 */
package lslrec.dataStream.family.stream.lsl;

import com.sun.jna.Pointer;

/**
 * @author Manuel Merino Monge
 * 
 * From:
 *  https://github.com/labstreaminglayer/liblsl-Java/blob/39799dae02edf34e138d2a67ae768dc38a0248a9/src/edu/ucsd/sccn/LSL.java
 * 
 */

//=====================
// ==== XML Element ====
// =====================

/**
 * A lightweight XML element tree; models the .desc() field of stream_info.
 * Has a name and can have multiple named children or have text content as value; attributes are omitted.
 * Insider note: The interface is modeled after a subset of pugixml's node type and is compatible with it.
 * See also http://pugixml.googlecode.com/svn/tags/latest/docs/manual/access.html for additional documentation.
 */
public class XMLElement 
{
	private LSLDll inst = LSL.getDllInstance();
	
    public XMLElement(Pointer handle) { obj = handle; }

    // === Tree Navigation ===

    /** Get the first child of the element. */
    public XMLElement first_child() { return new XMLElement(inst.lsl_first_child(obj)); }

    /** Get the last child of the element. */
    public XMLElement last_child() { return new XMLElement(inst.lsl_last_child(obj)); }

    /** Get the next sibling in the children list of the parent node. */
    public XMLElement next_sibling() { return new XMLElement(inst.lsl_next_sibling(obj)); }

    /** Get the previous sibling in the children list of the parent node. */
    public XMLElement previous_sibling() { return new XMLElement(inst.lsl_previous_sibling(obj)); }

    /** Get the parent node. */
    public XMLElement parent() { return new XMLElement(inst.lsl_parent(obj)); }


    // === Tree Navigation by Name ===

    /** Get a child with a specified name. */
    public XMLElement child(String name) { return new XMLElement(inst.lsl_child(obj,name)); }

    /** Get the next sibling with the specified name. */
    public XMLElement next_sibling(String name) { return new XMLElement(inst.lsl_next_sibling_n(obj, name)); }

    /** Get the previous sibling with the specified name. */
    public XMLElement previous_sibling(String name) { return new XMLElement(inst.lsl_previous_sibling_n(obj, name)); }


    // === Content Queries ===

    /** Whether this node is empty. */
    public boolean empty() { return inst.lsl_empty(obj)!=0; }

    /** Whether this is a text body (instead of an XML element). True both for plain char data and CData. */
    public boolean is_text() { return inst.lsl_is_text(obj) != 0; }

    /** Name of the element. */
    public String name() { return (inst.lsl_name(obj)); }

    /** Value of the element. */
    public String value() { return (inst.lsl_value(obj)); }

    /** Get child value (value of the first child that is text). */
    public String child_value() { return (inst.lsl_child_value(obj)); }

    /** Get child value of a child with a specified name. */
    public String child_value(String name) { return (inst.lsl_child_value_n(obj,name)); }


    // === Modification ===

    /**
     * Append a child node with a given name, which has a (nameless) plain-text child with the given text value.
     */
    public XMLElement append_child_value(String name, String value) { return new XMLElement(inst.lsl_append_child_value(obj, name, value)); }

    /**
     * Prepend a child node with a given name, which has a (nameless) plain-text child with the given text value.
     */
    public XMLElement prepend_child_value(String name, String value) { return new XMLElement(inst.lsl_prepend_child_value(obj, name, value)); }

    /**
     * Set the text value of the (nameless) plain-text child of a named child node.
     */
    public boolean set_child_value(String name, String value) { return inst.lsl_set_child_value(obj, name, value) != 0; }

    /**
     * Set the element's name.
     * @return False if the node is empty.
     */
    public boolean set_name(String rhs) { return inst.lsl_set_name(obj, rhs) != 0; }

    /**
     * Set the element's value.
     * @return False if the node is empty.
     */
    public boolean set_value(String rhs) { return inst.lsl_set_value(obj, rhs) != 0; }

    /** Append a child element with the specified name. */
    public XMLElement append_child(String name) { return new XMLElement(inst.lsl_append_child(obj, name)); }

    /** Prepend a child element with the specified name. */
    public XMLElement prepend_child(String name) { return new XMLElement(inst.lsl_prepend_child(obj, name)); }

    /** Append a copy of the specified element as a child. */
    public XMLElement append_copy(XMLElement e) { return new XMLElement(inst.lsl_append_copy(obj, e.obj)); }

    /** Prepend a child element with the specified name. */
    public XMLElement prepend_copy(XMLElement e) { return new XMLElement(inst.lsl_prepend_copy(obj, e.obj)); }

    /** Remove a child element with the specified name. */
    public void remove_child(String name) { inst.lsl_remove_child_n(obj, name); }

    /** Remove a specified child element. */
    public void remove_child(XMLElement e) { inst.lsl_remove_child(obj, e.obj); }

    private Pointer obj;
}


