package dk.aau.cs.qweb.airbase;

public class Config {

	private static String cubeStructurePath = "src/test/resources/Ontology/Airbase.ttl";
	private static String namespace = "http://qweb.cs.aau.dk/airbase/";
	private static String metadataGraphLabel = namespace+"metadata/";
	private static String provenanceGraphLabel = namespace+"provenance/";
	private static String file;
	private static String xmlFile;
	private static String dbLocation;

	public static String getNamespace() {
		return namespace;
	}

	public static String getCubeStructureFilePath() {
		return cubeStructurePath;
	}

	public static void setCubeStructurePath(String cubeStructurePath) {
		Config.cubeStructurePath = cubeStructurePath;
	}

	public static String getMetadataGraphLabel() {
		return metadataGraphLabel ;
	}

	public static void setCurrentInputFilePath(String file) {
		Config.file = file;
	}
	
	public static String getCurrentInputFilePath() {
		return file;
	}

	public static String getProvenanceGraphLabel() {
		return provenanceGraphLabel;
	}

	public static String getXMLfilePath() {
		return xmlFile;
	}

	public static void setXMLfilePath(String path) {
		xmlFile = path;
	}

	public static String getDBLocation() {
		return dbLocation;
	}

	public static void setDBLocation(String dbLocation) {
		Config.dbLocation = dbLocation;
	}
}
