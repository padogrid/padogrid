package org.hazelcast.addon.test.perf.data;

import java.io.IOException;
import java.util.Date;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.partition.PartitionAware;

/**
 * Eligibility key is a composite key class containing member information.
 * @author dpark
 *
 */
public class EligKey implements DataSerializable, PartitionAware<String>
{	
	private String memberNumber; // 18
	private String groupNumber; // 15
	private short personNumber; // 3

	// Searched by
	// 1. memberNumber
	// 2. memberNumber, groupNumber
	// 3. memberNumber, groupNumber, personNumber

	private Date effectiveDate;
	private Date termDate;
	
	private int agn;
	private short partCntlNumber;
	private int membershipAgnId;
	private int memberAgnId;

	public EligKey()
	{
	}
	
	public EligKey(String memberNumber, String groupNumber, short personNumber, Date effectiveDate)
	{
		this(memberNumber, groupNumber, personNumber, effectiveDate, null);
	}
	
	public EligKey(String memberNumber, String groupNumber, short personNumber, Date effectiveDate, Date termDate)
	{
		this(memberNumber, groupNumber, personNumber, effectiveDate, termDate, -1, (short)-1);
	}
	
	public EligKey(String memberNumber, String groupNumber, short personNumber, Date effectiveDate, Date termDate, int agn, short partCntlNumber)
	{
		this(memberNumber, groupNumber, personNumber, effectiveDate, termDate, agn, partCntlNumber, -1, -1);
	}
	
	public EligKey(String memberNumber, 
			String groupNumber, 
			short personNumber, 
			Date effectiveDate, 
			Date termDate, 
			int agn, 
			short partCntlNumber, 
			int membershipAgnId, 
			int memberAgnId)
	{
		this.memberNumber = memberNumber;
		this.groupNumber = groupNumber;
		this.personNumber = personNumber;
		this.effectiveDate = effectiveDate;
		this.termDate = termDate;
		this.agn = agn;
		this.partCntlNumber = partCntlNumber;
		this.membershipAgnId = membershipAgnId;
		this.memberAgnId = memberAgnId;
	}

	public String getMemberNumber()
	{
		return memberNumber;
	}

	public void setMemberNumber(String memberNumber)
	{
		this.memberNumber = memberNumber;
	}

	public String getGroupNumber()
	{
		return groupNumber;
	}

	public void setGroupNumber(String groupNumber)
	{
		this.groupNumber = groupNumber;
	}

	public short getPersonNumber()
	{
		return personNumber;
	}

	public void setPersonNumber(short personNumber)
	{
		this.personNumber = personNumber;
	}

	public Date getEffectiveDate()
	{
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}

	public Date getTermDate()
	{
		return termDate;
	}

	public void setTermDate(Date termDate)
	{
		this.termDate = termDate;
	}

	public int getAgn()
	{
		return agn;
	}

	public void setAgn(int agn)
	{
		this.agn = agn;
	}

	public short getPartCntlNumber()
	{
		return partCntlNumber;
	}

	public void setPartCntlNumber(short partCntlNumber)
	{
		this.partCntlNumber = partCntlNumber;
	}

	public int getMembershipAgnId()
	{
		return membershipAgnId;
	}

	public void setMembershipAgnId(int membershipAgnId)
	{
		this.membershipAgnId = membershipAgnId;
	}

	public int getMemberAgnId()
	{
		return memberAgnId;
	}

	public void setMemberAgnId(int memberAgnId)
	{
		this.memberAgnId = memberAgnId;
	}

	/**
	 * Returns the hash code of this object.
	 */
	public int hashCode()
	{
		int hash = 7;
		hash = 31 * hash + (this.memberNumber == null ? 0 : this.memberNumber.hashCode());
		hash = 31 * hash + (this.groupNumber == null ? 0 : this.groupNumber.hashCode());
		hash = 31 * hash + this.personNumber;
		hash = 31 * hash + (this.effectiveDate == null ? 0 : this.effectiveDate.hashCode());
		hash = 31 * hash + (this.termDate == null ? 0 : this.termDate.hashCode());
		return hash;
	}

	/**
	 * Returns true if the specified object equals this object.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EligKey other = (EligKey) obj;
		return (memberNumber == other.memberNumber || memberNumber != null && memberNumber.equals(other.memberNumber))
				&& (groupNumber == other.groupNumber || groupNumber != null && groupNumber.equals(other.groupNumber))
				&& personNumber == other.personNumber
				&& (effectiveDate == other.effectiveDate || effectiveDate != null
						&& effectiveDate.equals(other.effectiveDate))
				&& (termDate == other.termDate || termDate != null && termDate.equals(other.termDate));
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(memberNumber);
		out.writeUTF(groupNumber);
		out.writeShort(personNumber);
		out.writeLong(effectiveDate.getTime());
		out.writeLong(termDate.getTime());
		out.writeInt(agn);
		out.writeShort(partCntlNumber);
		out.writeInt(membershipAgnId);
		out.writeInt(memberAgnId);
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		memberNumber = in.readUTF();
		groupNumber = in.readUTF();
		personNumber = in.readShort();
		effectiveDate = new Date(in.readLong());
		termDate = new Date(in.readLong());
		agn = in.readInt();
		partCntlNumber = in.readShort();
		membershipAgnId = in.readInt();
		memberAgnId = in.readInt();
	}

	/**
	 * Returns the group number as the partition key.
	 */
	@Override
	public String getPartitionKey() {
		return groupNumber;
	}
}
