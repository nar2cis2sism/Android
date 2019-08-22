package engine.android.http.util.xml;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Xml节点组装
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public final class XmlNode {

    public static char QUOT = '"';

    private final String name;

    private Map<String, String> attributes;

    private String context;

    private List<XmlNode> children;

    public XmlNode(String name) {
        this.name = name;
    }

    public XmlNode setAttribute(String key, String value) {
        if (TextUtils.isEmpty(value))
        {
            return this;
        }

        if (attributes == null)
        {
            attributes = new HashMap<String, String>();
        }

        attributes.put(key, value);
        return this;
    }

    public XmlNode setContext(String context) {
        this.context = context;
        return this;
    }

    public XmlNode addChild(XmlNode child) {
        if (child == null)
        {
            return this;
        }

        if (children == null)
        {
            children = new LinkedList<XmlNode>();
        }

        children.add(child);
        return this;
    }

    private void appendXml(StringBuilder xml) {
        xml.append("<").append(name);
        if (attributes != null)
        {
            for (Entry<String, String> entry : attributes.entrySet())
            {
                xml.append(" ")
                .append(entry.getKey())
                .append("=")
                .append(QUOT)
                .append(entry.getValue())
                .append(QUOT);
            }
        }

        if (children != null)
        {
            xml.append(">");
            for (XmlNode child : children)
            {
                child.appendXml(xml);
            }

            xml.append("</")
            .append(name)
            .append(">");
        }
        else if (TextUtils.isEmpty(context))
        {
            xml.append(" />");
        }
        else
        {
            xml.append(">")
            .append(context)
            .append("</")
            .append(name)
            .append(">");
        }
    }

    @Override
    public String toString() {
        StringBuilder xml = new StringBuilder();
        appendXml(xml);
        return xml.toString();
    }
}