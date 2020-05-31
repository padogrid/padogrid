package org.oracle.coherence.addon.test.perf.data;

import java.io.IOException;
import java.util.Date;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * GroupSummary contains group summary information aggregated by
 * {@linkplain EligCallable}.
 * 
 * @author dpark
 *
 */
public class GroupSummary implements PortableObject {
	private String groupNumber;
	private String carrierNumber;
	private String contractNumber;
	private int memberCount;
	private long totalBlobSize;
	private Date writtenTime;

	public GroupSummary() {
	}

	public GroupSummary(ClientProfileKey profileKey, Date writtenTime) {
		this.groupNumber = profileKey.getGroupNumber();
		this.carrierNumber = profileKey.getCarrierNumber();
		this.contractNumber = profileKey.getContractNumber();
		this.writtenTime = writtenTime;
	}

	public GroupSummary(ClientProfileKey profileKey, int memberCount, long totalBlobSize, Date writtenTime) {
		this(profileKey, writtenTime);
		this.memberCount = memberCount;
		this.totalBlobSize = totalBlobSize;
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

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public long getTotalBlobSize() {
		return totalBlobSize;
	}

	public void setTotalBlobSize(long totalBlobSize) {
		this.totalBlobSize = totalBlobSize;
	}

	public Date getWrittenTime() {
		return writtenTime;
	}

	public void setWrittenTime(Date writtenTime) {
		this.writtenTime = writtenTime;
	}

	@Override
	public String toString() {
		return "GroupSummary [groupNumber=" + groupNumber + ", carrierNumber=" + carrierNumber + ", contractNumber="
				+ contractNumber + ", memberCount=" + memberCount + ", totalBlobSize=" + totalBlobSize
				+ ", writtenTime=" + writtenTime + "]";
	}

	@Override
	public void readExternal(PofReader reader) throws IOException {
		int i = 0;
		groupNumber = reader.readString(i++);
		carrierNumber = reader.readString(i++);
		contractNumber = reader.readString(i++);
		memberCount = reader.readInt(i++);
		totalBlobSize = reader.readLong(i++);
		writtenTime = new Date(reader.readLong(i++));
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = 0;
		writer.writeString(i++, groupNumber);
		writer.writeString(i++, carrierNumber);
		writer.writeString(i++, contractNumber);
		writer.writeInt(i++, memberCount);
		writer.writeLong(i++, totalBlobSize);
		writer.writeLong(i++, writtenTime.getTime());
	}
}
