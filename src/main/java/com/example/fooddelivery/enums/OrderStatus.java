package com.example.fooddelivery.enums;


public enum OrderStatus {
    NEW, //новый
    CONFIRMED, //оплачен
    CANCELLED, //отмене
    PREPARING, //готовится
    READY, //готов на выдачу курьеру
    TAKED, //дсотавляется
    DELIVERED //доставлен
}
