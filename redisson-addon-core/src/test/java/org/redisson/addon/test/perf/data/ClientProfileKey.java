package org.redisson.addon.test.perf.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * ClientProfileKey is a composite key class containing client profile information.
 * @author dpark
 *
 */
public class ClientProfileKey implements Externalizable
{	
	private static final long serialVersionUID = 1L;
	
	private String groupNumber;
	private String carrierNumber;
	private String contractNumber;

	public ClientProfileKey()
	{
	}

	public ClientProfileKey(String groupNumber)
	{
		this(groupNumber, null, null);
	}
	
	public ClientProfileKey(String groupNumber, String carrierNumber)
	{
		this(groupNumber, carrierNumber, null);
	}
	
	public ClientProfileKey(String groupNumber, String carrierNumber, String contractNumber)
	{
		this.groupNumber = groupNumber;
		this.carrierNumber = carrierNumber;
		this.contractNumber = contractNumber;
	}
	
	public String getGroupNumber()
	{
		return groupNumber;
	}

	public void setGroupNumber(String groupNumber)
	{
		this.groupNumber = groupNumber;
	}

	public String getCarrierNumber()
	{
		return carrierNumber;
	}

	public void setCarrierNumber(String carrierNumber)
	{
		this.carrierNumber = carrierNumber;
	}

	public String getContractNumber()
	{
		return contractNumber;
	}

	public void setContractNumber(String contractNumber)
	{
		this.contractNumber = contractNumber;
	}

	public int hashCode()
	{
		int hash = 7;
		hash = 31 * hash + (this.groupNumber == null ? 0 : this.groupNumber.hashCode());
		return hash;
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientProfileKey other = (ClientProfileKey) obj;
		return (groupNumber == other.groupNumber || groupNumber != null && groupNumber.equals(other.groupNumber));
	}

	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(groupNumber);
		out.writeUTF(carrierNumber);
		out.writeUTF(contractNumber);
	}
	
	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		groupNumber = in.readUTF();
		carrierNumber = in.readUTF();
		contractNumber = in.readUTF();
	}
}
