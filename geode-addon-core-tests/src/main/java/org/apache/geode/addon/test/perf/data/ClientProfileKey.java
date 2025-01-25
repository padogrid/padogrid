package org.apache.geode.addon.test.perf.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.geode.DataSerializable;
import org.apache.geode.cache.EntryOperation;
import org.apache.geode.cache.PartitionResolver;

/**
 * ClientProfileKey is a composite key class containing client profile information.
 * @author dpark
 *
 */
public class ClientProfileKey implements DataSerializable, PartitionResolver<String, ClientProfileKey>
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
		return this.groupNumber == null ? 0 : this.groupNumber.hashCode();
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		String otherGroupNumber = obj.toString();
		return (groupNumber == otherGroupNumber || groupNumber != null && groupNumber.equals(otherGroupNumber));
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 */
	@Override
	public void toData(DataOutput out) throws IOException {
		out.writeUTF(groupNumber);
		out.writeUTF(carrierNumber);
		out.writeUTF(contractNumber);
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 */
	@Override
	public void fromData(DataInput in) throws IOException, ClassNotFoundException {
		groupNumber = in.readUTF();
		carrierNumber = in.readUTF();
		contractNumber = in.readUTF();
	}

	/**
	 * Returns the group number as the routing object.
	 */
	@Override
	public Object getRoutingObject(EntryOperation<String, ClientProfileKey> opDetails) {
		return groupNumber;
	}

	@Override
	public String getName() {
		return "ClientProfileKeyResolver";
	}
}
