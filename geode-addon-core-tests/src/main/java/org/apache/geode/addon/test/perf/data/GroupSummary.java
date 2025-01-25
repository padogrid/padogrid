package org.apache.geode.addon.test.perf.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import org.apache.geode.DataSerializable;
import org.apache.geode.addon.test.perf.EligFunction;

/**
 * GroupSummary contains group summary information aggregated by
 * {@linkplain EligFunction}.
 * 
 * @author dpark
 *
 */
public class GroupSummary implements DataSerializable {
	private static final long serialVersionUID = 1L;
	
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
	public void toData(DataOutput out) throws IOException {
		out.writeUTF(groupNumber);
		out.writeUTF(carrierNumber);
		out.writeUTF(contractNumber);
		out.writeInt(memberCount);
		out.writeLong(totalBlobSize);
		out.writeLong(writtenTime.getTime());
	}

	@Override
	public void fromData(DataInput in) throws IOException, ClassNotFoundException {
		groupNumber = in.readUTF();
		carrierNumber = in.readUTF();
		contractNumber = in.readUTF();
		memberCount = in.readInt();
		totalBlobSize = in.readLong();
		writtenTime = new Date(in.readLong());
	}
}
