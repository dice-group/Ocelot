[1]: https://github.com/dice-group/FOX
[2]: http://fox-demo.aksw.org/

We will publish Ocelot's source-code, settings, datasets and results after the paper was accepted. 

The restuls of Ocelot are integerated in FOX (version 2.5.0): [`http://fox-demo.aksw.org`][1] and are availiable within the online [`WebService`][2].

Example: "Is Michelle Obama the wife of Barack Obama?"

Request FOX with CURL:

```bash
curl -d '{"input" : "Is Michelle Obama the wife of Barack Obama?","type": "text","task": "re","output": "turtle","lang": "en"}' -H "Content-Type:application/json;charset=utf-8" http://fox-demo.aksw.org/fox
```

Response in TURTLE:

```turtle
@prefix dbo:   <http://dbpedia.org/ontology/> .
@prefix foxo:  <http://ns.aksw.org/fox/ontology#> .
@prefix schema: <http://schema.org/> .
@prefix oa:    <http://www.w3.org/ns/oa#> .
@prefix foxr:  <http://ns.aksw.org/fox/resource#> .
@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix dbr:   <http://dbpedia.org/resource/> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
@prefix its:   <http://www.w3.org/2005/11/its/rdf#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .
@prefix prov:  <http://www.w3.org/ns/prov#> .
@prefix foaf:  <http://xmlns.com/foaf/0.1/> .

# RELATION EXTRACTION
foxr:1523262029418  a  oa:Annotation , rdf:Statement , foxo:Relation ;
        rdf:subject    dbr:Michelle_Obama  ;        
        rdf:predicate  dbo:spouse ;
        rdf:object     dbr:Barack_Obama ;
        oa:hasTarget   [ a             oa:SpecificResource ;
                         oa:hasSource  <http://ns.aksw.org/fox/demo/document-1#char0,43>
                       ] .

# NAMED ENTITY RECOGNITION AND LINKING            
<http://ns.aksw.org/fox/demo/document-1#char3,17>
        a                     nif:Phrase ;
        nif:anchorOf          "Michelle Obama" ;
        nif:beginIndex        "3"^^xsd:nonNegativeInteger ;
        nif:endIndex          "17"^^xsd:nonNegativeInteger ;
        nif:referenceContext  <http://ns.aksw.org/fox/demo/document-1#char0,43> ;
        its:taClassRef        foxo:PERSON , schema:Person , <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Person> , dbo:Person ;
        its:taIdentRef        dbr:Michelle_Obama .
        
# NAMED ENTITY RECOGNITION AND LINKING   
<http://ns.aksw.org/fox/demo/document-1#char30,42>
        a                     nif:Phrase ;
        nif:anchorOf          "Barack Obama" ;
        nif:beginIndex        "30"^^xsd:nonNegativeInteger ;
        nif:endIndex          "42"^^xsd:nonNegativeInteger ;
        nif:referenceContext  <http://ns.aksw.org/fox/demo/document-1#char0,43> ;
        its:taClassRef        foxo:PERSON , <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Person> , dbo:Person , schema:Person ;
        its:taIdentRef        dbr:Barack_Obama .
        
# DOCUMENT
<http://ns.aksw.org/fox/demo/document-1#char0,43>
        a               nif:CString , nif:Context ;
        nif:beginIndex  "0"^^xsd:nonNegativeInteger ;
        nif:endIndex    "43"^^xsd:nonNegativeInteger ;
        nif:isString    "Is Michelle Obama the wife of Barack Obama?" .

# SOFTWAREAGENT (PROVENANCE INFORMATION)
foxr:org.aksw.fox.tools.re.en.OcelotEN
        a                       schema:SoftwareApplication , prov:SoftwareAgent ;
        schema:softwareVersion  "n/a" ;
        foaf:name               "org.aksw.fox.tools.re.en.OcelotEN" .

# SOFTWAREAGENT (PROVENANCE INFORMATION)
foxr:org.aksw.fox.Fox
        a                       schema:SoftwareApplication , prov:SoftwareAgent ;
        schema:softwareVersion  "2.4.0" ;
        foaf:name               "org.aksw.fox.Fox" .

# SOFTWAREAGENT (PROVENANCE INFORMATION)
[ a                   foxo:NamedEntityRecognition , prov:Activity ;
  prov:endedAtTime    "2018-04-09T10:20:26.187+02:00"^^xsd:dateTime ;
  prov:generated      <http://ns.aksw.org/fox/demo/document-1#char3,17> , <http://ns.aksw.org/fox/demo/document-1#char30,42> ;
  prov:startedAtTime  "2018-04-09T10:20:25.843+02:00"^^xsd:dateTime ;
  prov:used           foxr:org.aksw.fox.Fox
] .

# ACTIVITY (PROVENANCE INFORMATION)
[ a                   foxo:RelationExtraction , prov:Activity ;
  prov:endedAtTime    "2018-04-09T10:20:27.051+02:00"^^xsd:dateTime ;
  prov:generated      foxr:1523262029418 ;
  prov:startedAtTime  "2018-04-09T10:20:26.187+02:00"^^xsd:dateTime ;
  prov:used           foxr:org.aksw.fox.tools.re.en.OcelotEN
] .
```


