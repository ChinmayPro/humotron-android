package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetCartResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: Data?
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("deliveryMethods")
        val deliveryMethods: List<DeliveryMethod>?,
        @SerializedName("totalVAT")
        val totalVAT: Double?,
        @SerializedName("cart")
        val cart: List<CartItem>?,
        @SerializedName("totalAmount")
        val totalAmount: Double?,
        @SerializedName("couponDetails")
        val couponDetails: CouponDetails?,
        @SerializedName("address")
        val address: Address?
    ) : Parcelable

    @Parcelize
    data class DeliveryMethod(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("isFree")
        val isFree: String?,
        @SerializedName("methodName")
        val methodName: String?,
        @SerializedName("maxDays")
        val maxDays: Int?,
        @SerializedName("cartValue")
        val cartValue: Int?,
        @SerializedName("methodDescription")
        val methodDescription: String?,
        @SerializedName("price")
        val price: Double?,
        @SerializedName("termsAndCondition")
        val termsAndCondition: String?,
        @SerializedName("estimatedDelivery")
        val estimatedDelivery: String?,
        @SerializedName("minDays")
        val minDays: Int?
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
        val discountValue: Double?,
        @SerializedName("startDate")
        val startDate: String?,
        @SerializedName("endDate")
        val endDate: String?,
        @SerializedName("promoCodeName")
        val promoCodeName: String?,
        @SerializedName("minAmount")
        val minAmount: Double?,
        @SerializedName("qty")
        val qty: Int?,
        @SerializedName("isDeleted")
        val isDeleted: Boolean?,
        @SerializedName("createdAt")
        val createdAt: String?,
        @SerializedName("updatedAt")
        val updatedAt: String?
    ) : Parcelable

    @Parcelize
    data class CartItem(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("quantity")
        val quantity: Int?,
        @SerializedName("productType")
        val productType: String?,
        @SerializedName("variantDetails")
        val variantDetails: VariantDetails?,
        @SerializedName("vatAmount")
        val vatAmount: Double?,
        @SerializedName("offer")
        val offer: Offer?,
        @SerializedName("totalAmount")
        val totalAmount: Double?,
        @SerializedName("visitAddress")
        val visitAddress: VisitAddress?,
        @SerializedName("productDetails")
        val productDetails: ProductDetails?,
        @SerializedName("bookingDetails")
        val bookingDetails: BookingDetails?
    ) : Parcelable

    @Parcelize
    data class VisitAddress(
        @SerializedName("line1")
        val line1: String?,
        @SerializedName("line2")
        val line2: String?,
        @SerializedName("line3")
        val line3: String?,
        @SerializedName("city")
        val city: String?,
        @SerializedName("postcode")
        val postcode: String?,
        @SerializedName("country")
        val country: String?
    ) : Parcelable

    @Parcelize
    data class VariantDetails(
        @SerializedName("size")
        val size: String?,
        @SerializedName("variantId")
        val variantId: String?,
        @SerializedName("image")
        val image: String?,
        @SerializedName("color")
        val color: String?
    ) : Parcelable

    @Parcelize
    data class Offer(
        @SerializedName("offerId")
        val offerId: String?
    ) : Parcelable

    @Parcelize
    data class ProductDetails(
        @SerializedName("vat")
        val vat: Int?,
        @SerializedName("author")
        val author: String?,
        @SerializedName("model")
        val model: String?,
        @SerializedName("productId")
        val productId: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("price")
        val price: String?
    ) : Parcelable

    @Parcelize
    data class BookingDetails(
        @SerializedName("labAddress")
        val labAddress: String?,
        @SerializedName("addressId")
        val addressId: String?,
        @SerializedName("labPrice")
        val labPrice: Double?,
        @SerializedName("labId")
        val labId: String?,
        @SerializedName("time")
        val time: String?,
        @SerializedName("date")
        val date: String?
    ) : Parcelable

    @Parcelize
    data class Address(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("firstName")
        val firstName: String?,
        @SerializedName("lastName")
        val lastName: String?,
        @SerializedName("address1")
        val address1: String?,
        @SerializedName("address2")
        val address2: String?,
        @SerializedName("address3")
        val address3: String?,
        @SerializedName("city")
        val city: String?,
        @SerializedName("postcode")
        val postcode: String?,
        @SerializedName("country")
        val country: String?,
        @SerializedName("contactNo")
        val contactNo: String?,
        @SerializedName("isDefault")
        val isDefault: Boolean?
    ) : Parcelable
}
