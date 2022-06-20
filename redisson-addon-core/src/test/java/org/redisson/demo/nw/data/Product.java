package org.redisson.demo.nw.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
public class Product extends BaseEntity implements Externalizable
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

	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(productId);
		out.writeUTF(productName);
		out.writeUTF(supplierId);
		out.writeUTF(categoryId);
		out.writeUTF(quantityPerUnit);
		out.writeDouble(unitPrice);
		out.writeInt(unitsInStock);
		out.writeInt(unitsOnOrder);
		out.writeInt(reorderLevel);
		out.writeInt(discontinued);
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		this.productId = in.readUTF();
		this.productName = in.readUTF();
		this.supplierId = in.readUTF();
		this.categoryId = in.readUTF();
		this.quantityPerUnit = in.readUTF();
		this.unitPrice = in.readDouble();
		this.unitsInStock = in.readInt();
		this.unitsOnOrder = in.readInt();
		this.reorderLevel = in.readInt();
		this.discontinued = in.readInt();
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
