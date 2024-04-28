package com.thefirstlineofcode.granite.cluster.nodes.commons.deploying;

import java.util.Collections;

public class Cluster {
	private String domainName;
	private String[] domainAliasNames;
	private String[] nodeTypes;
	
	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String[] getDomainAliasNames() {
		return domainAliasNames;
	}

	public void setDomainAliasNames(String[] domainAliasNames) {
		this.domainAliasNames = domainAliasNames;
	}

	public String[] getNodeTypes() {
		return nodeTypes;
	}

	public void setNodeTypes(String[] nodes) {
		this.nodeTypes = nodes;
	}
	
	@Override
	public String toString() {
		if(domainName==null){return "";}
		if(domainAliasNames==null||domainAliasNames.length<1){
			return domainName.trim()+"|[]";
		}
		StringBuilder sb=new StringBuilder();
		int idx=-1;
		for(String str:domainAliasNames){
			if(str==null||str.isEmpty()){continue;}
			idx++;
			if(idx>0){sb.append(",");}
			sb.append(str.trim());
		}
		return String.format("%s|[%s]",domainName.trim(),sb.toString()) ;
	}
	
}
