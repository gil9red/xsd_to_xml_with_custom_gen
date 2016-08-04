/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Xsd2xml_custom_generator;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamResult;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.XSInstance;
import jlibs.xml.xsd.XSParser;
import org.apache.xerces.xs.XSModel;
import org.apache.xmlbeans.XmlObject;


/**
 *
 * @author ipetrash
 */
public class Xsd2xml_custom_generator {
    public static 
    java.util.List<javax.xml.namespace.QName> getGlobalElements(URL path) throws Exception {
        // http://xmlbeans.apache.org/docs/2.4.0/reference/org/apache/xmlbeans/SchemaTypeSystem.html
        org.apache.xmlbeans.SchemaTypeSystem sts = org.apache.xmlbeans.XmlBeans.compileXsd(
            new org.apache.xmlbeans.XmlObject[] { 
                org.apache.xmlbeans.XmlObject.Factory.parse(path) 
            },
            org.apache.xmlbeans.XmlBeans.getBuiltinTypeSystem(),
            null
        );
        
        java.util.List<javax.xml.namespace.QName> qnames = new java.util.ArrayList();
        for (org.apache.xmlbeans.SchemaGlobalElement el: sts.globalElements()) {
            qnames.add(el.getName());
        }
        
        return qnames;
    }

    static final 
    java.util.List<String> typeXsdList = new java.util.ArrayList() {{
        add("string");
        add("normalizedString");
        add("token");
        add("base64Binary");
        add("hexBinary");

        add("integer");
        add("positiveInteger");
        add("negativeInteger");
        add("nonNegativeInteger");
        add("nonPositiveInteger");
        add("long");
        add("unsignedLong");
        add("int");
        add("unsignedInt");
        add("short");
        add("unsignedShort");
        add("byte");
        add("unsignedByte");
        add("decimal");

        add("float");
        add("double");

        add("boolean");

        add("duration");
        add("dateTime");
        add("date");
        add("time");
        add("gYear");
        add("gYearMonth");
        add("gMonth");
        add("gMonthDay");
        add("gDay");

        add("Name");
        add("QName");
        add("NCName");

        add("anyURI");

        add("language");
        add("ID");
        add("IDREF");
        add("IDREFS");
        add("ENTITY");
        add("ENTITIES");
        add("NOTATION");
        add("NMTOKEN");
        add("NMTOKENS");
    }};

    // Генератор, который вместо значений вставляет тип значения.
    // т.е. если в xsd у элемента тип "xs:decimal", то значение будет "decimal"
    static 
    class TypeNameValueGenerator implements jlibs.xml.xsd.XSInstance.SampleValueGenerator {
        // Функция у указанного типа ищет название его xsd типа. Т.к. тип может быть пользовательским,
        // то получить его xsd тип может не получиться. Для этого и используется эта рекурсивная функция,
        // т.к. я не уверен, что вложенность полей XSSimpleTypeDefinition не может быть только уровней XSSimpleTypeDefinition и
        // XSSimpleTypeDefinition.BaseType.
        // Функция будет рекурсивно спускаться в типы и при первом нахождении типа xsd, возвращать его.
        String findNameType(org.apache.xerces.xs.XSSimpleTypeDefinition simpleType) {
            if (simpleType.getName() == null) {
                if (simpleType.getBaseType() == null || simpleType.getBaseType().getName() == null) {
                    return null;
                }
            }

            // Проверяем, что найденный тип относится к типам xsd, а не например к simpleType
            if (simpleType.getName() != null && typeXsdList.contains(simpleType.getName())) {            
                return simpleType.getName();
            } else {
                return simpleType.getBaseType().getName();
            }
        }

        @Override
        public String generateSampleValue(org.apache.xerces.xs.XSElementDeclaration element, org.apache.xerces.xs.XSSimpleTypeDefinition simpleType) {
            String type = findNameType(simpleType);
            System.out.println("generate element: " + element.getName() + ", " + type);
            if (type == null) {
                throw new RuntimeException(String.format("Не удалось найти тип элемента \"%s\".", element.getName()));
            }

            return type;
        }

        @Override
        public String generateSampleValue(org.apache.xerces.xs.XSAttributeDeclaration attribute, org.apache.xerces.xs.XSSimpleTypeDefinition simpleType) {
            String type = findNameType(simpleType);
            System.out.println("@ generate attribute: " + attribute.getName() + ", " + type);
            if (type == null) {
                throw new RuntimeException(String.format("Не удалось найти тип атрибута \"%s\".", attribute.getName()));
            }

            return type;
        }
    };
    
    /**
     * @param args the command line arguments
     */
    public static 
    void main(String[] args) throws Exception {
        URL url = Xsd2xml_custom_generator.class.getResource("/resources/XmlSchema.xsd");
        String path = url.getFile();
        System.out.println("Xsd path: " + path);

        java.util.List<javax.xml.namespace.QName> globalElements = getGlobalElements(url);
        if (globalElements.isEmpty()) {
            throw new Exception("В схеме корневые элементы не были найдены.");
        }
        System.out.println("Корневых элементов: " + globalElements.size());
        for (javax.xml.namespace.QName root: globalElements) {
            System.out.println("  " + root);
        }
        
        // TODO: должны быть на выбор, первый попавшийся -- пока временное решение
        javax.xml.namespace.QName rootElement = globalElements.iterator().next();
        System.out.println("Первый корневой элемент: " + rootElement);
        
        XSModel xsModel = new XSParser().parse(path);
        XSInstance xsInstance = new XSInstance();
        xsInstance.generateAllChoices = Boolean.TRUE;
        xsInstance.generateOptionalElements = Boolean.TRUE;
        xsInstance.generateOptionalAttributes = Boolean.TRUE;
        xsInstance.generateFixedAttributes = Boolean.TRUE;
        xsInstance.generateDefaultAttributes = Boolean.TRUE;
        xsInstance.sampleValueGenerator = new TypeNameValueGenerator();

        // Create a StringWriter for the output
        java.io.StringWriter outWriter = new java.io.StringWriter();
        XMLDocument sampleXml = new XMLDocument(new StreamResult(outWriter), true, 4, null);
        xsInstance.generate(xsModel, rootElement, sampleXml);
        
        System.out.println();
        System.out.println("Xml:");
        String xmlString = outWriter.getBuffer().toString();
        // Pretty xml
        System.out.println(XmlObject.Factory.parse(xmlString));
    }
}
