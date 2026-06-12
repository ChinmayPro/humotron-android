package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class SaveTicketResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: SaveTicketData?
)

data class SaveTicketData(
    @SerializedName("ticket")
    val ticket: SaveTicketTicket?
)

data class SaveTicketTicket(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("ticket_number")
    val ticketNumber: String?
)
