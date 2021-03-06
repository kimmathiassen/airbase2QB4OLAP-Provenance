package dk.aau.cs.qweb.qboairbase.types;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import dk.aau.cs.qweb.qboairbase.Airbase2QB4OLAP;
import dk.aau.cs.qweb.qboairbase.Config;
import dk.aau.cs.qweb.qboairbase.Qb4OLAP.CubeStructure;
import dk.aau.cs.qweb.qboairbase.Qb4OLAP.HierarchyStep;
import dk.aau.cs.qweb.qboairbase.callback.CallBack;
import dk.aau.cs.qweb.qboairbase.provenance.Provenance;
import dk.aau.cs.qweb.qboairbase.vocabulary.DBpedia;
import dk.aau.cs.qweb.qboairbase.vocabulary.SSN;
import dk.aau.cs.qweb.qboairbase.vocabulary.Yago;

public class QuadContainer {

	private Tuple tuple;
	private Set<Quad> measureTriples = new HashSet<Quad>();
	private Set<Quad> metadataTriples = new HashSet<Quad>();
	private Set<Quad> attributeTriples = new HashSet<Quad>();
	private static int measureCounter = 1;
	
	public QuadContainer(Tuple tuple) throws FileNotFoundException, IOException {
		this.tuple = tuple;
		
		if (tupleIsAllowed()) {
			int index = 0;
			for (String predicateString : this.tuple.getHeader()) {
				if (Airbase2QB4OLAP.isPredicatePartOfCube(predicateString)) {
					String predicate = Airbase2QB4OLAP.getPredicate(predicateString);
					List<String> files = Airbase2QB4OLAP.getFiles(predicateString);
					boolean observationTriple = predicate.equals("measure"); 
					if (observationTriple) {
						predicate = "http://qweb.cs.aau.dk/airbase/schema/"+ Airbase2QB4OLAP.removeIllegalChars(tuple.getValue("component_caption"));
					}
					
					List<String> levels = Airbase2QB4OLAP.getLevels(predicateString); 
					
					for (String level : levels) {
						String literal = tuple.getData().get(index).trim();
						if (literal.equals(""))
							continue;
						String subject = createSubject(level);
						if (subject == null)
							continue;					
						
						CallBack cleanFunction = Airbase2QB4OLAP.getCallbackFunctionRawPredicate(predicateString);
						Object object = new Object(literal);
						
						if (cleanFunction != null) {
							try {
								object = cleanFunction.callBackMethod(literal, tuple);
							} catch (IllegalArgumentException e) {
								continue;
							}
						}
						
						if (!object.isEmpty()) {
							Quad quad =  new Quad(subject,predicate,(object));
							
							String graphLabel = getGraphLabel(quad, level, files, tuple);
							quad.setGraphLabel(graphLabel);
							
							metadataTriples.addAll(createMetadata(subject, level));
							if (observationTriple)	
								measureTriples.add(quad);
							else
								attributeTriples.add(quad);
						}
					}
				}
				index++;
			}
		}
	}


	private boolean tupleIsAllowed() {
		return Airbase2QB4OLAP.getAllowedComponents().contains(tuple.getValue("component_caption"));
	}

	private String getGraphLabel(Quad quad, String level, List<String> files, Tuple tuple) {
		Provenance index = Provenance.getInstance();
		String provenanceIdentifier = index.getProvenanceIdentifier(quad, level, files, tuple);
		return provenanceIdentifier;
	}

	private String createSubject(String level) {
		String subject = Config.getDataNamespace();
		if (level.equals("http://qweb.cs.aau.dk/airbase/schema/value")) {
			subject+="observation/"+Config.getCountryCode()+measureCounter;
			measureCounter++;
		} else {
			String suffix = Airbase2QB4OLAP.getSuffixUsedInIRI(level, tuple);
			if (suffix == null) {
				return null;
			}
			
			subject = subject + Airbase2QB4OLAP.removePrefix(level)+ "/" + suffix;
		}
		
		return subject;
	}

	
	private Set<Quad> createMetadata(String subject, String level) throws FileNotFoundException, IOException {
		CubeStructure cs = CubeStructure.getInstance();
		Set<Quad> quads = new HashSet<Quad>();
		
		if (level.equals("http://qweb.cs.aau.dk/airbase/schema/value")) { //Handle Observations			
			Quad year = new Quad(subject, "http://qweb.cs.aau.dk/airbase/schema/year", new Object (createSubject("http://qweb.cs.aau.dk/airbase/schema/year")), Config.getMetadataGraphLabel());
			quads.add(year);
			
			Quad sensor = new Quad(subject, "http://qweb.cs.aau.dk/airbase/schema/sensor", new Object (createSubject("http://qweb.cs.aau.dk/airbase/schema/sensor")), Config.getMetadataGraphLabel());
			quads.add(sensor);
			
			Quad station = new Quad(subject, "http://qweb.cs.aau.dk/airbase/schema/station", new Object (createSubject("http://qweb.cs.aau.dk/airbase/schema/station")), Config.getMetadataGraphLabel());
			quads.add(station);
			
		} else { 
			if (level.equals("http://qweb.cs.aau.dk/airbase/schema/city")) {
				String sj = createSubject(level);
				if (sj != null) {
					Quad dbpedia = new Quad(sj, OWL.sameAs.toString(), new dk.aau.cs.qweb.qboairbase.types.Object(DBpedia.Resource + wikify(Airbase2QB4OLAP.getSuffixUsedInIRI(level, tuple))), Config.getMetadataGraphLabel());
					Quad yago = new Quad(sj, OWL.sameAs.toString(),  new dk.aau.cs.qweb.qboairbase.types.Object(Yago.Resource + wikify(Airbase2QB4OLAP.getSuffixUsedInIRI(level, tuple))), Config.getMetadataGraphLabel());
					quads.add(dbpedia);
					quads.add(yago);
				}
			} else if (level.equals("http://qweb.cs.aau.dk/airbase/schema/component")) {
				String object = null;
				String sj = createSubject(level);
				if (sj == null) {
					return quads;
				}
				
				String relation = OWL.sameAs.toString();			
				switch(tuple.getValue("component_caption")) {
				case "SO2" :
					object = "Sulfure_Dioxide";
					break;
				case "SPM" : case "PM10" : case "PM2.5" :
					object = "Particulates";
					relation = RDFS.seeAlso.toString();
					break;
				case "BS" :
					break;
				case "O3" :
					object = "Ozone";
					break;
				case "NO2" :
					object = "Nitrogen_dioxide";
					break;
				case "NOX" :
					object = "NOx";
					break;
				case "CO" :
					object = "Carbon_monoxide";
					break;
				case "Pb" :
					object = "Lead";
					break;
				case "Hg" : 
					object = "Mercury_%28element%29";
					break;
				case "Cd" :
					object = "Cadmium";
					break;
				case "Ni" :
					object = "Nickel";
					break;
				case "As" :
					object = "Arsenic";
					break;
				case "C6H6" :
					object = "Benzene";
					break;
					
				}
				if (object != null) {
					quads.add(new Quad(sj, relation, new dk.aau.cs.qweb.qboairbase.types.Object(DBpedia.Resource + object), Config.getMetadataGraphLabel()));
					quads.add(new Quad(sj, relation, new dk.aau.cs.qweb.qboairbase.types.Object(Yago.Resource + object), Config.getMetadataGraphLabel()));
				}
				
				quads.add(new Quad(sj, RDF.type.toString(), new Object(SSN.Property), Config.getMetadataGraphLabel()));
				String sensorSj = createSubject("http://qweb.cs.aau.dk/airbase/schema/sensor");
				quads.add(new Quad(sensorSj, SSN.observes, new Object(sj), Config.getMetadataGraphLabel()));
				
			}
			
			Quad quad1 = new Quad(subject, "http://purl.org/qb4olap/cubes#memberOf", new Object(level), Config.getMetadataGraphLabel());
			quads.add(quad1);
						
			for (HierarchyStep hs : cs.getHierarchyStepByParentLevel(level)) {
				String childLevel = createSubject(hs.getChildLevel());
				if (childLevel != null) {
					Quad quad2 = new Quad(childLevel, hs.getRollup(), new Object(subject), Config.getMetadataGraphLabel());
					quads.add(quad2);
				}
			}
			
		}
		
		quads.addAll(getTypes(subject, level));
		
		return quads;
	}
	
	/**
	 * Get all the statements of type rdf:type
	 * @param subject
	 * @param level
	 * @return
	 */
	private List<Quad> getTypes(String subject, String level) {
		List<Quad> quads = new ArrayList<>();
		Quad quad = null;
		switch(level) {
		case "http://qweb.cs.aau.dk/airbase/schema/value":
			Quad quad1 = new Quad(subject, RDF.type.toString() , new Object("http://purl.org/linked-data/cube#Observation"), Config.getMetadataGraphLabel());
			quads.add(quad1);
						
			Quad quad2 = new Quad(subject, RDF.type.toString() , new Object("http://purl.org/linked-data/cube#dataSet"),Config.getMetadataGraphLabel());
			quads.add(quad2);
			
			Quad quad3 = new Quad(subject, RDF.type.toString() , new Object(SSN.Observation), Config.getMetadataGraphLabel());
			quads.add(quad3);
			
			break;
		case "http://qweb.cs.aau.dk/airbase/schema/component" :
			quad = new Quad(subject, RDF.type.toString(), new Object(SSN.Property), Config.getMetadataGraphLabel());
			quads.add(quad);
			break;
		case "http://qweb.cs.aau.dk/airbase/schema/sensor" :			
			quad = new Quad(subject, RDF.type.toString(), new Object(SSN.Sensor), Config.getMetadataGraphLabel());
			quads.add(quad);
			break;
		}

		return quads;
	}


	private String wikify(String subject) {
		return WordUtils.capitalizeFully(subject).replaceAll(" ", "_").replace("/", "");
	}

	public Set<Quad> getInformationTriples() {
		return measureTriples;
	}
	
	public Set<Quad> getMetadataTriples() {
		return metadataTriples;
	}
	
	public Set<Quad> getProvenanceTriples() {
		Provenance index = Provenance.getInstance();
		return index.getProvenanceQuads();
	}

	public Set<Quad> getAttributeTriples() {
		return attributeTriples;
	}
}
