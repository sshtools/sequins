package com.sshtools.sequins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

public class ArtifactVersion {

	static Map<String, String> versions = Collections.synchronizedMap(new HashMap<>());
	
	public static String getVersion(String groupId, String artifactId) {
		String fakeVersion = Boolean.getBoolean("jadaptive.development")
				? System.getProperty("jadaptive.development.version", System.getProperty("jadaptive.devVersion"))
				: null;
		if (fakeVersion != null) {
			return fakeVersion;
		}

		String detectedVersion = versions.getOrDefault(groupId+ ":" + artifactId, "");
		if (!detectedVersion.equals(""))
			return detectedVersion;

		// try to load from maven properties first
		try {
			var p = new Properties();
			var is = ArtifactVersion.class.getClassLoader()
					.getResourceAsStream("META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");
			if (is == null) {
				is = ArtifactVersion.class
						.getResourceAsStream("/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");
			}
			if (is != null) {
				try {
					p.load(is);
					detectedVersion = p.getProperty("version", "");
				} finally {
					is.close();
				}
			}
		} catch (Exception e) {
			// ignore
		}

		// fallback to using Java API
		if (detectedVersion.equals("")) {
			var aPackage = ArtifactVersion.class.getPackage();
			if (aPackage != null) {
				detectedVersion = aPackage.getImplementationVersion();
				if (detectedVersion == null) {
					detectedVersion = aPackage.getSpecificationVersion();
				}
			}
			if (detectedVersion == null)
				detectedVersion = "";
		}

		if (detectedVersion.equals("")) {
			try {
				var docBuilderFactory = DocumentBuilderFactory.newInstance();
				var docBuilder = docBuilderFactory.newDocumentBuilder();
				var doc = docBuilder.parse(new File("pom.xml"));
				if(doc.getDocumentElement().getElementsByTagName("name").item(0).getTextContent().equals(artifactId) && doc.getDocumentElement().getElementsByTagName("group").item(0).getTextContent().equals(groupId)) {
					detectedVersion = doc.getDocumentElement().getElementsByTagName("version").item(0).getTextContent();
				}
			} catch (Exception e) {
			}

		}

		if (detectedVersion.equals("")) {
			detectedVersion = "DEV_VERSION";
		}

		/* Treat snapshot versions as build zero */
		if (detectedVersion.endsWith("-SNAPSHOT")) {
			detectedVersion = detectedVersion.substring(0, detectedVersion.length() - 9) + "-0";
		}

		versions.put(groupId+ ":" + artifactId, detectedVersion);

		return detectedVersion;
	}
}
