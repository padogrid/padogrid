package org.apache.geode.addon.demo.nw.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

@Entity
@Table(name = "products")
public class Product implements PdxSerializable
{
	@Id
	private String productId;
	@Column (length=100)
	private String productName;
	@Column (length=30)
	private String supplierId;
	@Column (length=30)
	private String categoryId;
	@Column (length=30)
	private String quantityPerUnit;
	@Column
	private double unitPrice;
	@Column
	private int unitsInStock;
	@Column
	private int unitsOnOrder;
	@Column
	private int reorderLevel;
	@Column
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
	public void toData(PdxWriter writer) {
		writer.writeString("productId", productId);
		writer.writeString("productName", productName);
		writer.writeString("supplierId", supplierId);
		writer.writeString("categoryId", categoryId);
		writer.writeString("quantityPerUnit", quantityPerUnit);
		writer.writeDouble("unitPrice", unitPrice);
		writer.writeInt("unitsInStock", unitsInStock);
		writer.writeInt("unitsOnOrder", unitsOnOrder);
		writer.writeInt("reorderLevel", reorderLevel);
		writer.writeInt("discontinued", discontinued);
	}

	@Override
	public void fromData(PdxReader reader) {
		this.productId = reader.readString("productId");
		this.productName = reader.readString("productName");
		this.supplierId = reader.readString("supplierId");
		this.categoryId = reader.readString("categoryId");
		this.quantityPerUnit = reader.readString("quantityPerUnit");
		this.unitPrice = reader.readDouble("unitPrice");
		this.unitsInStock = reader.readInt("unitsInStock");
		this.unitsOnOrder = reader.readInt("unitsOnOrder");
		this.reorderLevel = reader.readInt("reorderLevel");
		this.discontinued = reader.readInt("discontinued");
	}
}
