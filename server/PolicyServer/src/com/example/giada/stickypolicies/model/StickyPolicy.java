package com.example.giada.stickypolicies.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class StickyPolicy {
	private String[] trustedAuthority;
	private Owner owner;
	private List<Policy> policies;

	public StickyPolicy(String[] trustedAuthority) {
		super();
		this.trustedAuthority = trustedAuthority;
		this.owner = new Owner();
		this.policies = new ArrayList<Policy>();
	}

	private StickyPolicy() {
		super();
	}

	public void addPolicy(String[] targets, String[] dataTypes, Date validityDate, String[] constraints, String[] action) {
		this.policies.add(new Policy(targets, dataTypes, validityDate, constraints, action));
	}

	public String[] getTrustedAuthority() {
		return trustedAuthority;
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(String referenceName, String[] details, BigInteger certificateSN) {
		this.owner = new Owner(referenceName, details, certificateSN);
	}
	
	public List<Policy> getPolicies() {
		return policies;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Policy p : policies) {
			sb.append(p.toString());
		}
		return "PKIPolicy [trustedAuthority=" + Arrays.toString(trustedAuthority) + ",\nowner=\n" + owner.toString() + "\npolicies=\n"
				+ sb.toString() + "]";
	}
}

class Policy {
	private String[] target;
	private String[] dataType;
	private Date expirationDate;
	private String[] constraint;
	private String[] action;

	protected Policy() {
	}

	protected Policy(String[] target, String[] dataType, Date expirationDate, String[] constraint, String[] action) {
		super();
		this.target = target;
		this.dataType = dataType;
		this.expirationDate = expirationDate;
		this.constraint = constraint;
		this.action = action;
	}

	public String[] getTarget() {
		return target;
	}

	void setTarget(String[] target) {
		this.target = target;
	}
	
	public String[] getDataType() {
		return dataType;
	}

	void setDataType(String[] dataType) {
		this.dataType = dataType;
	}
	
	public Date getExpirationDate() {
		return expirationDate;
	}

	void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	public String[] getConstraint() {
		return constraint;
	}

	void setConstraint(String[] constraint) {
		this.constraint = constraint;
	}
	
	public String[] getAction() {
		return action;
	}

	void setAction(String[] action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return "Policy [target=" + Arrays.toString(target) + ",\n expirationDate=" + expirationDate + ",\n constraint="
				+ Arrays.toString(constraint) + ",\n action=" + Arrays.toString(action) + "]\n";
	}
}