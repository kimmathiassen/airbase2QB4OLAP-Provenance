package dk.aau.cs.qweb.qboairbase.provenance.provo;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;

import dk.aau.cs.qweb.qboairbase.Config;
import dk.aau.cs.qweb.qboairbase.types.Object;
import dk.aau.cs.qweb.qboairbase.types.Quad;
import dk.aau.cs.qweb.qboairbase.vocabulary.PROVvocabulary;

public class Person extends Agent implements PROV {

	public Person(String string) {
		subject = Config.getProvenanceGraphLabel()+"agent/"+string;
	}

	@Override
	public List<Quad> getType() {
		List<Quad> list = new ArrayList<Quad>();
		list.addAll(super.getType());
		Quad agent = new Quad(subject, RDF.type.toString(),new Object(FOAF.Person.toString()), Config.getProvenanceGraphLabel());
		list.add(agent);
		Quad agent2 = new Quad(subject, RDF.type.toString(),new Object(PROVvocabulary.Person), Config.getProvenanceGraphLabel());
		list.add(agent2);
		return list;
	}
}
