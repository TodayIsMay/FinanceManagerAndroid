package com.example.financemanagerandroid.entities

class Category(var id: Long, var name: String) {
    override fun toString(): String {
        return name
    }
}