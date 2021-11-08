package org.auscope.portal.core.cloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * Represent a directory in cloud storage, may contain other directories and/or files.
 * 
 * @author woo392
 *
 */
public class CloudDirectoryInformation implements Serializable {

	private static final long serialVersionUID = 8154906555042742255L;
	
	// Name of directory, this will be the path fragment relative to the parent
	private String name;
	// Complete path from root to this directory, will be null if this is the job output root directory
	private String path;
	@JsonIgnore
	private CloudDirectoryInformation parent;
	// List of files within this directory
	private ArrayList<CloudFileInformation> files = new ArrayList<CloudFileInformation>();
	// List of directories within this directory
	private ArrayList<CloudDirectoryInformation> directories = new ArrayList<CloudDirectoryInformation>();
	
	public CloudDirectoryInformation(String name, CloudDirectoryInformation parent) {
		this.name = name;
		this.parent = parent;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	// Path will be built top down from all parents' directory names
	public String getPath() {
		Stack<String> stack = new Stack<String>();
		CloudDirectoryInformation cloudDir = this;
		while (cloudDir != null) {
			if(cloudDir.getName() != "") {
				stack.push(cloudDir.getName() + "/");
			}
			cloudDir = cloudDir.getParent();
		}
		String path = "";
		while (!stack.empty()) {
			path += stack.pop();
		}
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public CloudDirectoryInformation getParent() {
		return parent;
	}
	
	public void setParent(CloudDirectoryInformation parent) {
		this.parent = parent;
	}
	
	public ArrayList<CloudFileInformation> getFiles() {
		return files;
	}
	
	public void setFiles(ArrayList<CloudFileInformation> files) {
		this.files = files;
	}
	
	public void addFile(CloudFileInformation file) {
		this.files.add(file);
	}

	public ArrayList<CloudDirectoryInformation> getDirectories() {
		return directories;
	}

	public void setDirectories(ArrayList<CloudDirectoryInformation> directories) {
		this.directories = directories;
	}
	
	public void addDirectory(CloudDirectoryInformation directory) {
		this.directories.add(directory);
	}
	
}
