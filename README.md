# xsd_to_xml_with_custom_gen
Generation xml from xsd with custom generator (#java #jlibs #xsd #xml) 

#### Description
Generator inserts as the value of the element type.

Генератор вставляет в качестве значения элемента его тип.

### Xsd
```xsd
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <xs:element name="person">
        <xs:complexType>
            <xs:choice>
                <xs:element name="employee" type="Employee"/>
                <xs:element name="member" type="Member"/>
            </xs:choice>
        </xs:complexType>
    </xs:element> 

    <xs:complexType name="Employee">
        <xs:sequence>
            <xs:element name="name" type="xs:string"/>
            <xs:element name="age" type="xs:int"/>
            <xs:element name="hobby">
                <xs:complexType>
                    <xs:choice>
                        <xs:element name="cpp" type="xs:string"/>
                        <xs:element name="java" type="xs:string"/>
                        <xs:element name="c_sharp" type="xs:string"/>
                        <xs:element name="other">
                            <xs:complexType>
                                <xs:sequence>
                                    <xs:element name="lang" type="xs:string" maxOccurs="unbounded"/>
                                </xs:sequence>
                            </xs:complexType>
                        </xs:element>
                    </xs:choice>
                </xs:complexType>
            </xs:element>
        </xs:sequence>
        <xs:attribute name="checked" type="xs:boolean"/>
    </xs:complexType>

    <xs:complexType name="Member">
        <xs:sequence>
            <xs:element name="fio" type="xs:string"/>
            <xs:element name="weight" type="xs:int"/>
        </xs:sequence>
        <xs:attribute name="birthday" type="xs:date"/>
    </xs:complexType>

</xs:schema>
```

### Xml
```xml
<!--(employee | member)-->
<person xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <employee checked="boolean">
        <name>string</name>
        <age>int</age>
        <!--(cpp | java | c_sharp | other)-->
        <hobby>
            <!--(lang+)-->
            <other>
                <lang>string</lang>
                <lang>string</lang>
            </other>
            <c_sharp>string</c_sharp>
            <java>string</java>
            <cpp>string</cpp>
        </hobby>
    </employee>
    <member birthday="date">
        <fio>string</fio>
        <weight>int</weight>
    </member>
</person>
```
