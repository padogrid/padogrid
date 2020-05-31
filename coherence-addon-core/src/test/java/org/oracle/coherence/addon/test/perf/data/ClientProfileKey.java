package org.oracle.coherence.addon.test.perf.data;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.cache.KeyAssociation;

public class ClientProfileKey implements PortableObject, KeyAssociation<String> {
	private String groupNumber;
	private String carrierNumber;
	private String contractNumber;

	public ClientProfileKey(String groupNumber) {
		this(groupNumber, null, null);
	}

	public ClientProfileKey(String groupNumber, String carrierNumber) {
		this(groupNumber, carrierNumber, null);
	}

	public ClientProfileKey(String groupNumber, String carrierNumber, String contractNumber) {
		this.groupNumber = groupNumber;
		this.carrierNumber = carrierNumber;
		this.contractNumber = contractNumber;
	}

	public String getGroupNumber() {
		return groupNumber;
	}

	public void setGroupNumber(String groupNumber) {
		this.groupNumber = groupNumber;
	}

	public String getCarrierNumber() {
		return carrierNumber;
	}

	public void setCarrierNumber(String carrierNumber) {
		this.carrierNumber = carrierNumber;
	}

	public String getContractNumber() {
		return contractNumber;
	}

	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (this.groupNumber == null ? 0 : this.groupNumber.hashCode());
		return hash;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientProfileKey other = (ClientProfileKey) obj;
		return (groupNumber == other.groupNumber || groupNumber != null && groupNumber.equals(other.groupNumber));
	}

	@Override
	public void readExternal(PofReader reader) throws IOException {
		int i = 0;
		groupNumber = reader.readString(i++);
		carrierNumber = reader.readString(i++);
		contractNumber = reader.readString(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = 0;
		writer.writeString(i++, groupNumber);
		writer.writeString(i++, carrierNumber);
		writer.writeString(i++, contractNumber);
	}

	@Override
	public String getAssociatedKey() {
		return groupNumber;
	}

}
