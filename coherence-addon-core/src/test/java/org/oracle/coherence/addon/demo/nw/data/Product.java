package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class Product extends BaseEntity implements PortableObject
{
	private String productId;
	private String productName;
	private String supplierId;
	private String categoryId;
	private String quantityPerUnit;
	private double unitPrice;
	private int unitsInStock;
	private int unitsOnOrder;
	private int reorderLevel;
	private int discontinued;

	public Product()
	{
	}

	public void setProductId(String productId) {
		this.productId=productId;
	}

	public String getProductId() {
		return this.productId;
	}

	public void setProductName(String productName) {
		this.productName=productName;
	}

	public String getProductName() {
		return this.productName;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId=supplierId;
	}

	public String getSupplierId() {
		return this.supplierId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId=categoryId;
	}

	public String getCategoryId() {
		return this.categoryId;
	}

	public void setQuantityPerUnit(String quantityPerUnit) {
		this.quantityPerUnit=quantityPerUnit;
	}

	public String getQuantityPerUnit() {
		return this.quantityPerUnit;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice=unitPrice;
	}

	public double getUnitPrice() {
		return this.unitPrice;
	}

	public void setUnitsInStock(int unitsInStock) {
		this.unitsInStock=unitsInStock;
	}

	public int getUnitsInStock() {
		return this.unitsInStock;
	}

	public void setUnitsOnOrder(int unitsOnOrder) {
		this.unitsOnOrder=unitsOnOrder;
	}

	public int getUnitsOnOrder() {
		return this.unitsOnOrder;
	}

	public void setReorderLevel(int reorderLevel) {
		this.reorderLevel=reorderLevel;
	}

	public int getReorderLevel() {
		return this.reorderLevel;
	}

	public void setDiscontinued(int discontinued) {
		this.discontinued=discontinued;
	}

	public int getDiscontinued() {
		return this.discontinued;
	}

	@Override
	public String toString()
	{
		return "[categoryId=" + this.categoryId
			 + ", discontinued=" + this.discontinued
			 + ", productId=" + this.productId
			 + ", productName=" + this.productName
			 + ", quantityPerUnit=" + this.quantityPerUnit
			 + ", reorderLevel=" + this.reorderLevel
			 + ", supplierId=" + this.supplierId
			 + ", unitPrice=" + this.unitPrice
			 + ", unitsInStock=" + this.unitsInStock
			 + ", unitsOnOrder=" + this.unitsOnOrder + "]";
	}
	
	@Override
	public void readExternal(PofReader reader) throws IOException {
		int i = super.readExternal(0, reader);
		this.productId = reader.readString(i++);
		this.productName = reader.readString(i++);
		this.supplierId = reader.readString(i++);
		this.categoryId = reader.readString(i++);
		this.quantityPerUnit = reader.readString(i++);
		this.unitPrice = reader.readDouble(i++);
		this.unitsInStock = reader.readInt(i++);
		this.unitsOnOrder = reader.readInt(i++);
		this.reorderLevel = reader.readInt(i++);
		this.discontinued = reader.readInt(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = super.writeExternal(0, writer);
		writer.writeString(i++, productId);
		writer.writeString(i++, productName);
		writer.writeString(i++, supplierId);
		writer.writeString(i++, categoryId);
		writer.writeString(i++, quantityPerUnit);
		writer.writeDouble(i++, unitPrice);
		writer.writeInt(i++, unitsInStock);
		writer.writeInt(i++, unitsOnOrder);
		writer.writeInt(i++, reorderLevel);
		writer.writeInt(i++, discontinued);
	}
}
