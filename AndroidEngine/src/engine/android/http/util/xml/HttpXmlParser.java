package engine.android.http.util.xml;

import engine.android.http.HttpResponse;
import engine.android.http.util.HttpParser;
import engine.android.http.util.xml.SAXParserFactory.SAXParser;
import engine.android.http.util.xml.SAXParserFactory.SAXParser.Node;

/**
 * Xml解析器
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public abstract class HttpXmlParser implements HttpParser {

    private final SAXParserFactory factory = SAXParserFactory.getInstance();

    protected SAXParser initParser(SAXParserFactory factory) {
        return factory.newSAXParser();
    }

    @Override
    public Object parse(HttpResponse response) throws Exception {
        SAXParser parser = initParser(factory);
        parser.parse(response.getInputStream());
        return parse(parser, parser.next());
    }

    /**
     * 需要子类实现
     * 
     * @param parser Sax解析器
     * @param root 根节点
     */
    protected abstract Object parse(SAXParser parser, Node root) throws Exception;

    protected static final void parseChildNode(SAXParser parser, Node node,
            HttpXmlNodeHandler childHandler) {
        if (node.getType() != Node.START)
        {
            return;
        }

        String tag = node.getName();
        while ((node = parser.next()) != null)
        {
            String name = node.getName();
            if (node.getType() != Node.END)
            {
                if (childHandler.handle(node))
                {
                    break;
                }
            }
            else if (tag.equals(name))
            {
                break;
            }
        }
    }

    public interface HttpXmlNodeHandler {

        /**
         * @return True表示终止当前节点解析
         */
        boolean handle(Node node);
    }
}