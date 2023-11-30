package com.example.taxi.domain.model.message

data class MessageResponse(

    val data: List<MessageItem>
)
data class MessageItem(
    val message: String,
    val time: String
)
