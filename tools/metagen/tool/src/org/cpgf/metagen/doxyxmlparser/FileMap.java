package org.cpgf.metagen.doxyxmlparser;

import java.io.IOException;
import java.util.*; //scturner

import org.cpgf.metagen.Util;
import org.cpgf.metagen.Config; //scturner
import org.cpgf.metagen.metadata.MetaInfo;
import org.cpgf.metagen.filters.*;
import org.w3c.dom.Node;


public class FileMap {
	private HashMap<String, FileInfo> fileMap;
	private List<String> locationList;

	public FileMap() {
		this.fileMap = new HashMap<String, FileInfo>();
		this.locationList = new ArrayList<String>();
	}

	public void addLocation(String location, Node node) {
		if(! this.fileMap.containsKey(location)) {
			this.fileMap.put(location, new FileInfo(location));
			this.locationList.add(location);
		}

		FileInfo fileInfo = this.fileMap.get(location);
		for(Node child : Util.getChildNodesByName(node, "innernamespace")) {
			fileInfo.getNamespaceList().add(Util.getNodeText(child));
		}
	}

	public HashMap<String, FileInfo> getFileMap() {
		return fileMap;
	}

	//scturner
	/**
	 * Based on the configuration options excludeSource and excludeRegEx, this
	 * decides what files (and, therefore, classes/functions) to exclude from
	 * the metadata generation.
	 * 
	 * @param config The configuration for the program
	 */
	public void filterFiles(Config config) {
		Iterator<String> setIterator  = fileMap.keySet().iterator();
		while (setIterator.hasNext()) {
			String fileName = setIterator.next();
			if (!FileFilter.getFilter(config).acceptFile(fileName)) {
				setIterator.remove(); //remove the file from the fileMap
				locationList.remove(fileName);
			}
		}
	}

	//TODO move some place else???  scturner
	public void createFakeHeaders(Config config, MetaInfo metainfo) {
		if (config.createHeaderFiles) {
			//find unmatched .cpp files
			UnmatchedSourceFilter srcFilter = new UnmatchedSourceFilter(this, 
					config);
			Set<String> unmatched = srcFilter.getUnmatchedSourceFiles();


			for (String src : unmatched) {
				String headerName = srcFilter.getHeaderName(src);
				FileInfo info = fileMap.get(src);
				try {
					//create headers
					FakeHeaderWriter header = new FakeHeaderWriter(headerName, 
							src, metainfo, config);
					header.write();
					//adjust filemap
					FileInfo headerInfo = new FileInfo(info.getLocation());
					headerInfo.appendNamespaces(info.getNamespaceList());
					fileMap.put(headerName, headerInfo);	
					locationList.add(headerName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	    
		}
	}
}
