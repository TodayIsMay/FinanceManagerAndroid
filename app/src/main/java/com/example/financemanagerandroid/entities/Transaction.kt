package com.example.financemanagerandroid.entities

import com.example.financemanagerandroid.TransactionType

class Transaction(
    var id: Long,
    var transactionType: TransactionType,
    var amount: Double,
    var comment: String,
    var category: String
) {
    override fun toString(): String {
        return "Transaction type = $transactionType, \n" +
                "amount = $amount, \n" +
                "comment = $comment, \n" +
                "category = $category"
    }
}