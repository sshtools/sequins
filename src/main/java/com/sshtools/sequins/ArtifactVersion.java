/**
 * Copyright © 2023 JAdaptive Limited (support@jadaptive.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sshtools.sequins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Use the copy of this in "Jaul" project.
 */
@Deprecated(forRemoval = true, since = "0.0.2-SNAPSHOT")
public class ArtifactVersion {

	static Map<String, String> versions = Collections.synchronizedMap(new HashMap<>());

	public static String getVersion(String groupId, String artifactId) {
		return getVersion(null, groupId, artifactId);
	}
	
	public static String getVersion(String installerShortName, String groupId, String artifactId) {
		String fakeVersion = Boolean.getBoolean("jadaptive.development")
				? System.getProperty("jadaptive.development.version", System.getProperty("jadaptive.devVersion"))
				: null;
		if (fakeVersion != null) {
			return fakeVersion;
		}

		String detectedVersion = versions.getOrDefault(groupId+ ":" + artifactId, "");
		if (!detectedVersion.equals(""))
			return detectedVersion;
		
		/* installed apps may have a .install4j/i4jparams.conf. If this XML
		 * file exists, it will contain the full application version which
		 * will have the build number in it too. 
		 */
		if(installerShortName != null) {
			try {
				var docBuilderFactory = DocumentBuilderFactory.newInstance();
				var docBuilder = docBuilderFactory.newDocumentBuilder();
				var appDir = new File(System.getProperty("install4j.installationDir", System.getProperty("user.dir")));
				var doc = docBuilder.parse(new File(new File(appDir, ".install4j"),"i4jparams.conf"));
				var el = doc.getDocumentElement().getElementsByTagName("general").item(0);
				var mediaName = el.getAttributes().getNamedItem("mediaName").getTextContent();
				if(mediaName.startsWith(installerShortName + "-")) {
					detectedVersion = el.getAttributes().getNamedItem("applicationVersion").getTextContent();
				}
			} catch (Exception e) {
			}
		}

		if (detectedVersion.equals("")) {		
	
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
