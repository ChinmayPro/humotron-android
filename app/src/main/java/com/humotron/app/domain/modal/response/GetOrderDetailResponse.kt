package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetOrderDetailResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: OrderDetails?
) : Parcelable {

    @Parcelize
    data class OrderDetails(
        @SerializedName("order")
        val order: List<OrderItem>?,
        @SerializedName("orderNumber")
        val orderNumber: String?,
        @SerializedName("productTotalAmount")
        val productTotalAmount: Double?,
        @SerializedName("payableAmount")
        val payableAmount: Double?,
        @SerializedName("totalVAT")
        val totalVAT: Double?,
        @SerializedName("couponType")
        val couponType: String?,
        @SerializedName("couponAmount")
        val couponAmount: String?,
        @SerializedName("address")
        val address: Address?,
        @SerializedName("couponDetails")
        val couponDetails: CouponDetails?,
        @SerializedName("deliveryMethods")
        val deliveryMethods: DeliveryMethods?
    ) : Parcelable

    @Parcelize
    data class OrderItem(
        @SerializedName("quantity")
        val quantity: Int?,
        @SerializedName("totalAmount")
        val totalAmount: Double?,
        @SerializedName("productType")
        val productType: String?,
        @SerializedName("itemDiscount")
        val itemDiscount: Double?,
        @SerializedName("discountedAmount")
        val discountedAmount: Double?,
        @SerializedName("productDetails")
        val productDetails: ProductDetails?,
        @SerializedName("variantDetails")
        val variantDetails: VariantDetails?,
        @SerializedName("vatAmount")
        val vatAmount: Double?
    ) : Parcelable

    @Parcelize
    data class ProductDetails(
        @SerializedName("productId")
        val productId: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("price")
        val price: String?,
        @SerializedName("author")
        val author: String?,
        @SerializedName("model")
        val model: String?,
        @SerializedName("vat")
        val vat: Int?
    ) : Parcelable

    @Parcelize
    data class VariantDetails(
        @SerializedName("variantId")
        val variantId: String?,
        @SerializedName("color")
        val color: String?,
        @SerializedName("size")
        val size: String?,
        @SerializedName("image")
        val image: String?
    ) : Parcelable

    @Parcelize
    data class Address(
        @SerializedName("firstName")
        val firstName: String?,
        @SerializedName("lastName")
        val lastName: String?,
        @SerializedName("contactNo")
        val contactNo: String?,
        @SerializedName("address1")
        val address1: String?,
        @SerializedName("address2")
        val address2: String?,
        @SerializedName("address3")
        val address3: String?,
        @SerializedName("postcode")
        val postcode: String?,
        @SerializedName("city")
        val city: String?,
        @SerializedName("country")
        val country: String?,
        @SerializedName("_id")
        val id: String?
    ) : Parcelable

    @Parcelize
    data class CouponDetails(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("promoCode")
        val promoCode: String?,
        @SerializedName("discountType")
        val discountType: String?,
        @SerializedName("discountValue")
        val discountValue: Double?
    ) : Parcelable

    @Parcelize
    data class DeliveryMethods(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("methodName")
        val methodName: String?,
        @SerializedName("methodDescription")
        val methodDescription: String?,
        @SerializedName("minDays")
        val minDays: Int?,
        @SerializedName("maxDays")
        val maxDays: Int?,
        @SerializedName("price")
        val price: Double?,
        @SerializedName("estimatedDelivery")
        val estimatedDelivery: String?
    ) : Parcelable
}
