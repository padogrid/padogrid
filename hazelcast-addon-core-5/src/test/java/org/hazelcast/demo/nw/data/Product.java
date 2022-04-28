package org.hazelcast.demo.nw.data;

import java.io.IOException;


import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

/**
  * Product is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema products.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
public class Product extends BaseEntity implements VersionedPortable
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
	public int getClassId() 
	{
		return PortableFactoryImpl.Product_CLASS_ID;
	}

	@Override
	public int getFactoryId() {
		return PortableFactoryImpl.FACTORY_ID;
	}
	
	@Override
	public int getClassVersion() {
		return 1;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		super.writePortable(writer);
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
	public void readPortable(PortableReader reader) throws IOException {
		super.readPortable(reader);
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
}
