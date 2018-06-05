package engine.android.http.util.xml;

import android.text.TextUtils;

import engine.android.http.util.xml.SAXParserFactory.SAXParser.Node;
import engine.android.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SAX解析器工厂
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class SAXParserFactory {

    private static final AtomicReference<SAXParserFactory> factory
    = new AtomicReference<SAXParserFactory>();

    private SAXParserFactory() {}

    public static SAXParserFactory getInstance() {
        SAXParserFactory f = factory.get();
        if (f == null)
        {
            factory.compareAndSet(null, new SAXParserFactory());
            f = factory.get();
        }

        return f;
    }

    public SAXParser newSAXParser() {
        return new SAXParser();
    }

    /**
     * 生成XML请求包（懒人方法）
     */
    public static String generateRequestPackage(HttpPackage p) {
        Map<String, String> map = new HashMap<String, String>();
        p.request(map);
        if (map.isEmpty())
        {
            return null;
        }

        StringBuilder sb = new StringBuilder(map.size() * 10);
        for (Entry<String, String> entry : map.entrySet())
        {
            sb.append(StringUtil.getXMLString(entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }

    /**
     * 解析XML回应包（懒人方法）
     * 
     * @param sax SAX解析器
     * @param end 结束节点名称
     */
    public static void parseResponePackage(HttpPackage p, SAXParser sax, String endTag) {
        p.response(parseKeyValue(sax, endTag));
    }

    /**
     * 解析XML键值对
     * 
     * @param sax SAX解析器
     * @param endTag 结束节点名称
     */
    public static Map<String, String> parseKeyValue(SAXParser sax, String endTag) {
        Map<String, String> map = new HashMap<String, String>();
        Node node;
        while ((node = sax.next()) != null)
        {
            String name = node.getName();
            if (node.getType() != Node.END)
            {
                String value = node.getContext();
                map.put(name, value);
            }
            else if (endTag != null && endTag.equals(name))
            {
                break;
            }
        }

        return map;
    }

    /**
     * SAX解析器
     */
    public static final class SAXParser {

        public static final String ISO          = StringUtil.ISO;

        public static final String UTF_8        = StringUtil.UTF_8;

        private InputStream is;                 // 要解析的流数据

        private boolean parseText;              // 是否正在解析文本

        private String encoding;                // 编码格式

        private String decoding;                // 解码格式

        SAXParser() {};

        /**
         * 设置编码
         * 
         * @param encoding 编码格式
         */

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        /**
         * 设置解码
         * 
         * @param decoding 解码格式
         */

        public void setDecoding(String decoding) {
            this.decoding = decoding;
        }

        public void parse(InputStream is) {
            this.is = is;
        }

        public Node next() {
            if (is == null)
            {
                return null;
            }

            try {
                char c;
                while ((c = (char) is.read()) != (char) -1)
                {
                    if (c != '<' && !parseText)
                    {
                        continue;
                    }

                    if (c == '<')
                    {
                        c = (char) is.read();
                    }

                    parseText = false;

                    if (c == '/')
                    {
                        Node node = new Node(Node.END);
                        StringBuilder sb = new StringBuilder();
                        while ((c = (char) is.read()) != '>')
                        {
                            sb.append(c);
                        }

                        node.name = toEncodingString(sb.toString());
                        parseText(node);
                        return node;
                    }
                    else if (c == '!' || c == '?')
                    {
                        if ((c = (char) is.read()) == '-')
                        {
                            Node node = new Node(Node.START);
                            node.name = "!--";
                            StringBuilder sb = new StringBuilder();
                            while ((c = (char) is.read()) != '>')
                            {
                                if (c != '-')
                                {
                                    sb.append(c);
                                }
                            }

                            node.context = toEncodingString(sb.toString());
                            return node;
                        }

                        while ((c = (char) is.read()) != '>');
                    }
                    else
                    {
                        Node node = new Node(Node.START);
                        StringBuilder sb = new StringBuilder();
                        sb.append(c);
                        while ((c = (char) is.read()) != '>')
                        {
                            if (c == ' ')
                            {
                                node.name = toEncodingString(sb.toString());
                                parseAttr(node);
                                parseText(node);
                                return node;
                            }
                            else if (c == '/')
                            {
                                node.type = Node.START_END;
                                node.name = toEncodingString(sb.toString());
                                is.read();
                                parseText(node);
                                return node;
                            }

                            sb.append(c);
                        }

                        node.name = toEncodingString(sb.toString());
                        parseText(node);
                        return node;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private void parseAttr(Node node) {
            StringBuilder sb = new StringBuilder();
            char c;
            try {
                while ((c = (char) is.read()) != '>')
                {
                    if (c == '/')
                    {
                        node.type = Node.START_END;
                        continue;
                    }

                    if (c != '=')
                    {
                        sb.append(c);
                    }
                    else
                    {
                        char sqot = ' ';
                        String key = toEncodingString(sb.toString().trim());
                        sb.delete(0, sb.length());
                        c = (char) is.read();
                        if (c == '"')
                        {
                            sqot = '"';
                        }
                        else if (c == '\'')
                        {
                            sqot = '\'';
                        }
                        else
                        {
                            sb.append(c);
                        }

                        while ((c = (char) is.read()) != sqot && c != '>')
                        {
                            sb.append(c);
                        }

                        if (node.attributes == null)
                        {
                            node.attributes = new HashMap<String, String>();
                        }

                        node.attributes.put(key, toEncodingString(sb.toString().trim()));
                        sb.delete(0, sb.length());
                        if (c == '>')
                        {
                            return;
                        }
                    }
                }

                if (sb.toString().trim().length() != 0)
                {
                    if (node.attributes == null)
                    {
                        node.attributes = new HashMap<String, String>();
                    }

                    node.attributes.put(toEncodingString(sb.toString().trim()), "");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void parseText(Node node) {
            parseText = true;
            StringBuilder sb = new StringBuilder();
            char c;
            boolean b = false;
            try {
                while ((c = (char) is.read()) != (char) -1)
                {
                    if (c == '<' && !b)
                    {
                        break;
                    }
                    else if (c == '\'')
                    {
                        b = !b;
                    }

                    sb.append(c);
                }

                node.context = toEncodingString(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String toEncodingString(String s) {
            if ((encoding == null && decoding == null)
            ||  (encoding != null && encoding.equals(decoding)))
            {
                return s;
            }

            return StringUtil.toEncodingString(s, decoding, encoding);
        }

        public static final class Node {

            public static final byte START = 1;

            public static final byte END = 2;

            public static final byte START_END = 3;

            byte type;

            String name;

            Map<String, String> attributes;

            String context;

            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("节点类型：");
                if (type == START)
                {
                    sb.append("开始节点");
                }
                else if (type == END)
                {
                    sb.append("结束节点");
                }
                else
                {
                    sb.append("空节点");
                }

                sb.append("\t节点名称：" + name);
                if (attributes != null)
                {
                    sb.append("\t节点属性：");
                    sb.append(attributes.toString());
                }

                if (!TextUtils.isEmpty(context))
                {
                    sb.append("\t节点文本：" + context);
                }

                return sb.toString();
            }

            Node(byte type) {
                this.type = type;
            }

            public byte getType() {
                return type;
            }

            public String getName() {
                return name;
            }

            public Map<String, String> getAttributes() {
                return attributes;
            }

            public String getContext() {
                return context;
            }

            public String getAttribute(String name) {
                if (attributes == null)
                {
                    return null;
                }

                return attributes.get(name);
            }
        }
    }
}